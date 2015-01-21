package com.mas.masstreamer.adapter;

import java.util.ArrayList;

import com.mas.masstreamer.R;
import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.model.DownloadData;

import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DownloadAdapter extends ArrayAdapter<DownloadData> {

	private static final String TAG = "DownloadAdapter";
	private Context mContext = null;
	private ArrayList<DownloadData> mData = new ArrayList<DownloadData>();
	private LayoutInflater mInflater = null;
	ItemCallback mItemCallback;
	
	public DownloadAdapter(Context context, int resource, ArrayList<DownloadData> data, ItemCallback callback) {
		super(context, resource, data);
		mContext = context;
		mData = data;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemCallback = callback;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View root = convertView;
		ViewHolder holder = null;
		
		if( root == null){
			root = mInflater.inflate(R.layout.download_row, null);
			holder = new ViewHolder();
			holder.download = (ImageView) root.findViewById(R.id.id_imageview_surat_download);
			holder.name	= (TextView) root.findViewById(R.id.id_text_surat_download);
			holder.image = (ImageButton) root.findViewById(R.id.id_image_surat_download);
			

			holder.image.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final int pos = (Integer) v.getTag();

					MasLog.log(TAG, " Clicked at " + pos, MasLog.D);
					mItemCallback.startDownload(pos);
				}
			});
			
			root.setTag(holder);
		}
		else{
			holder = (ViewHolder) root.getTag();
		}
		holder.image.setTag(position);

		DownloadData data = mData.get(position);
		holder.name.setText(data.getName());
		if( data.isDownloaded() ){
			holder.image.setImageResource(R.drawable.delete);
			holder.download.setImageResource(R.drawable.image_downloaded);
			holder.name.setTextColor(mContext.getResources().getColor(R.color.color_downloaded));
		}
		else{
			holder.image.setImageResource(R.drawable.download);
			holder.download.setImageResource(R.drawable.image_not_downloaded);
			holder.name.setTextColor(mContext.getResources().getColor(R.color.color_not_downloaded));
		}
		return root;
	}

	
	private static class ViewHolder{
		TextView name;
		ImageButton image;
		ImageView  download;
	}
	
	public static interface ItemCallback{
		public void startDownload(int position);
	}

}
