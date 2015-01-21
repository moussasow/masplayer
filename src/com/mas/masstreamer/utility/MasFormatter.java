package com.mas.masstreamer.utility;

import java.util.concurrent.TimeUnit;

public class MasFormatter {
	
	public static String getFormattedTime(String time){
		long ms = Integer.valueOf(time);
		int h = (int)TimeUnit.MILLISECONDS.toHours(ms) % 24;
		int m = (int)TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
		int s = (int)TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	public static String getFormattedTime(int time) {
		return getFormattedTime(String.valueOf(time));
	}

}
