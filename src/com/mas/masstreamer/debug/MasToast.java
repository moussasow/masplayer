package com.mas.masstreamer.debug;

import android.content.Context;
import android.widget.Toast;

public class MasToast {

	/**
	 * @param context
	 * @param message to display
	 * @param type 0: short duration  1: long duration
	 */
	public static void show(Context ctx, String msg, int type){
		int duration = type == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
		Toast.makeText(ctx, msg, duration).show();
	}
}
