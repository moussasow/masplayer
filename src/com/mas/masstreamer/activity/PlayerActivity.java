package com.mas.masstreamer.activity;

import java.util.ArrayList;

import com.mas.masstreamer.R;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.fragments.MediaFragment;
import com.mas.masstreamer.fragments.MediaFragment.FragmentNavigation;
import com.mas.masstreamer.fragments.PlayerFragment;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.utility.MasConstants;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class PlayerActivity extends FragmentActivity implements FragmentNavigation{
	private final static String TAG = FragmentActivity.class.getSimpleName();
	private PlayerFragment mPlayerFragment;
	private MediaFragment  mMediaFragment;
	private boolean hasPlayerStarted;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		Bundle extras = getIntent().getExtras();
		if( extras != null ){
			ArrayList<MediaData> songs = extras.getParcelableArrayList(MasConstants.INTENT_MEDIA_DATA);
			String folder = extras.getString(MasConstants.INTENT_DIRECTORY);
			moveToMediaFragment(extras);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MasLog.log(TAG, "onPause()", MasLog.D);
		if( hasPlayerStarted ){
			mPlayerFragment.addNotificationToBar(PlayerActivity.this);
		}

	}

	private void moveToMediaFragment(Bundle extras) {
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		MediaFragment fragment = (MediaFragment) manager.findFragmentByTag(MediaFragment.class.getName());
		if( fragment == null){
			fragment = new MediaFragment();
			ft.add(fragment, MediaFragment.class.getName());
		}
		mMediaFragment = fragment;
		fragment.setStartPlaybackLister(this);
		fragment.setArguments(extras);
		ft.replace(R.id.player_fragment_container, fragment).commit();
	}


	@Override
	public void onAudioPlayStart(ArrayList<MediaData> data, int position) {
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(MasConstants.INTENT_MEDIA_DATA, data);
		bundle.putInt(MasConstants.INTENT_POSITION, position);
		bundle.putBoolean(MasConstants.INTENT_FROM_LIST, true);
		startPlayerFragment(bundle);
		hasPlayerStarted = true;
	}

	private void startPlayerFragment(Bundle bundle){
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		if( mMediaFragment != null){
			ft.remove(mMediaFragment);
		}
		PlayerFragment fragment = (PlayerFragment) manager.findFragmentByTag(PlayerFragment.class.getName());
		if( fragment == null){
			fragment = new PlayerFragment();
			ft.add(fragment, PlayerFragment.class.getName());
			fragment.setArguments(bundle);
		}
		ft.addToBackStack(null);
		mPlayerFragment = fragment;
		ft.replace(R.id.player_fragment_container, fragment).commit();
		
	}

	@Override
	public void onDirectoriesListed(ArrayList<MediaData> data, String directory) {
		// TODO Auto-generated method stub
		
	}

}
           