package com.mas.masstreamer.service;

import android.os.Binder;

public class MusicBinder extends Binder {
	private PlayerService service = null;
	
	public MusicBinder(PlayerService service){
		this.service = service;
	}
	public PlayerService getService(){
		return service;
	}
}
