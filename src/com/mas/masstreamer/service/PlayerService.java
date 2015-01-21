package com.mas.masstreamer.service;

import java.util.ArrayList;

import com.mas.masstreamer.R;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.utility.MasConstants;
import com.mas.masstreamer.utility.MasFormatter;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;

public class PlayerService extends Service implements OnPreparedListener, OnCompletionListener, OnErrorListener {

	private final static String TAG = "PlayerService";
	private MediaPlayer mPlayer = null;
	private ArrayList<MediaData> mSongs = null;
	private IBinder mBinder = null;
	private int mPosition = 0;
	private Handler mHandler = new Handler();
	private boolean isRepeatPlayback = false;
	private boolean isPlaybackPaused = false;
	private boolean isNotificationDisplayed = false;
	private Bitmap mCoverBitmap = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new MusicBinder(this);
		mPlayer = new MediaPlayer();
		initMediaPlayer();
		registerReceivers();
	}
	
	private void initMediaPlayer() {
		mPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnErrorListener(this);
	}
	
	public void setSongsList(ArrayList<MediaData> songs){
		mSongs = songs;
	}
	
	public void setCurrentPosition(int index){
		mPosition = index;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		mHandler.removeCallbacks(GetElapsedTime);
		mPlayer.stop();
		mPlayer.release();
		unregisterReceivers();
		MasLog.log(TAG, "onUnbind() called" , MasLog.D);;
		return true;
	}

	public void playAudio(){
		stopElapsedTimer();
		isPlaybackPaused = false;
		mPlayer.reset();
		MediaData md = mSongs.get(mPosition);
		String path = md.getPath();
		mCoverBitmap = updateCoverImage(path);
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepareAsync();
		} catch (Exception e) {
			MasLog.log(TAG, "Error setting data source: " + e.toString(), MasLog.E);
		}
	}
	
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		MasLog.log(TAG, "onCompletion() ", MasLog.I);
		if( isRepeatPlayback ){
			playAudio();
		}
		else{
			stopElapsedTimer();	
			playerNext();
			sendCompletionStatus();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// start playback
		mp.start();
		startElapsedTimer();
	}
	
	private Runnable GetElapsedTime = new Runnable() {
		
		@Override
		public void run() {
			int time = mPlayer.getCurrentPosition();
			sendElapsedtime(time);
			mHandler.postDelayed(GetElapsedTime, MasConstants.HANDLER_TIME_INTERVAL);
			if( isNotificationDisplayed ){
				updateNotificationToBar(time);				
			}
		}
	};
	
	private void startElapsedTimer(){
		mHandler.postDelayed(GetElapsedTime, MasConstants.HANDLER_TIME_INTERVAL);
	}
	
	private void stopElapsedTimer(){
		mHandler.removeCallbacks(GetElapsedTime);
	}
	
	private boolean isLastSong(){
		int size = mSongs.size() - 1;
		if( mPosition >= size){
			return true;
		}
		return false;
	}
	
	private void sendElapsedtime(int time){
		Intent intent = new Intent(MasConstants.CAST_ELAPSED_TIME);
		intent.putExtra(MasConstants.INTENT_CAST_TIME, time);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void sendCompletionStatus(){
		Intent intent = new Intent(MasConstants.CAST_CURRENT_POSITION);
		intent.putExtra(MasConstants.INTENT_CAST_POSITION, mPosition);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
	}
	
	// Functions called by PlayerFragment
	public void seekStart(){
		stopElapsedTimer();
	}
	
	public void seekComplete(){
		mHandler.postDelayed(GetElapsedTime, MasConstants.HANDLER_TIME_INTERVAL);
	}

	public void playerSeek(int millis){
		mPlayer.seekTo(millis);
		startElapsedTimer();
	}

	public void playerNext(){
		if( !isLastSong() ){
			mPosition++;
			playAudio();
		}
	}
	
	public void playerPrev(){
		mPosition--;
		playAudio();
	}

	public void playerUnpause(){
		mPlayer.start();
		isPlaybackPaused = false;
	}

	public void playerPause(){
		mPlayer.pause();
		isPlaybackPaused = true;
	}

	public void playerRepeat(boolean isRepeat){
		isRepeatPlayback = isRepeat;
	}

	private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getStringExtra(MasConstants.INTENT_CAST_NOTIFICATION);
			MasLog.log(TAG, "received = " + action , MasLog.I);
			if( MasConstants.NOTIFICATION_ACTION_NEXT.equals(action)){
				playerNext();
			}
			else if( MasConstants.NOTIFICATION_ACTION_PREV.equals(action)){
				if( mPosition > 0){
					playerPrev();
				}
			}
			else if( MasConstants.NOTIFICATION_ACTION_PLAY.equals(action)){
				if( isPlaybackPaused ){
					playerUnpause();
				}
				else{
					playerPause();
				}
				updateNotificationToBar( 0 );
			}
			
		}
	};

	private void registerReceivers(){
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.registerReceiver(mNotificationReceiver, new IntentFilter(MasConstants.CAST_NOTIFICATION_CONTROL));
	}
	
	private void unregisterReceivers(){
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		lbm.unregisterReceiver(mNotificationReceiver);
	}
	
	public void setNotificationStatus(boolean onOff){
		isNotificationDisplayed = onOff;
	}

	private void updateNotificationToBar(int time){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setAutoCancel(false);
		builder.setOngoing(true);
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_layout);
		MediaData md = mSongs.get(mPosition);
		views.setTextViewText(R.id.id_notify_text_title, md.getTitle());
		if( mCoverBitmap != null){
			views.setImageViewBitmap(R.id.id_notify_image_song, mCoverBitmap);
		}
		else{
			views.setImageViewResource(R.id.id_notify_image_song, R.drawable.no_image);
		}

		if( time > 0){
			views.setTextViewText(R.id.id_notify_text_artist, MasFormatter.getFormattedTime(time));
		}
		if( isPlaybackPaused ){
			views.setImageViewResource(R.id.id_notify_btn_play, R.drawable.notify_btn_play);
		}
		else{
			views.setImageViewResource(R.id.id_notify_btn_play, R.drawable.notify_btn_pause);
		}
		builder.setContent(views);		
		// add as notification
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(MasConstants.PLAYER_NOTIFICATION_ID, builder.build());
	}

	private Bitmap updateCoverImage(String path){
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(path);
		byte[] art = retriever.getEmbeddedPicture();
		Bitmap bitmap = null;
		if( art != null ){
			bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
		}
		return bitmap;		
	}
}
