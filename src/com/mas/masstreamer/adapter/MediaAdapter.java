package com.mas.masstreamer.adapter;

import java.util.ArrayList;

import com.mas.masstreamer.R;
import com.mas.masstreamer.database.AudioProvider;
import com.mas.masstreamer.model.MediaData;
import com.mas.masstreamer.utility.MasFormatter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaAdapter extends BaseAdapter {
	
	private Activity mActivity;
	private ArrayList<MediaData> mData;
	private static LayoutInflater mInflater = null;
	MediaMetadataRetriever retriever;
	
	public MediaAdapter(Activity activity, ArrayList<MediaData> data){
		mActivity = activity;
		mData = data;
		mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		
		if( row == null){
			row = mInflater.inflate(R.layout.list_row, null);
			holder = new ViewHolder();
			holder.title = (TextView) row.findViewById(R.id.id_text_title);
			holder.artist = (TextView) row.findViewById(R.id.id_text_artist);
			holder.duration = (TextView) row.findViewById(R.id.id_text_duration);
			holder.image = (ImageView) row.findViewById(R.id.id_image_song);
			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}
		
		MediaData data = mData.get(position);
		holder.title.setText(data.getTitle());
		holder.artist.setText( data.getArtist() );
		holder.duration.setText( MasFormatter.getFormattedTime( data.getDuration()));
		new LoadImageTask(holder.image).execute(data.getId());
		return row;
	}
	
	private static class ViewHolder{
		TextView title;
		TextView artist;
		TextView duration;
		ImageView image;
	}
	
	private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{
		
		private ImageView artImage;
		
		public LoadImageTask(ImageView image){
			artImage = image;
		}
		
		@Override
		protected void onPreExecute() {
			retriever = new MediaMetadataRetriever();
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			//retriever.setDataSource(params[0]);
			//byte[] art = retriever.getEmbeddedPicture();
			byte[] art = null;
			Uri songs = Uri.parse(AudioProvider.URL);
			ContentResolver cr = mActivity.getContentResolver();
			String[] projection = new String[]{ AudioProvider.KEY_AUDIO_IMAGE}; // which columns you want to pick
			String selection = AudioProvider.KEY_AUDIO_ID + " = ?"; // condition  where audio_id = 

			Cursor cursor = cr.query(songs, projection, selection, new String[] {params[0]}, null);
			if( cursor.getCount() > 0 ){
				cursor.moveToFirst();
				art = cursor.getBlob(0);
			}
			cursor.close();

			Bitmap bitmap = null;
			if( art != null ){
				bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
			}
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bm) {
			if( bm != null ){
				artImage.setImageBitmap(bm);
			}
			else{
				artImage.setImageResource(R.drawable.no_image);
			}
		}
		
	}

}
