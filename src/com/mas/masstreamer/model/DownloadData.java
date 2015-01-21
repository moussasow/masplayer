package com.mas.masstreamer.model;

public class DownloadData {

	private String name;
	private String path;
	private boolean isMemorized;
	private boolean isDownloaded;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isMemorized() {
		return isMemorized;
	}
	public void setMemorized(boolean isMemorized) {
		this.isMemorized = isMemorized;
	}
	public boolean isDownloaded() {
		return isDownloaded;
	}
	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
		
}

