package com.mas.masstreamer.service;

import java.util.ArrayList;

import com.mas.masstreamer.database.AudioProvider;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.utility.MasConstants;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

public class DatabaseManagerService extends Service{
	
	private static final String TAG = "DatabaseManagerService";

	private ArrayList<MediaData> mSongs = new ArrayList<MediaData>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		MasLog.log(TAG, "Service onCreate()", MasLog.I);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		MasLog.log(TAG, "Service onBind()", MasLog.I);
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		MasLog.log(TAG, "Service onDestroy()", MasLog.I);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MasLog.log(TAG, "Service onStartCommand()", MasLog.I);
		scanSdcard();
		updateDatabase();
		stopSelf();
		return START_STICKY;
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
			cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
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
	
	private void updateDatabase(){
		ContentValues cv = new ContentValues();
		String URL = AudioProvider.URL;
		Uri songs = Uri.parse(URL);
		ContentResolver cr = getContentResolver();
		String[] projection = new String[]{ AudioProvider.KEY_AUDIO_ID};
		String selection = AudioProvider.KEY_AUDIO_ID + " = ?";
		Cursor c = null;

		for( MediaData data:mSongs ){
			cv.put(AudioProvider.KEY_AUDIO_ID, data.getId());
			cv.put(AudioProvider.KEY_AUDIO_TITLE, data.getTitle());
			cv.put(AudioProvider.KEY_AUDIO_ARTIST, data.getArtist());
			cv.put(AudioProvider.KEY_AUDIO_PATH, data.getPath());
			cv.put(AudioProvider.KEY_AUDIO_DISPLAY, data.getDisplay());
			cv.put(AudioProvider.KEY_AUDIO_DURATION, data.getDuration());
			cv.put(AudioProvider.KEY_AUDIO_FAVORITE, "0");
			cv.put(AudioProvider.KEY_AUDIO_PLAYBACKS, "0");
			cv.put(AudioProvider.KEY_AUDIO_IMAGE, getSongArt( data.getPath() ));

			c = cr.query(songs, projection, selection, new String[] {data.getId()}, null);
			if( c.getCount() > 0 ){
				c.moveToFirst();
				MasLog.log(TAG, "Updating row! id = " + c.getString(0), MasLog.V);
				cr.update(songs, cv, selection, new String[] {data.getId()});
			}
			else{
				Uri uri = getContentResolver().insert(AudioProvider.CONTENT_URI, cv);
				MasLog.log(TAG, uri.toString() + " inserted with id = " + data.getId(), MasLog.V);
			}
		}
		if( c != null){
			c.close();
		}
	}

	private byte[] getSongArt( String path){
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource( path );
		byte[] art = retriever.getEmbeddedPicture();
		return art;
	}

}
