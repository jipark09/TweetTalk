package model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ResData implements Serializable {
	public static final int LOGIN_FAILED_DUPLICATE = 1100;
	public static final int LOGIN_SUCCESSED = 1101;
	public static final int UPDATE_WAITROOM_USERLIST = 1102;
	public static final int SYSTEM_INFORM_NO_USER_ERROR = 1103;
	public static final int NETWORK_CHECK = 1104;

	public static final int NEW_ROOM_ALL = 1200;
	public static final int UPDATE_ROOM_USERLIST = 1201;
	public static final int UPDATE_WAITROOM_ALL = 1202;
	public static final int NEW_WAITROOM_ALL = 1203;
	public static final int REQUEST_PW = 1204;
	public static final int ENTER_FAILED_PW = 1205;
	public static final int ENTER_FAILED_FULL = 1206;
	public static final int NEW_ROOM_HOST = 1207;
	public static final int ENTER_FAILED_DEAD_ROOM = 1208;

	public static final int UPDATE_ROOM_ALL = 1300;
	public static final int UPDATE_ROOM_HOST = 1301;
	public static final int UPDATE_WAITROOM_ROOMLIST = 1302;
	public static final int SHOW_INVITE_MSG = 1303;
	public static final int INVITE_FAILED = 1304;
	public static final int NEW_WAITROOM_ALL_KICKED = 1305;
	public static final int CHANGE_ROOM_FAILED_MAX_USER = 1306;

	public static final int BROADCAST = 1400;
	public static final int WHISPER = 1401;
	public static final int WHISPER_SUCCESSED = 1402;
//	public static final int WHISPER_FAILED = 1403;

	public static final int UPDATE_PROFILE_ALL = 1500;
	public static final int UPDATE_PROFILE_USERLIST = 1501;
	public static final int UPDATE_PROFILE_FAILED_DUPLICATE = 1502;

	public static final int NEW_INVITE_USER_ALL = 1600;
	public static final int UPDATE_INVITE_USER_ALL = 1601;
	public static final int NEW_BOOKMARKS_ALL = 1602;
	public static final int UPDATE_BOOKMARKS_ALL = 1603;
	public static final int BOOKMARK_ADD_FAILED_DUPLICATED = 1604;
	
	private int protocol;
	private long id;
	private String content;

	private WaitRoom waitRoom;
	private ChatRoom chatRoom;
	private Message message;
	private Map<Long, User> idUserMap;
	private Map<Long, User> bookmarks;

	public ResData(int protocol) {
		setProtocol(protocol);
	}
	public ResData(int protocol, String content) {
		setProtocol(protocol);
		setContent(content);
	}
	public ResData(int protocol, Message message) {
		setProtocol(protocol);
		setMessage(message);
	}
	public ResData(int protocol, ChatRoom chatRoom) {
		setProtocol(protocol);
		setChatRoom(chatRoom);
	}
	public ResData(int protocol, WaitRoom waitRoom) {
		setProtocol(protocol);
		setWaitRoom(waitRoom);
	}
	public ResData(int protocol, long id) {
		setProtocol(protocol);
		setId(id);
	}
	public ResData(int protocol, Map<Long, User> idUserMap) {
		setProtocol(protocol);
		setIdUserMap(idUserMap);
	}
	public ResData(int protocol, String content, Message message) {
		setProtocol(protocol);
		setContent(content);
		setMessage(message);
	}
	public ResData(int protocol, long id, WaitRoom waitRoom) {
		setProtocol(protocol);
		setId(id);
		setWaitRoom(waitRoom);
	}
	public ResData(int protocol, long id, Message message) {
		setProtocol(protocol);
		setId(id);
		setMessage(message);
	}
	public ResData(int protocol, long id, Map<Long, User> idUserMap) {
		setProtocol(protocol);
		setId(id);
		setIdUserMap(idUserMap);
	}
	public ResData(int protocol, ChatRoom chatRoom, Message message) {
		setProtocol(protocol);
		setChatRoom(chatRoom);
		setMessage(message);
	}
	public ResData(int protocol, WaitRoom waitRoom, Message message) {
		setProtocol(protocol);
		setWaitRoom(waitRoom);
		setMessage(message);
	}
	public ResData(int protocol, Map<Long, User> idUserMap, Message message) {
		setProtocol(protocol);
		setIdUserMap(idUserMap);
		setMessage(message);
	}
	public ResData(int protocol, Map<Long, User> idUserMap, Map<Long, User> bookmarks) {
		setProtocol(protocol);
		setIdUserMap(idUserMap);
		setBookmarks(bookmarks);
		setMessage(message);
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public WaitRoom getWaitRoom() {
		return waitRoom;
	}

	public void setWaitRoom(WaitRoom waitRoom) {
		this.waitRoom = waitRoom;
	}

	public ChatRoom getChatRoom() {
		return chatRoom;
	}

	public void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Map<Long, User> getIdUserMap() {
		return idUserMap;
	}

	public void setIdUserMap(Map<Long, User> idUserMap) {
		this.idUserMap = idUserMap;
	}
	
	public Map<Long, User> getBookmarks() {
		return bookmarks;
	}
	public void setBookmarks(Map<Long, User> bookmarks) {
		this.bookmarks = bookmarks;
	}
	
	@Override
	public String toString() {
		return "ResData [protocol=" + protocol + ", id=" + id + ", content=" + content + ", waitRoom=" + waitRoom
				+ ", chatRoom=" + chatRoom + ", message=" + message + ", idUserMap=" + idUserMap + ", bookmarks="
				+ bookmarks + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ResData)) {
			return false;
		}
		ResData data = (ResData) o;
		return protocol == data.getProtocol();
	}
}
