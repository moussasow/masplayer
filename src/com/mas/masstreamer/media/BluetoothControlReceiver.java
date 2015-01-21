package com.mas.masstreamer.media;


import com.mas.masstreamer.debug.MasLog;
import com.mas.masstreamer.utility.MasConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;

public class BluetoothControlReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if( event.getAction() == KeyEvent.ACTION_DOWN){
			MasLog.log("BluetoothControlReceiver", "keyreceived code = " + event.getKeyCode(), MasLog.D);
			int keyCode = event.getKeyCode();
			Intent cast = new Intent(MasConstants.CAST_BLUETOOTH_CONTROL);
			cast.putExtra(MasConstants.INTENT_CAST_BLUETOOTH, keyCode);
			LocalBroadcastManager.getInstance(context).sendBroadcast(cast);
		}
		
		abortBroadcast();
	}

}
