package com.mas.masstreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaData implements Parcelable{
	private String title;
	private String artist;
	private String data;
	private String display;
	private String duration;
	private String id;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getPath() {
		return data;
	}
	public void setPath(String data) {
		this.data = data;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	// Parcel
	public static final Parcelable.Creator<MediaData> CREATOR = new Creator<MediaData>() {
		
		@Override
		public MediaData[] newArray(int size) {
			return new MediaData[size];
		}
		
		@Override
		public MediaData createFromParcel(Parcel source) {
			MediaData media = new MediaData();
			media.title = source.readString();
			media.artist = source.readString();
			media.data = source.readString();
			media.display = source.readString();
			media.duration = source.readString();
			media.id = source.readString();
			return media;
		}
	};
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(artist);
		dest.writeString(data);
		dest.writeString(display);
		dest.writeString(duration);		
		dest.writeString(id);		
	}

	
	
}
