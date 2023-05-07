package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaitRoom extends Room {
	private Map<Long, ChatRoom> idRoomMap;

	public WaitRoom() {
		super();
		idRoomMap = new ConcurrentHashMap<Long, ChatRoom>();
	}

	public Map<Long, ChatRoom> getIdRoomMap() {
		return idRoomMap;
	}

//	public void setIdRoomMap(Map<Long, ChatRoom> idRoomMap) {
//		this.idRoomMap = idRoomMap;
//	}

	@Override
	public String toString() {
		return "WaitRoom [idRoomMap=" + idRoomMap + ", " + super.toString() + "]";
	}
	public void addRoom(ChatRoom chatRoom) {
		synchronized (idRoomMap) { 					// 룸 추가, 삭제와 동시 실행 불가
			idRoomMap.put(chatRoom.getId(), chatRoom);
		}
	}
	public void removeRoom(ChatRoom chatRoom) {
		synchronized (idRoomMap) { 					// 룸 추가, 삭제와 동시 실행 불가
			idRoomMap.remove(chatRoom.getId());
		}
	}
}
