package server;

import model.ChatRoom;

public class RoomIsFullException extends Exception {
	public RoomIsFullException(ChatRoom room) {
		super(room.getInfo().getTitle() + " 방에 남은 자리가 없습니다.");
	}
}
