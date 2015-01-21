package com.mas.masstreamer.fragments;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import com.mas.masstreamer.R;
import com.mas.masstreamer.activity.PlayerActivity;
import com.mas.masstreamer.database.AudioProvider;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.service.DatabaseManagerService;
import com.mas.masstreamer.utility.MasConstants;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class DirectoryFragment extends Fragment {

	private ListView mDirectoryList = null;
	private ProgressBar mProgressBar = null;
	private final static String TAG = "DirectoryFragment";
	private ArrayList<String> mFolders = new ArrayList<String>();
	private ArrayList<String> mDirectories = new ArrayList<String>(); // For display
	private ArrayList<MediaData> mSongs = new ArrayList<MediaData>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_directories, container,
				false);
		
		mDirectoryList = (ListView) rootView.findViewById(R.id.id_listview_directories);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.id_progress_bar_directories);
		mDirectoryList.setOnItemClickListener(mOnItemClicked);
		mSongs.clear();
		if( populatFromDatabase() ){
			obtainMusicFolders();
			setListAdapter();
		}
		else{
			new LoadDirectoriesTask().execute();
		}
		
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//mStartPlayback = (FragmentNavigation) getActivity();
	}
	
	
/*	public void setStartPlaybackLister(FragmentNavigation navigation ){
		mStartPlayback = navigation;
	}
*/	
	private OnItemClickListener mOnItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity(), PlayerActivity.class);
			Bundle bundle = new Bundle();
			intent.putParcelableArrayListExtra(MasConstants.INTENT_MEDIA_DATA, mSongs);
			intent.putExtra(MasConstants.INTENT_DIRECTORY,  mFolders.get(position));
			startActivity(intent);			
		}
	};

	private void scanDirectory(File dir) {
		
		File[] files = dir.listFiles(new AudioFilter());
		
		if( files != null ){			
			for( int i=0; i<files.length; i++ ){
				File file = files[i];
				if( file.isDirectory() ){
					if( hasAudioFile(file) ){
						MasLog.log(TAG , file.getName(), MasLog.V);
						mFolders.add(file.getAbsolutePath());
					}
					scanDirectory(file);					
				}
			}
		}
	}
	
	private boolean hasAudioFile(File dir){
		File[] files = dir.listFiles();
		if( files == null ){
			return false;
		}
		for( int i=0; i<files.length; i++ ){
			File file = files[i];
			if( !file.isDirectory() ){
				if( file.getName().toLowerCase().endsWith(".mp3") ){
					return true;
				}
			}
		}

		return false;
	}
	
	private class AudioFilter implements FileFilter{
	    // only want to see the following audio file types
	    private String[] extension = { ".mp3"};

		@Override
		public boolean accept(File file) {
			if( file.isDirectory() && !file.isHidden()){
				return true;
			}
			
			for( String ext : extension){
				if( file.getName().toLowerCase().endsWith(ext)){
					return true;
				}
			}
			return false;
		}		
	}
	
	private void startDatabaseManagerService(){
		Intent service = new Intent(getActivity(), DatabaseManagerService.class);
		getActivity().startService(service);
	}
	
	private class LoadDirectoriesTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
		
		@Override
		protected Void doInBackground(Void... arg0) {
			String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
			MasLog.log(TAG, "doInBackground = " + dir, MasLog.I);
			File rootDir = new File(dir);
			//scanDirectory(Environment.getExternalStorageDirectory());
			scanSdcard();
			obtainMusicFolders();
			//showDatabase();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			setListAdapter();
		}
	}
	
	private void setListAdapter(){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mDirectories);
		mDirectoryList.setAdapter(adapter);	
		mProgressBar.setVisibility(View.GONE);
		startDatabaseManagerService();
	}
	
	private void scanSdcard(){
		String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
		String[] projection = {
		        MediaStore.Audio.Media.TITLE,
		        MediaStore.Audio.Media.ARTIST,
		        MediaStore.Audio.Media.DATA,
		        MediaStore.Audio.Media.DISPLAY_NAME,
		        MediaStore.Audio.Media.DURATION,
		        MediaStore.Audio.Media._ID
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
					media.setId( cursor.getString(MasConstants.COL_MEDIA_ID) );
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
	
	private void obtainMusicFolders(){
		int dirLength = Environment.getExternalStorageDirectory().getAbsolutePath().length() + 1;
		for( MediaData data : mSongs ){
			File file = new File(data.getPath());
			String parent = file.getParent();
			if( !mFolders.contains(parent)){
				mFolders.add(parent);
				if( parent.length() > dirLength){
					parent = parent.substring(dirLength);
				}
				mDirectories.add(parent);
	
			}
		}
	}
	

	
	private boolean populatFromDatabase(){
		String URL = AudioProvider.URL;
		Uri songs = Uri.parse(URL);
		Cursor cursor = getActivity().getContentResolver().query(songs, null, null, null, null);
		if( cursor.getCount() > 0 ){
			cursor.moveToFirst();
			do{
				MediaData media = new MediaData();
				media.setTitle(cursor.getString( AudioProvider.INDEX_AUDIO_TITLE ));
				media.setArtist(cursor.getString( AudioProvider.INDEX_AUDIO_ARTIST ));
				media.setPath(cursor.getString( AudioProvider.INDEX_AUDIO_PATH ));
				media.setDisplay(cursor.getString( AudioProvider.INDEX_AUDIO_DISPLAY ));
				media.setDuration(cursor.getString( AudioProvider.INDEX_AUDIO_DURATION ));
				media.setId( cursor.getString( AudioProvider.INDEX_AUDIO_ID ) );
				mSongs.add(media);

				MasLog.log(TAG, "title=" + cursor.getString(cursor.getColumnIndex(AudioProvider.KEY_AUDIO_TITLE)) , MasLog.D);
			}while(cursor.moveToNext());
			cursor.close();
			return true;
		}
		else{
			cursor.close();
			return false;
		}
	}
}
