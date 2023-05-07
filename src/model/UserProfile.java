package model;

import java.io.Serializable;

public class UserProfile implements Serializable {
	private String nickname; 
	private int imgNum;
	public UserProfile(String nickname, int imgNum) {
		this.nickname = nickname;
		this.imgNum = imgNum;
	}
	public String getNickname() {
		return nickname;
	}
//	public void setNickname(String nickname) {
//		this.nickname = nickname;
//	}
	public int getImgNum() {
		return imgNum;
	}
//	public void setImgNum(int imgNum) {
//		this.imgNum = imgNum;
//	}
	@Override
	public String toString() {
		return "UserProfile [nickname=" + nickname + ", imgNum=" + imgNum + "]";
	}
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof UserProfile)) {
			return false;
		}
		UserProfile temp = (UserProfile) o;
		return nickname.equals(temp.getNickname());
	}
}
