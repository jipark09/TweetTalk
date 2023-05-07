package model;

import java.io.Serializable;

public class User implements Serializable, Comparable<User> {
	private long id;
	private long roomId;
	private long enterTime;
	private UserProfile profile;
	public User() {
//		id = System.nanoTime();
	}
	public long getId() {
		return id;
	}
	public void setId() {
		id = System.nanoTime();
//		this.id = id;
	}
	public synchronized long getRoomId() {
		return roomId;
	}
	public synchronized void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	public UserProfile getProfile() {
		return profile;
	}
	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}
	public long getEnterTime() {
		return enterTime;
	}
	public void setEnterTime() {
		enterTime = System.currentTimeMillis();
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", roomId=" + roomId + ", profile=" + profile + "]";
	}
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof User)) {
			return false;
		}
		User temp = (User) o;
		return id == temp.getId();
	}
	@Override
	public int compareTo(User other) {
		long otherTime = other.getEnterTime();
		if(enterTime == otherTime) {
			return 0;
		}
		return enterTime > otherTime ? 1 : -1;
	}
}
