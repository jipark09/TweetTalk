package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import server.RoomIsFullException;

public class ChatRoom extends Room {
	private long hostId;
	private RoomInfo info;
//	private boolean isPrivate;

	public ChatRoom(long hostId, RoomInfo info) {
		super();
		this.hostId = hostId;
		this.info = info;
	}

	public long getHostId() {
		return hostId;
	}
	public void setHostId(long hostId) {
		this.hostId = hostId;
	}
	public boolean isPrivate() {
		return info.isPrivate();
	}
	public RoomInfo getInfo() {
		return info;
	}
	public void setInfo(RoomInfo info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "ChatRoom [hostId=" + hostId + ", info=" + info + "]";
	}

	@Override
	public void addUser(User user) throws RoomIsFullException {
		Map<Long, User> idUserMap = getIdUserMap();
		synchronized(idUserMap) {
			if(idUserMap.size() < getInfo().getMaxUser()) {
				idUserMap.put(user.getId(), user);
				user.setRoomId(getId());
				user.setEnterTime();
			} else {
				throw new RoomIsFullException(this);
			}
		}
	}
	@Override
	public void removeUser(User user) {
		Map<Long, User> idUserMap = getIdUserMap();
		synchronized(idUserMap) {
			long userId = user.getId();
			idUserMap.remove(userId);
			if(userId == hostId && idUserMap.size() != 0) {
				List<User> userList = new ArrayList<User>(idUserMap.values());
				userList.sort(new Comparator<User>() {
					@Override
					public int compare(User user1, User user2) {
						return user1.compareTo(user2);
					}
				});
				hostId = userList.get(0).getId();
			}
		}
	}
}
