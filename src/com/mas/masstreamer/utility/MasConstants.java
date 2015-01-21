package com.mas.masstreamer.utility;

public class MasConstants {


	public static final String FILE_EXTENSION = ".mp3";

	public static final int COL_TITLE 	= 0;
	public static final int COL_ARTIST 	= 1;
	public static final int COL_DATA 		= 2;
	public static final int COL_DISPLAY	= 3;
	public static final int COL_DURATION 	= 4;
	public static final int COL_MEDIA_ID 	= 5;
	
	public static final String AB_REPEAT_A = "A-  ";
	public static final String AB_REPEAT_AB = "A-B";
	
	public static final int PLAYER_NOTIFICATION_ID = 200;
	
	public static final String INTENT_MEDIA_DATA = "intent_media_data";
	public static final String INTENT_POSITION = "intent_playback_position";
	public static final String INTENT_FROM_LIST = "intent_playback_from_list";
	public static final String INTENT_DIRECTORY = "intent_playback_directory";
	public static final String INTENT_DIRECTORIES_LIST = "intent_playback_directories_list";
	
	public static final String INTENT_CAST_TIME = "intent_broadcast_elapsed_time";
	public static final String INTENT_CAST_POSITION = "intent_broadcast_current_position";
	public static final String INTENT_CAST_BLUETOOTH = "intent_broadcast_bluetooth_keycode";
	public static final String INTENT_CAST_NOTIFICATION = "intent_broadcast_notification_action";
	
	//Broadcast
	public static final String CAST_ELAPSED_TIME = "broadcast_elapsed_time";
	public static final String CAST_CURRENT_POSITION = "broadcast_current_position";
	public static final String CAST_BLUETOOTH_CONTROL = "broadcast_bluetooth_control";
	public static final String CAST_NOTIFICATION_CONTROL = "broadcast_notification_control";
	
	public static final int HANDLER_TIME_INTERVAL = 500; // 500ms
	
	public static final int PLAYER_SEEK_TIME = 10000; // 10sec
	
	//
	public static final String NOTIFICATION_BAR_CONTROL = "notification_bar_control";
	public static final String NOTIFICATION_ACTION_PLAY = "notification_play_action";
	public static final String NOTIFICATION_ACTION_NEXT = "notification_play_next";
	public static final String NOTIFICATION_ACTION_PREV = "notification_play_previous";

	
	public static class TabsName{
		public static String[] tabs = {"Directories", "Albums", "Favorites"};
	}

}
