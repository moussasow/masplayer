package com.mas.masstreamer.debug;

import android.util.Log;

public class MasLog {

	private static boolean bDebug = true;
	
	public static void log(String tag, String msg, String type){
		if( !bDebug ) return;
		
		if( "i".equalsIgnoreCase(type)){
			Log.i(tag, msg);
		}
		else if( "d".equalsIgnoreCase(type)){
			Log.d(tag, msg);
		}
		else if( "e".equalsIgnoreCase(type)){
			Log.e(tag, msg);
		}
		else{
			Log.v(tag, msg);
		}

	}
	
	public final static String E = "e";
	public final static String I = "i";
	public final static String V = "v";
	public final static String D = "d";
}
