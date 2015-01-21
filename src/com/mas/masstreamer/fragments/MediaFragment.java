package com.mas.masstreamer.fragments;

import java.io.File;
import java.util.ArrayList;








import com.mas.masstreamer.R;
import com.mas.masstreamer.adapter.MediaAdapter;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.utility.MasConstants;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MediaFragment extends Fragment {
	private final static String TAG = "PlaceholderFragment";
	private ArrayList<MediaData> mSongs = new ArrayList<MediaData>();
	private ArrayList<MediaData> mMediaData = new ArrayList<MediaData>();
	private ArrayList<String> mArtists = new ArrayList<String>();
	private ListView mList = null;
	private MediaAdapter mAdapter = null;
	private FragmentNavigation mStartPlayback;
	private String mPlayDirectory;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_media, container,
				false);
		
		Bundle bundle = this.getArguments();
		mPlayDirectory = bundle.getString(MasConstants.INTENT_DIRECTORY);
		mMediaData = bundle.getParcelableArrayList(MasConstants.INTENT_MEDIA_DATA);
		
		mList = (ListView) rootView.findViewById(R.id.id_listview_songs);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				mStartPlayback.onAudioPlayStart(mSongs, position);
			}
		});
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//mStartPlayback = (FragmentNavigation) activity;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mSongs.clear();
		mArtists.clear();
		new LoadAudioTask().execute();
	}
	
				
	private void scanSdcard(){
		final String folder = "'" + mPlayDirectory + "%'";
		String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + 
						   MediaStore.Audio.Media.DATA + " LIKE " + folder;
		String[] projection = {
		        MediaStore.Audio.Media.TITLE,
		        MediaStore.Audio.Media.ARTIST,
		        MediaStore.Audio.Media.DATA,
		        MediaStore.Audio.Media.DISPLAY_NAME,
		        MediaStore.Audio.Media.DURATION
		};
		final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
		
		Cursor cursor = null;
		try {
			Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			cursor = getActivity().getContentResolver().query(uri, projection, selection, null, sortOrder);
			if( cursor != null){
				cursor.moveToFirst();
				while( !cursor.isAfterLast() ){
					MediaData media = new MediaData();
					media.setTitle(cursor.getString(MasConstants.COL_TITLE));
					media.setArtist(cursor.getString(MasConstants.COL_ARTIST));
					media.setPath(cursor.getString(MasConstants.COL_DATA));
					media.setDisplay(cursor.getString(MasConstants.COL_DISPLAY));
					media.setDuration(cursor.getString(MasConstants.COL_DURATION));
					mSongs.add(media);
					cursor.moveToNext();
				}
				
			}
			
		} catch (Exception e) {
			MasLog.log(TAG, e.toString(), "e");
		}finally{
			if( cursor != null){
				cursor.close();
			}
		}
	}
	
	
	private class LoadAudioTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			//scanSdcard();
			getFilesFromFolder();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mAdapter = new MediaAdapter(getActivity(), mSongs);
			mList.setAdapter(mAdapter);
		}
	}
	
	private void getFilesFromFolder(){
		for( MediaData media : mMediaData){
			File file = new File(media.getPath());
			String parent = file.getParent();
			if(mPlayDirectory.equals(parent)){
				mSongs.add(media);
			}
		}
	}

	public void setStartPlaybackLister(FragmentNavigation navigation ){
		mStartPlayback = navigation;
	}

	
	public interface FragmentNavigation {
		public void onAudioPlayStart(ArrayList<MediaData> data, int position);
		public void onDirectoriesListed( ArrayList<MediaData> data, String directory );
	}

}
