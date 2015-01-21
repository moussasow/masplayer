package com.mas.masstreamer.database;

import java.util.HashMap;

import com.mas.masstreamer.debug.MasLog;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class AudioProvider extends ContentProvider {

	private static final String TAG = "AudioProvider";
	
	private static final String PROVIDER_NAME = "com.mas.audio.player.provider.Audio";
	public 	static final String URL = "content://" + PROVIDER_NAME + "/songs";
	public 	static final Uri CONTENT_URI = Uri.parse(URL);
	private static final int SONGS 	= 1;
	private static final int SONGS_ID = 2;
	
	// for download table
	public 	static final String DL_URL = "content://" + PROVIDER_NAME + "/download";
	public 	static final Uri DOWNLOAD_URI = Uri.parse(DL_URL);
	private static final int DOWNLOAD 	= 3;
	private static final int DOWNLOAD_ID = 4;
	
	
	private DBhelper dbHelper;
	
	// projection map for a query
	private static HashMap<String, String> SONGS_PROJECTION_MAP;
	private static HashMap<String, String> DOWNLOAD_PROJECTION_MAP;
	
	private static  UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "songs", SONGS);
		uriMatcher.addURI(PROVIDER_NAME, "songs/#", SONGS_ID);
		uriMatcher.addURI(PROVIDER_NAME, "download", DOWNLOAD);
		uriMatcher.addURI(PROVIDER_NAME, "download/#", DOWNLOAD_ID);
	}
	
	// fields for database
	public static final String KEY_ID 				= "table_id"; 
	public static final String KEY_AUDIO_ID 	 	= "table_audio_id";
	public static final String KEY_AUDIO_TITLE  	= "table_audio_title";
	public static final String KEY_AUDIO_ARTIST 	= "table_audio_artist";
	public static final String KEY_AUDIO_PATH 	 	= "table_audio_path";
	public static final String KEY_AUDIO_DISPLAY	= "table_audio_display";
	public static final String KEY_AUDIO_DURATION	= "table_audio_duration";
	public static final String KEY_AUDIO_FAVORITE 	= "table_audio_favorite";
	public static final String KEY_AUDIO_PLAYBACKS	= "table_audio_number_plays";
	public static final String KEY_AUDIO_IMAGE 		= "table_audio_image";

	public static final int INDEX_AUDIO_ID 	 		= 1;
	public static final int INDEX_AUDIO_TITLE  		= 2;
	public static final int INDEX_AUDIO_ARTIST 		= 3;
	public static final int INDEX_AUDIO_PATH 		= 4;
	public static final int INDEX_AUDIO_DISPLAY		= 5;
	public static final int INDEX_AUDIO_DURATION	= 6;
	public static final int INDEX_AUDIO_FAVORITE 	= 7;
	public static final int INDEX_AUDIO_PLAYBACKS	= 8;
	public static final int INDEX_AUDIO_IMAGE 		= 9;

	// fields for DOWNLOAD database
	private static final String DOWNLOAD_TABLE_NAME   = "download_table_name";
	public static final String KEY_DL_ID 			  = "download_table_id";
	public static final String KEY_DL_SURAT_NUMBER 	  = "download_table_surat_number";
	public static final String KEY_DL_SURAT_NAME 	  = "download_table_surat_name";
	public static final String KEY_DL_SURAT_PATH 	  = "download_table_surat_path";
	public static final String KEY_DL_SURAT_DOWNLOAD  = "download_table_surat_downloaded";
	public static final String KEY_DL_SURAT_MEMORIZED = "download_table_surat_memorized";
	
	private static final String CREATE_DOWNLOAD_TABLE = 
			" CREATE TABLE " + DOWNLOAD_TABLE_NAME + "(" + 
					KEY_DL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_DL_SURAT_NUMBER + " TEXT NOT NULL, " +
					KEY_DL_SURAT_NAME + " TEXT NOT NULL, " +
					KEY_DL_SURAT_PATH + " TEXT NOT NULL, " +
					KEY_DL_SURAT_DOWNLOAD + " TEXT NOT NULL, " +
					KEY_DL_SURAT_MEMORIZED + " TEXT NOT NULL);";
	
	// database declarations
	private SQLiteDatabase mDatabase;
	private static final String DATABASE_NAME = "audio_database";
	private static final String TABLE_NAME = "audio_table";
	private static final int DATABASE_VERSION = 1;
	private static final String CREATE_TABLE = 
			" CREATE TABLE " + TABLE_NAME + "(" + 
			KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			KEY_AUDIO_ID + " TEXT NOT NULL, " +
			KEY_AUDIO_TITLE + " TEXT NOT NULL, " +
			KEY_AUDIO_ARTIST + " TEXT NOT NULL, " +
			KEY_AUDIO_PATH + " TEXT NOT NULL, " +
			KEY_AUDIO_DISPLAY + " TEXT NOT NULL, " +
			KEY_AUDIO_DURATION + " TEXT NOT NULL, " +
			KEY_AUDIO_FAVORITE + " TEXT NOT NULL, " +
			KEY_AUDIO_PLAYBACKS + " TEXT NOT NULL, " +
			KEY_AUDIO_IMAGE + " BLOB);";
	
	
	private static class DBhelper extends SQLiteOpenHelper{
		
			public DBhelper(Context context){
				super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				MasLog.log(TAG, "Database created = " + CREATE_TABLE, MasLog.V);
				db.execSQL(CREATE_TABLE);
				db.execSQL(CREATE_DOWNLOAD_TABLE);
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
				db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE_NAME);
				onCreate(db);
			}
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		String id = null;
		String whereClause = null;
		
		switch( uriMatcher.match(uri)){
		case SONGS:
			count = mDatabase.delete(TABLE_NAME, selection, selectionArgs);
			break;
			
		case SONGS_ID:
			id = uri.getLastPathSegment();
			whereClause = KEY_ID + " = " + id + (!TextUtils.isEmpty(selection) ? 
					" AND (" + selection + ')' : "");
			count = mDatabase.delete(TABLE_NAME, whereClause, selectionArgs);
			break;
				
		case DOWNLOAD:
			count = mDatabase.delete(DOWNLOAD_TABLE_NAME, selection, selectionArgs);
			break;
			
		case DOWNLOAD_ID:
			id = uri.getLastPathSegment();
			whereClause = KEY_DL_ID + " = " + id + (!TextUtils.isEmpty(selection) ? 
					" AND (" + selection + ')' : "");
			count = mDatabase.delete(DOWNLOAD_TABLE_NAME, whereClause, selectionArgs);
			break;
				
		default:
			throw new IllegalArgumentException("Unsupported URI" + uri);
					
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch( uriMatcher.match(uri)){
		case SONGS:
			return "to be checked";
			
		case SONGS_ID:
			return "to be checked";
				
		case DOWNLOAD:
			return "to be checked";
			
		case DOWNLOAD_ID:
			return "to be checked";
				
		default:
			throw new IllegalArgumentException("Unsupported URI" + uri);
					
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri newUri = null;
		switch (uriMatcher.match(uri)){
		case SONGS:
			
			long row = mDatabase.insert(TABLE_NAME, "", values);
			if( row > 0){
				newUri = ContentUris.withAppendedId(CONTENT_URI, row);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			break;
		case DOWNLOAD:
			long row1 = mDatabase.insert(DOWNLOAD_TABLE_NAME, "", values);
			if( row1 > 0){
				newUri = ContentUris.withAppendedId(DOWNLOAD_URI, row1);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
			break;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		dbHelper = new DBhelper(context);
		// permissions to be writable
		mDatabase = dbHelper.getWritableDatabase();
		if(mDatabase == null){
			return false;
		}
		else{
			return true;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		switch( uriMatcher.match(uri)){
		// maps all database column names
		case SONGS:
			builder.setTables(TABLE_NAME);
			builder.setProjectionMap(SONGS_PROJECTION_MAP);
			break;
			
		case SONGS_ID:
			builder.setTables(TABLE_NAME);
			builder.appendWhere( KEY_ID + "=" + uri.getLastPathSegment());
			break;
			
		case DOWNLOAD:
			builder.setTables(DOWNLOAD_TABLE_NAME);
			builder.setProjectionMap(DOWNLOAD_PROJECTION_MAP);
			break;
			
		case DOWNLOAD_ID:
			builder.setTables(DOWNLOAD_TABLE_NAME);
			builder.appendWhere( KEY_DL_ID + "=" + uri.getLastPathSegment());
			break;
			
			default:
				throw new IllegalArgumentException("Unknown URI" + uri);
		}
		
		if( uriMatcher.match(uri) == SONGS || uriMatcher.match(uri) == SONGS_ID){
			if( sortOrder == null || sortOrder == ""){
				sortOrder = KEY_AUDIO_TITLE;
			}
		}
		
		Cursor cursor = builder.query(mDatabase, projection, selection, selectionArgs, null, null, sortOrder);
		// register to watch a content URI for changes
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] whereArgs) {
		int count = 0;
		String whereClause = null;
		
		switch( uriMatcher.match(uri)){
		case SONGS:
			count = mDatabase.update(TABLE_NAME, values, selection, whereArgs);
			break;
			
		case SONGS_ID:
			whereClause = KEY_ID + " = " + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? 
					" AND (" + selection + ')' : "");
			count = mDatabase.update(TABLE_NAME, values, whereClause, whereArgs);
			break;
			
		case DOWNLOAD:
			count = mDatabase.update(DOWNLOAD_TABLE_NAME, values, selection, whereArgs);
			break;
			
		case DOWNLOAD_ID:
			whereClause = KEY_DL_ID + " = " + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? 
					" AND (" + selection + ')' : "");
			count = mDatabase.update(DOWNLOAD_TABLE_NAME, values, whereClause, whereArgs);
			break;
				
		default:
			throw new IllegalArgumentException("Unsupported URI" + uri);
					
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
