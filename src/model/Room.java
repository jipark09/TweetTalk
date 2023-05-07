package model;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import server.RoomIsFullException;

public abstract class Room implements Serializable, Comparable<Room> {
	private long id;
	private Map<Long, User> idUserMap;

	public Room() {
		id = System.nanoTime();
		idUserMap = new ConcurrentHashMap<Long, User>(); // add, remove 락 <- 기준 idUserMap
	}
	public long getId() {
		return id;
	}
	public Map<Long, User> getIdUserMap() {
		return idUserMap;
	}
//	public void setId(long id) {
//		this.id = id;
//	}
//	public void setIdUserMap(Map<Long, User> idUserMap) {
//		this.idUserMap = idUserMap;
//	}

	@Override
	public String toString() {
		return "Room [id=" + id + ", idUserMap=" + idUserMap + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Room)) {
			return false;
		}
		Room temp = (Room) o;
		return id == temp.getId();
	}
	public void addUser(User user) throws RoomIsFullException {
		synchronized(idUserMap) {
			idUserMap.put(user.getId(), user);
			user.setRoomId(getId());
			user.setEnterTime();
		}
	}
	public void removeUser(User user) {
		synchronized(idUserMap) {
			idUserMap.remove(user.getId());
		}
	}

	@Override
	public int compareTo(Room other) {
		long otherId = other.getId();
		if(id == otherId) {
			return 0;
		}
		return id > otherId ? 1 : -1;
	}
}
