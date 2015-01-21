package com.mas.masstreamer.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import com.mas.masstreamer.R;
import com.mas.masstreamer.adapter.DownloadAdapter;
import com.mas.masstreamer.database.AudioProvider;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.model.DownloadData;
import com.mas.masstreamer.utility.MediaUtil;
import com.mas.masstreamer.utility.QuranQari;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class DownloadFragment extends Fragment {

	private static final String TAG = "DownloadFragment";
	private ArrayList<DownloadData> mDownloadData = new ArrayList<DownloadData>();
	private ArrayList<String> mDownloadedSurat = new ArrayList<String>();
	private ListView mList = null;
	private DownloadAdapter mAdapter = null;
	private long downloadReference;
	MenuItem downloadItem;
	private DownloadManager mDownloadManager;
	HashMap<Long, Integer> mDownloadItems = new HashMap<Long, Integer>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_download, container,
				false);
		
		mList = (ListView) rootView.findViewById(R.id.id_listview_download);
		//downloadItem = MainActivity.mMenu.findItem(R.id.action_download);
		//downloadItem.setEnabled(false);
		return rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mDownloadData.clear();
		mDownloadedSurat.clear();
		new LoadDownloadTask().execute();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//downloadItem.setEnabled(true);
		MasLog.log(TAG, "onDestroyView()", MasLog.D);
	}
	
	private class LoadDownloadTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			populateDownloadedSurats();
			populateAdapter();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mAdapter = new DownloadAdapter(getActivity(), R.layout.download_row, mDownloadData, mItemDownloadCallback);
			mList.setAdapter(mAdapter);
		}
		
	}
	
	private void populateAdapter(){
		for( String name : QuranQari.SURATS){
			DownloadData data = new DownloadData();
			data.setName(name.substring(4));
			if( mDownloadedSurat.contains(name)){
				data.setDownloaded(true);
			}
			else{
				data.setDownloaded(false);
			}
			data.setMemorized(false);
			mDownloadData.add(data);
		}
	}
	
	private void populateDownloadedSurats() {
		Cursor cursor = getActivity().getContentResolver().query(AudioProvider.DOWNLOAD_URI, null, null, null, null);
		if( cursor.getCount() > 0 ){
			cursor.moveToFirst();
			do{
				mDownloadedSurat.add(cursor.getString(cursor.getColumnIndex(AudioProvider.KEY_DL_SURAT_NAME)));

				MasLog.log(TAG, "Name=" + cursor.getString(cursor.getColumnIndex(AudioProvider.KEY_DL_SURAT_NAME)) , MasLog.D);
			}while(cursor.moveToNext());
		}
		if( cursor != null ){
			cursor.close();
		}		
	}
	

	DownloadAdapter.ItemCallback mItemDownloadCallback = new DownloadAdapter.ItemCallback() {
		
		@Override
		public void startDownload(int position) {
			MasLog.log(TAG, "ItemCallback position = " + position, MasLog.D);
			  //set filter to only when download is complete and register broadcast receiver
			IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			getActivity().registerReceiver(downloadReceiver, filter);
			downloadFile(position);			
		}
	};
	
	private void downloadFile(int suratNumber){
		mDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		//String link = "http://download.quranicaudio.com/quran/ahmed_ibn_3ali_al-3ajamy/089.mp3";
		String surat	= QuranQari.SURATS[suratNumber];
		String number 	= surat.substring(0, QuranQari.DIGITs_LEN);
		String link = QuranQari.DOWNLOAD_URL + number + QuranQari.FILE_EXTENSION;
		Uri dlUri = Uri.parse(link);
		DownloadManager.Request request = new DownloadManager.Request(dlUri);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
		request.setTitle("Quran Download");
		request.setDescription("Downloading surah " + surat);
		
		//request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "Alfajr.mp3");
		//String path = MediaUtil.getDownloadPath();
		request.setDestinationInExternalPublicDir(MediaUtil.DOWNLOAD_DIR, surat + QuranQari.FILE_EXTENSION);
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
		downloadReference = mDownloadManager.enqueue(request);
		mDownloadItems.put(downloadReference, suratNumber);
	}


	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if( referenceId == downloadReference){
				int number = mDownloadItems.get(referenceId);
				MasLog.log(TAG, QuranQari.SURATS[number] + " Download complete!", MasLog.V);
				mDownloadItems.remove(referenceId);
				updateDatabase(number);
			}
			
		}
	};

	protected void updateDatabase(int number) {
		ContentValues cv = new ContentValues();
		ContentResolver cr = getActivity().getContentResolver();
		String[] projection = new String[]{ AudioProvider.KEY_DL_SURAT_NUMBER};
		String selection = AudioProvider.KEY_DL_SURAT_NUMBER + " = ?";
		String suratNumber = getSuratNumber(number);
		String path = MediaUtil.getDownloadPath() + "/" + QuranQari.SURATS[number] + QuranQari.FILE_EXTENSION;
		Cursor c = cr.query(AudioProvider.DOWNLOAD_URI, projection, selection, new String[] {suratNumber}, null);
		cv.put(AudioProvider.KEY_DL_SURAT_NUMBER, suratNumber);
		cv.put(AudioProvider.KEY_DL_SURAT_NAME, QuranQari.SURATS[number]);
		cv.put(AudioProvider.KEY_DL_SURAT_PATH, path);
		cv.put(AudioProvider.KEY_DL_SURAT_DOWNLOAD, QuranQari.YES);
		cv.put(AudioProvider.KEY_DL_SURAT_MEMORIZED, QuranQari.NO);

		if( c.getCount() > 0 ){
			c.moveToFirst();
			MasLog.log(TAG, "Updating row! id = " + c.getString(0), MasLog.V);
			cr.update(AudioProvider.DOWNLOAD_URI, cv, selection, new String[] {suratNumber});
		}
		else{
			Uri uri = cr.insert(AudioProvider.DOWNLOAD_URI, cv);
			MasLog.log(TAG, uri.toString() + " inserted with id = " + suratNumber, MasLog.V);
		}
		if( c != null){
			c.close();
		}
		
		
	}
	
	private String getSuratNumber( int number ){
		return	QuranQari.SURATS[number].substring(0, QuranQari.DIGITs_LEN);		
	}

}
