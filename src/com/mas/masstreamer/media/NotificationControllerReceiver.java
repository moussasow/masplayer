package com.mas.masstreamer.media;

import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.utility.MasConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class NotificationControllerReceiver extends BroadcastReceiver {
	
	private final static String TAG ="NotificationControllerReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if( intent != null){
			MasLog.log(TAG, "Received = " + intent.getAction(), MasLog.D);
			Intent notification = new Intent(MasConstants.CAST_NOTIFICATION_CONTROL);
			notification.putExtra(MasConstants.INTENT_CAST_NOTIFICATION, intent.getAction());
			LocalBroadcastManager.getInstance(context).sendBroadcast(notification);
		}
	}

}
