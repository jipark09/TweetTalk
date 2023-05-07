package model;

import java.io.Serializable;

public class ReqData implements Serializable {
	public static final int LOGIN = 100;
	public static final int LOGOUT = 101;

	public static final int MAKE_ROOM = 200;
	public static final int ENTER_ROOM = 201;
	public static final int EXIT_ROOM = 202;
	public static final int SEND_PW = 203;

	public static final int CHANGE_ROOM_INFO = 300;
	public static final int CHANGE_ROOM_HOST = 301;
	public static final int INVITE_USER = 302;
	public static final int KICK_USER = 303;
	public static final int THANKS = 304;
	public static final int NO_THANKS = 305;

	public static final int SEND_MSG = 400;
	public static final int SEND_MSG_TO = 401;

	public static final int CHANGE_PROFILE = 500;

	public static final int SHOW_INVITE_USER = 600;
//	public static final int INVITE_USER = 601;
	public static final int SHOW_BOOKMARKS = 601;
	public static final int ADD_BOOKMARK = 602;
	public static final int REMOVE_BOOKMARK = 603;
	public static final int INVITE_BOOKMARK_USER = 604;

	private int protocol;
	private long id;
	private String content;
	private UserProfile profile;
	private RoomInfo roomInfo;

	public ReqData(int protocol) {
		setProtocol(protocol);
	}
	public ReqData(int protocol, long id) {
		setProtocol(protocol);
		setId(id);
	}
	public ReqData(int protocol, String content) {
		setProtocol(protocol);
		setContent(content);
	}
	public ReqData(int protocol, long id, String content) {
		setProtocol(protocol);
		setId(id);
		setContent(content);
	}
	public ReqData(int protocol, UserProfile profile) {
		setProtocol(protocol);
		setProfile(profile);
	}
	public ReqData(int protocol, RoomInfo roomInfo, String content) {
		setProtocol(protocol);
		setRoomInfo(roomInfo);
		setContent(content);
	}
//	public ReqData(int protocol, RoomInfo roomInfo, String content, long id) {
//		setProtocol(protocol);
//		setRoomInfo(roomInfo);
//		setContent(content);
//		setId(id);
//	}

	public int getProtocol() {
		return protocol;
	}

	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public UserProfile getProfile() {
		return profile;
	}

	public RoomInfo getRoomInfo() {
		return roomInfo;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}

	public void setRoomInfo(RoomInfo roomInfo) {
		this.roomInfo = roomInfo;
	}

	@Override
	public String toString() {
		return "ReqData [protocol=" + protocol + ", id=" + id + ", content=" + content
				+ ", profile=" + profile + ", roomInfo=" + roomInfo + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ReqData)) {
			return false;
		}
		ReqData data = (ReqData) o;
		return protocol == data.getProtocol();
	}
}
