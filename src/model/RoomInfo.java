package model;

import java.io.Serializable;

public class RoomInfo implements Serializable {
	private String title;
	private int colorNum;
	private int maxUser;
	private boolean isPrivate;

	public RoomInfo(String title, int colorNum, int maxUser, boolean isPrivate) {
		this.title = title;
		this.colorNum = colorNum;
		this.maxUser = maxUser;
		this.isPrivate = isPrivate;
	}
	public String getTitle() {
		return title;
	}
	//	public void setTitle(String title) {
//		this.title = title;
//	}
	public int getColorNum() {
		return colorNum;
	}
	//	public void setColorNum(int colorNum) {
//		this.colorNum = colorNum;
//	}
	public int getMaxUser() {
		return maxUser;
	}
	//	public void setMaxUser(int maxUser) {
//		this.maxUser = maxUser;
//	}
	public boolean isPrivate() {
		return isPrivate;
	}
	
	@Override
	public String toString() {
		return "RoomInfo [title=" + title + ", colorNum=" + colorNum + ", maxUser=" + maxUser + ", isPrivate="
				+ isPrivate + "]";
	}
}
