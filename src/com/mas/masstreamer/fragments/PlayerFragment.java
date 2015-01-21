package com.mas.masstreamer.fragments;

import java.util.ArrayList;

import com.mas.masstreamer.R;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.media.BluetoothControlReceiver;
import com.mas.masstreamer.media.NotificationControllerReceiver;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.service.MusicBinder;
import com.mas.masstreamer.service.PlayerService;
import com.mas.masstreamer.utility.MasConstants;
import com.mas.masstreamer.utility.MasFormatter;
import com.mas.masstreamer.utility.MasRuntime;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerFragment extends Fragment implements OnSeekBarChangeListener, OnClickListener {
	private final static String TAG = "PlayerFragment";
	private ArrayList<MediaData> mSongs = new ArrayList<MediaData>();
	private MediaData mCurrentSong = null;
	private PlayerService mPlayerService = null;
	private boolean isPlayerBound = false;
	private boolean isPlayFromList = false;
	private Intent mPlayerIntent = null;
	private int mPosition = 0;
	private ImageView mCoverImage = null;
	private Bitmap mCoverBitmap = null;
	private TextView mElapsedTime = null;
	private TextView mSongTitle = null;
	private TextView mSongArtist = null;
	private TextView mSongDuration = null;
	private SeekBar mSongProgress = null;
	private ImageButton mNavPlayPause = null;
	private ImageButton mNavPlayRepeat = null;
	private ImageButton mNavPlayABRepeat = null;
	private TextView mTextABRepeat = null;
	private boolean isPrevPressed = false;
	private int mPrevPressedCount = 0;
	private int mSongDurationTime = 0;
	private boolean isPlaybackPaused = false;
	private boolean isPlaybackRepeat = false;
	private boolean isPlaybackABRepeat = false;
	private boolean isABPressed = false;
	private int[] mAtimeBtime = new int[2];
	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;
	NotificationControllerReceiver mNotificationControlReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_player, container,false);
		Bundle bundle = this.getArguments();
		mSongs = bundle.getParcelableArrayList(MasConstants.INTENT_MEDIA_DATA);
		mPosition = bundle.getInt(MasConstants.INTENT_POSITION);
		isPlayFromList = bundle.getBoolean(MasConstants.INTENT_FROM_LIST);
		mCurrentSong = mSongs.get(mPosition);
		registerReceivers();
		initializeDetails(root);
		updateUI();
		return root;
	}
	
	private void initializeDetails(View v) {
		mElapsedTime = (TextView) v.findViewById(R.id.id_text_player_current_time);
		mCoverImage = (ImageView) v.findViewById(R.id.id_image_player_cover);
		mSongProgress = (SeekBar) v.findViewById(R.id.id_seekbar_player_time);
		mSongProgress.setOnSeekBarChangeListener(this);
		mSongTitle = (TextView) v.findViewById(R.id.id_text_player_title);
		mSongArtist = (TextView) v.findViewById(R.id.id_text_player_artist);
		mSongDuration = (TextView) v.findViewById(R.id.id_text_player_duration);
		mTextABRepeat = (TextView) v.findViewById(R.id.id_text_player_ab_repeat);
		mNavPlayPause = (ImageButton) v.findViewById(R.id.id_button_player_play);
		mNavPlayPause.setOnClickListener(this);
		mNavPlayRepeat = (ImageButton) v.findViewById(R.id.id_button_player_repeat);
		mNavPlayRepeat.setOnClickListener(this);
		ImageButton nav = (ImageButton) v.findViewById(R.id.id_button_player_next);
		nav.setOnClickListener(this);
		nav = (ImageButton) v.findViewById(R.id.id_button_player_prev);
		nav.setOnClickListener(this);
		nav = (ImageButton) v.findViewById(R.id.id_button_player_back);
		nav.setOnClickListener(this);
		nav = (ImageButton) v.findViewById(R.id.id_button_player_forward);
		nav.setOnClickListener(this);
		mNavPlayABRepeat = (ImageButton) v.findViewById(R.id.id_button_player_shuffle);
		mNavPlayABRepeat.setOnClickListener(this);
	}
	
	private void updateUI(){
		mSongProgress.setMax(Integer.valueOf(mCurrentSong.getDuration()));
		mSongTitle.setText(mCurrentSong.getTitle());
		mSongArtist.setText(mCurrentSong.getArtist());
		mSongDurationTime = Integer.valueOf( mCurrentSong.getDuration());
		mSongDuration.setText(MasFormatter.getFormattedTime(mCurrentSong.getDuration()));
		new LoadCoverImageTask().execute(mCurrentSong.getPath());		
	}

	@Override
	public void onStart() {
		super.onStart();
		//boolean running = MasRuntime.isMyServiceRunning(PlayerService.class, getActivity());
		if( mPlayerIntent == null){
			mPlayerIntent = new Intent(getActivity(), PlayerService.class);
			getActivity().bindService(mPlayerIntent, musicConnection, Context.BIND_AUTO_CREATE);
			getActivity().startService(mPlayerIntent);
			isPlayFromList = false;
		}
		else{
			if( isPlayerBound && isPlayFromList){
				mPlayerService.setSongsList(mSongs);
				mPlayerService.setCurrentPosition(mPosition);
				mPlayerService.playAudio();	
				isPlayFromList = false;
			}
		}
		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		mRemoteControlResponder = new ComponentName(getActivity().getPackageName(), BluetoothControlReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);

		mNotificationControlReceiver = new NotificationControllerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MasConstants.NOTIFICATION_ACTION_PLAY);
		filter.addAction(MasConstants.NOTIFICATION_ACTION_PREV);
		filter.addAction(MasConstants.NOTIFICATION_ACTION_NEXT);
		getActivity().registerReceiver(mNotificationControlReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
		mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
	}
	
	public void exitingApp(Activity activity){
		MasLog.log(TAG, "exitingApp ="+activity, MasLog.I);
		activity.unbindService(musicConnection);
		activity.stopService(mPlayerIntent);
		try {
			activity.unregisterReceiver(mNotificationControlReceiver);
		} catch (Exception e) {
			MasLog.log(TAG, " Unregister = " + e.toString(), MasLog.E);
		}
		mPlayerService = null;
		mPlayerIntent = null;
	}
	
	private ServiceConnection musicConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			isPlayerBound = false;
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder) service;
			mPlayerService = binder.getService();
			isPlayerBound = true;
			mPlayerService.setSongsList(mSongs);
			mPlayerService.setCurrentPosition(mPosition);
			mPlayerService.playAudio();	
		}
	};
	
	private BroadcastReceiver mElapsedTimeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int time = intent.getIntExtra(MasConstants.INTENT_CAST_TIME, 0);
			//MasLog.log(TAG, "time="+time, MasLog.I);
			mSongProgress.setProgress(time);
			mElapsedTime.setText(MasFormatter.getFormattedTime(time));
			if( isPlaybackABRepeat ){
				if( time >= mAtimeBtime[1] || time <= mAtimeBtime[0]){
					mPlayerService.playerSeek(mAtimeBtime[0]);
				}
			}
		}
	};
	
	private BroadcastReceiver mCompletionReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			mPosition = intent.getIntExtra(MasConstants.INTENT_CAST_POSITION, 0);
			mCurrentSong = mSongs.get(mPosition);
			updateUI();			
		}
	};
	
	private BroadcastReceiver mBluetoothControlReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int keycode = intent.getIntExtra(MasConstants.INTENT_CAST_BLUETOOTH, 0);
			MasLog.log(TAG, "keycode="+keycode, MasLog.I);
			View v = new View(getActivity());

			switch(keycode){
			case KeyEvent.KEYCODE_MEDIA_PLAY:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				v.setId(R.id.id_button_player_play);
				onClick(v);
				break;
				
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				v.setId(R.id.id_button_player_next);
				onClick(v);
				break;
				
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				v.setId(R.id.id_button_player_prev);
				onClick(v);			
				break;
			}
		}
	};
	
	private void registerReceivers(){
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
		lbm.registerReceiver(mElapsedTimeReceiver, new IntentFilter(MasConstants.CAST_ELAPSED_TIME));
		lbm.registerReceiver(mCompletionReceiver, new IntentFilter(MasConstants.CAST_CURRENT_POSITION));
		lbm.registerReceiver(mBluetoothControlReceiver, new IntentFilter(MasConstants.CAST_BLUETOOTH_CONTROL));
	}
	
	private void unregisterReceivers(){
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
		lbm.unregisterReceiver(mElapsedTimeReceiver);
		lbm.unregisterReceiver(mCompletionReceiver);
		lbm.unregisterReceiver(mBluetoothControlReceiver);
	}
	
	private class LoadCoverImageTask extends AsyncTask<String, Void, Bitmap>{
		
		MediaMetadataRetriever retriever;
		
		@Override
		protected void onPreExecute() {
			retriever = new MediaMetadataRetriever();
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			retriever.setDataSource(params[0]);
			byte[] art = retriever.getEmbeddedPicture();
			Bitmap bitmap = null;
			if( art != null ){
				bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
			}
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bm) {
			if( bm != null ){
				mCoverImage.setImageBitmap(bm);
				mCoverBitmap = bm;
			}
			else{
				mCoverImage.setImageResource(R.drawable.no_image);
				mCoverBitmap = null;
			}
		}
		
	}

	@Override
	public void onProgressChanged(SeekBar seekbar, int progress, boolean fromTouch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekbar) {
		mPlayerService.seekStart();
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekbar) {
		int progress = seekbar.getProgress();
		mPlayerService.playerSeek(progress);
	}

	@Override
	public void onClick(View view) {
		int seekTime = 0;
		int curTime = 0;
		
		switch(view.getId()){
		case R.id.id_button_player_play:
			if( isPlaybackPaused ){
				isPlaybackPaused = false;
				mNavPlayPause.setImageResource(R.drawable.btn_pause);
				mPlayerService.playerUnpause();
			}
			else{
				isPlaybackPaused = true;
				mNavPlayPause.setImageResource(R.drawable.btn_play);
				mPlayerService.playerPause();
			}
			break;
			
		case R.id.id_button_player_next:
			if(!isLastSong()){
				mPlayerService.playerNext();
				mPosition++;
				mCurrentSong = mSongs.get(mPosition);
				updateUI();
			}
			break;
			
		case R.id.id_button_player_prev:
			prevButtonOperation();
			
			break;
		case R.id.id_button_player_forward:
			curTime = mSongProgress.getProgress();
			seekTime = curTime + MasConstants.PLAYER_SEEK_TIME;
			if( seekTime < mSongDurationTime ){
				mPlayerService.playerSeek(seekTime);
			}
			else{
				mPlayerService.playerSeek(mSongDurationTime);
			}
			
			break;
		case R.id.id_button_player_back:
			curTime = mSongProgress.getProgress();
			if( curTime > MasConstants.PLAYER_SEEK_TIME){
				seekTime = curTime - MasConstants.PLAYER_SEEK_TIME;
			}
			mPlayerService.playerSeek(seekTime);
			break;
			
		case R.id.id_button_player_repeat:
			isPlaybackABRepeat = false;
			if(isPlaybackRepeat){
				isPlaybackRepeat = false;
				mNavPlayRepeat.setImageResource(R.drawable.btn_repeat);			
			}
			else{
				isPlaybackRepeat = true;
				mNavPlayRepeat.setImageResource(R.drawable.btn_repeat_focused);
			}
			mPlayerService.playerRepeat(isPlaybackRepeat);
			break;
			
		case R.id.id_button_player_shuffle:
			isPlaybackRepeat = false;
			if( isABPressed ){
				mNavPlayABRepeat.setImageResource(R.drawable.btn_shuffle_focused);
				isPlaybackABRepeat = true;
				mAtimeBtime[1] = mSongProgress.getProgress();
				isABPressed = false;
				setABRepeatText(1);
			}
			else{
				if( isPlaybackABRepeat ){
					isPlaybackABRepeat = false;
					isABPressed = false;
					mNavPlayABRepeat.setImageResource(R.drawable.btn_shuffle);
					setABRepeatText(2);
				}
				else{
					isABPressed = true;
					mAtimeBtime[0] = mSongProgress.getProgress();
					setABRepeatText(0);
				}
			}
			break;
		}
		
	}
	
	private void setABRepeatText(int type){
		Resources rc = getActivity().getResources();
		
		if( type == 0){
			mTextABRepeat.setText(MasConstants.AB_REPEAT_A);
			mTextABRepeat.setTextColor( rc.getColor(R.color.color_ab_pressed) );
			
		}
		else if(type == 1){
			mTextABRepeat.setText(MasConstants.AB_REPEAT_AB);
			mTextABRepeat.setTextColor( rc.getColor(R.color.color_ab_pressed));			
		}
		else{
			mTextABRepeat.setText(MasConstants.AB_REPEAT_AB);
			mTextABRepeat.setTextColor( rc.getColor(R.color.color_white) );
		}
	}

	private void prevButtonOperation() {
		mPrevPressedCount++;
		if( !isPrevPressed ){
			isPrevPressed = true;
			mPrevPressTimer.start();
		}
		else{
			mPrevPressTimer.cancel();
			mPrevPressTimer.start();
		}
		
	}
	
	
	CountDownTimer mPrevPressTimer = new CountDownTimer(500, 1000) {
		
		@Override
		public void onTick(long millisUntilFinished) {}
		
		@Override
		public void onFinish() {
			MasLog.log(TAG, "Number of press = " + mPrevPressedCount, MasLog.D);
			if( mPrevPressedCount > 1){
				mPosition--;
				mCurrentSong = mSongs.get(mPosition);
				updateUI();
				mPlayerService.playerPrev();				
			}
			else{
				mPlayerService.playerSeek(0);
			}
			isPrevPressed = false;
		}
	};

	private boolean isLastSong(){
		int size = mSongs.size() - 1;
		if( mPosition >= size){
			return true;
		}
		return false;
	}
	
	public void addNotificationToBar(Activity activity){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);

		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setAutoCancel(false);
		builder.setOngoing(true);
		builder.setContentTitle(mCurrentSong.getTitle());
		builder.setContentText(mCurrentSong.getArtist());
		RemoteViews views = new RemoteViews(activity.getPackageName(), R.layout.notification_layout);
		if( mCoverBitmap != null){
			views.setImageViewBitmap(R.id.id_notify_image_song, mCoverBitmap);
		}
		else{
			views.setImageViewResource(R.id.id_notify_image_song, R.drawable.no_image);
		}
		views.setTextViewText(R.id.id_notify_text_title, mCurrentSong.getTitle());
		views.setTextViewText(R.id.id_notify_text_artist, mCurrentSong.getArtist());
	    //this is the intent that is supposed to be called when the button is clicked
	    Intent playIntent = new Intent(MasConstants.NOTIFICATION_ACTION_PLAY);
	    PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(activity, 0, playIntent, 0);
		views.setOnClickPendingIntent(R.id.id_notify_btn_play, pendingSwitchIntent);
		
	    Intent nextIntent = new Intent(MasConstants.NOTIFICATION_ACTION_PREV);
	    pendingSwitchIntent = PendingIntent.getBroadcast(activity, 0, nextIntent, 0);
		views.setOnClickPendingIntent(R.id.id_notify_btn_prev, pendingSwitchIntent);
		
	    Intent prevIntent = new Intent(MasConstants.NOTIFICATION_ACTION_NEXT);
	    pendingSwitchIntent = PendingIntent.getBroadcast(activity, 0, prevIntent, 0);		
		views.setOnClickPendingIntent(R.id.id_notify_btn_next, pendingSwitchIntent);
		
		builder.setContent(views);
		
		// add as notification
		NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(MasConstants.PLAYER_NOTIFICATION_ID, builder.build());
		if( mPlayerService != null){
			mPlayerService.setNotificationStatus(true);
		}
	}
	
	public void cancelNotificationBar(Activity activity){
		NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(MasConstants.PLAYER_NOTIFICATION_ID);
		if( mPlayerService != null){
			mPlayerService.setNotificationStatus(false);
		}
	}
}
