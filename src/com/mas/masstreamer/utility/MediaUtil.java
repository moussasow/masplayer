package com.mas.masstreamer.utility;

import java.io.File;

import android.os.Environment;

public class MediaUtil {

	public static final String DOWNLOAD_DIR = "Masplayer/Downloads";
	public static String getDownloadPath(){
		File file = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_DIR) ;
		if( !file.exists() ){
			if(!file.mkdirs()){
				return null;
			}
		}
		return file.getAbsolutePath();
	}
}
