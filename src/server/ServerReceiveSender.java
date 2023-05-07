package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import model.ChatRoom;
import model.Message;
import model.ReqData;
import model.ResData;
import model.Room;
import model.RoomInfo;
import model.User;
import model.UserProfile;
import model.WaitRoom;
import util.IOUtil;

public class ServerReceiveSender extends Thread {
	private static Map<Long, ObjectOutputStream> idSenderMap = new ConcurrentHashMap<Long, ObjectOutputStream>();
	// put() 메서드가 동시에 실행되어 같은 값이 중복으로 들어가는 것을 방지
	private static WaitRoom waitRoom = new WaitRoom();
	private static Map<Long, String> roomPwMap = new ConcurrentHashMap<Long, String>();

	public static final String KEY_PROFILES = "profiles";

	private Socket mySocket;
	private User myUser;
	private long myId;
	private ObjectInputStream myIn;
	private ObjectOutputStream myOut;
	private Map<Long, User> bookmarks; // removeAll();

	public ServerReceiveSender(Socket socket) {
		this.mySocket = socket;
	}

	@Override
	public void run() {
		try {
			myIn = new ObjectInputStream(mySocket.getInputStream());
			myOut = new ObjectOutputStream(mySocket.getOutputStream());

			// 로그인 (될 때까지 시도)
			login();
			// 프로토콜 받기 (연결 끊길 때까지 시도)
			keepReceiving();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myId != 0) {
				// 연결 끊긴 경우 로그아웃
				logout(myId);
			}
			IOUtil.allClose(mySocket, myIn, myOut);
		}
	}

	private void login() throws IOException, ClassNotFoundException {
		myUser = new User();
		// 로그인 요청을 연달아 보낼 경우 처리
		while (myId == 0) {
			ReqData request = (ReqData) myIn.readObject();
			if (setProfile(request.getProfile())) {
				myUser.setId();
				myId = myUser.getId();
				idSenderMap.put(myId, myOut);

				// 북마크 초기화
				bookmarks = new HashMap<Long, User>();

				// 대기실 입장
				try {
					waitRoom.addUser(myUser);
				} catch (RoomIsFullException e) {
				}
				// 방 다참 예외 발생하지 않음 (대기실 인원제한 x)

				Map<Long, User> idUserMap = waitRoom.getIdUserMap();

				// 로그인 성공 프로토콜 LOGIN_SUCCESSED = 1101
				ResData myResponse = new ResData(ResData.LOGIN_SUCCESSED, myId, waitRoom);
				// 대기실 유저리스트 갱신 프로토콜 UPDATE_WAITROOM_USERLIST = 1102
				ResData response = new ResData(ResData.UPDATE_WAITROOM_USERLIST, idUserMap, new Message(myUser, "입장"));

				// @@@ 우리는 유저리스트에 변화가 생길 때마다 기존 유저들의 자료를 갱신시킨다
				// @@@ 그러면 동기화는 필요없지 않을까? 어차피 유저 추가/삭제 때마다 또 보낼건데, 유저 추가/삭제로 동기화 오류를 걱정할 필요가 있나?
				synchronized (idUserMap) {
					boolean flag = true;
					for (Long id : idUserMap.keySet()) {
						if (flag && id == myId) {
							myOut.writeObject(myResponse);
							myOut.flush();
							myOut.reset();
							flag = false;
						} else {
							try {
								sendResponse(id, response);
							} catch (IOException e) {
							}
						}
					}
				}
			} else {
				// 로그인 실패 프로토콜 LOGIN_FAILED_DUPLICATE = 1100
				myOut.writeObject(new ResData(ResData.LOGIN_FAILED_DUPLICATE));
				myOut.flush();
				myOut.reset();
			}
		}
	}

	private void keepReceiving() throws IOException, ClassNotFoundException {
		while (true) {
			ReqData request = (ReqData) myIn.readObject();
			ResData response;
			ResData myResponse;
			Map<Long, User> idUserMap; // userList
//			Map<Long, ChatRoom> idRoomMap;
			ChatRoom chatRoom;
			RoomInfo info;
			long roomId;
			long userId;
			String pw;

			// 전송된 프로토콜 request 확인
			System.out.println(request);

			switch (request.getProtocol()) {
			case ReqData.MAKE_ROOM:
				// 방 생성
				chatRoom = new ChatRoom(myId, request.getRoomInfo());
				roomId = chatRoom.getId();

				// 방 추가
				waitRoom.addRoom(chatRoom);

				// 비밀번호 설정
				pw = request.getContent();
				if (request.getRoomInfo().isPrivate()) {
					roomPwMap.put(roomId, pw);
				}

				// 방 입장
				try {
					enterRoom(roomId);
				} catch (RoomIsFullException | RoomNotExistException e) {
				}
				// 방 다참 예외 발생하지 않음 (최소인원 2명)

				// 방 생성 프로토콜 NEW_ROOM_HOST = 1207
				myResponse = new ResData(ResData.NEW_ROOM_HOST, chatRoom);
				myOut.writeObject(myResponse);
				myOut.flush();
				myOut.reset();

				// 대기실 전체 업데이트 프로토콜 UPDATE_WAITROOM_ALL = 1202
				idUserMap = waitRoom.getIdUserMap();
				response = new ResData(ResData.UPDATE_WAITROOM_ALL, waitRoom, new Message(myUser, "퇴장"));
				synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
					synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제와 동시 실행 불가
						for (Long id : idUserMap.keySet()) {
							try {
								sendResponse(id, response);
							} catch (IOException e) {
								// logout(id);
							}
						}
					}
				}

				break;

			case ReqData.ENTER_ROOM:
				roomId = request.getId();

				// 비밀방인지 확인
				if (roomPwMap.containsKey(roomId)) {
					// 비밀번호 확인창 구현 요청 (비밀번호 입력 요청) 프로토콜
					myResponse = new ResData(ResData.REQUEST_PW, roomId);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
					break;
				}

				// 방 입장
				try {
					chatRoom = enterRoom(roomId);
					idUserMap = chatRoom.getIdUserMap();

					// 방 전체 구현 프로토콜 NEW_ROOM_ALL = 1200
					myResponse = new ResData(ResData.NEW_ROOM_ALL, chatRoom);

					// 채팅방 유저리스트 업데이트 UPDATE_ROOM_USERLIST = 1201
					response = new ResData(ResData.UPDATE_ROOM_USERLIST, chatRoom, new Message(myUser, "입장"));

					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						for (Long id : idUserMap.keySet()) {
							try {
								if (id == myId) {
									myOut.writeObject(myResponse);
									myOut.flush();
									myOut.reset();
								} else {
									sendResponse(id, response);
								}
							} catch (IOException e) {
								// logout(id);
							}
						}
					}

					// 대기실 전체 업데이트 프로토콜 UPDATE_WAITROOM_ALL = 1202
					idUserMap = waitRoom.getIdUserMap();
					response = new ResData(ResData.UPDATE_WAITROOM_ALL, waitRoom, new Message(myUser, "퇴장"));
					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제와 동시 실행 불가
							for (Long id : idUserMap.keySet()) {
								try {
									sendResponse(id, response);
								} catch (IOException e) {
									// logout(id);
								}
							}
						}
					}
				} catch (RoomNotExistException e) {
					// 방 존재하지 않음 프로토콜 ENTER_FAILED_DEAD_ROOM = 1208
					myResponse = new ResData(ResData.ENTER_FAILED_DEAD_ROOM, waitRoom);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				} catch (RoomIsFullException e) {
					// 만석이라 입장 불가 알림 ENTER_FAILED_FULL = 1206
					myResponse = new ResData(ResData.ENTER_FAILED_FULL);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				}
				break;
			case ReqData.EXIT_ROOM:
				// 방 퇴장
				chatRoom = exitRoom();

				// 대기실 전체 구현 프로토콜 NEW_WAITROOM_ALL = 1203
				myResponse = new ResData(ResData.NEW_WAITROOM_ALL, waitRoom);
				// 대기실 전체 업데이트 프로토콜 UPDATE_WAITROOM_ALL = 1202
				response = new ResData(ResData.UPDATE_WAITROOM_ALL, waitRoom, new Message(myUser, "입장"));

				idUserMap = waitRoom.getIdUserMap();
				synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
					synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제와 동시 실행 불가
						for (Long id : idUserMap.keySet()) {
							try {
								if (id == myId) {
									myOut.writeObject(myResponse);
									myOut.flush();
									myOut.reset();
								} else {
									sendResponse(id, response);
								}
							} catch (IOException e) {
								// logout(id);
							}
						}
					}
				}
				// 채팅방 유저리스트 업데이트 UPDATE_ROOM_USERLIST = 1201
				idUserMap = chatRoom.getIdUserMap();
				response = new ResData(ResData.UPDATE_ROOM_USERLIST, chatRoom, new Message(myUser, "퇴장"));
				synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
					for (Long id : idUserMap.keySet()) {
						try {
							sendResponse(id, response);
						} catch (IOException e) {
							// logout(id);
						}
					}
				}

				break;
			case ReqData.SEND_PW:
				roomId = request.getId();
				if (roomPwMap.get(roomId).equals(request.getContent())) {
					// 방 입장
					try {
						chatRoom = enterRoom(roomId);

						// 방 전체 구현 프로토콜 NEW_ROOM_ALL = 1200
						myResponse = new ResData(ResData.NEW_ROOM_ALL, chatRoom);
						// 채팅방 유저리스트 업데이트 UPDATE_ROOM_USERLIST = 1201
						response = new ResData(ResData.UPDATE_ROOM_USERLIST, chatRoom, new Message(myUser, "입장"));

						idUserMap = chatRoom.getIdUserMap();
						synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
							for (Long id : idUserMap.keySet()) {
								try {
									if (id == myId) {
										myOut.writeObject(myResponse);
										myOut.flush();
										myOut.reset();
									} else {
										sendResponse(id, response);
									}
								} catch (IOException e) {
									// logout(id);
								}
							}
						}
						// 대기실 전체 업데이트 프로토콜 UPDATE_WAITROOM_ALL = 1202
						response = new ResData(ResData.UPDATE_WAITROOM_ALL, waitRoom, new Message(myUser, "퇴장"));

						idUserMap = waitRoom.getIdUserMap();
						synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
							synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제와 동시 실행 불가
								for (Long id : idUserMap.keySet()) {
									try {
										sendResponse(id, response);
									} catch (IOException e) {
										// logout(id);
									}
								}
							}
						}
					} catch (RoomNotExistException e) {
						// 방 존재하지 않음 프로토콜 ENTER_FAILED_DEAD_ROOM = 1208
						myResponse = new ResData(ResData.ENTER_FAILED_DEAD_ROOM, waitRoom);
						myOut.writeObject(myResponse);
						myOut.flush();
						myOut.reset();
					} catch (RoomIsFullException e) {
						// 만석이라 입장 불가 알림 ENTER_FAILED_FULL = 1206
						myResponse = new ResData(ResData.ENTER_FAILED_FULL);
						myOut.writeObject(myResponse);
						myOut.flush();
						myOut.reset();
					}
				} else {
					// 비밀번호 틀려서 입장 실패 알림 ENTER_FAILED_PW = 1205
					myResponse = new ResData(ResData.ENTER_FAILED_PW);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				}
				break;
			case ReqData.CHANGE_ROOM_INFO:
				roomId = myUser.getRoomId();
				chatRoom = waitRoom.getIdRoomMap().get(roomId);
				info = request.getRoomInfo();

				try {
					if (isHost(chatRoom)) { // 방장 맞는지 확인
						synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제, 룸 리스트 여러명 전송과 동시 수행 불가
							idUserMap = chatRoom.getIdUserMap();
							synchronized (idUserMap) { // 채팅방 갱신(채팅방 인원 변경)과 동시 수행 불가
								if (info.getMaxUser() < idUserMap.size()) {
									throw new OverMaxCountException();
								}
								chatRoom.setInfo(info);
							}
						}
						pw = request.getContent();
						if (chatRoom.isPrivate()) {
							if (pw != null) {
								roomPwMap.put(roomId, pw);
							}
						} else {
							roomPwMap.remove(roomId);
						}
					}
					// 대화방 화면 갱신 UPDATE_ROOM_ALL : 1300
					response = new ResData(ResData.UPDATE_ROOM_ALL, chatRoom);

					idUserMap = chatRoom.getIdUserMap();
					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						for (Long id : idUserMap.keySet()) {
							try {
								sendResponse(id, response);
							} catch (IOException e) {
							}
						}
					}
					// 대기실 채팅방리스트 업데이트 프로토콜 UPDATE_WAITROOM_ROOMLIST = 1302
					response = new ResData(ResData.UPDATE_WAITROOM_ROOMLIST, waitRoom);

					idUserMap = waitRoom.getIdUserMap();
					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제와 동시 실행 불가
							for (Long id : idUserMap.keySet()) {
								try {
									sendResponse(id, response);
								} catch (IOException e) {
									// logout(id);
								}
							}
						}
					}
				} catch (OverMaxCountException e) {
					// 변경하려는 방 최대 인원이 현재 인원보다 작아서 변경 실패 CHANGE_ROOM_FAILED_MAX_USER = 1306
					myResponse = new ResData(ResData.CHANGE_ROOM_FAILED_MAX_USER);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				}
				break;
			case ReqData.CHANGE_ROOM_HOST:
				userId = request.getId();
				chatRoom = waitRoom.getIdRoomMap().get(myUser.getRoomId());
				if (isHost(chatRoom)) { // 방장 맞는지 확인
					synchronized (chatRoom.getIdUserMap()) { // 채팅방 갱신과 동시 수행 불가
						chatRoom.setHostId(userId);
					}
				}
				// 대화방 화면 갱신(방장 변경) UPDATE_ROOM_HOST : 1301
				response = new ResData(ResData.UPDATE_ROOM_HOST, chatRoom);

				idUserMap = chatRoom.getIdUserMap();
				synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
					for (Long id : idUserMap.keySet()) {
						try {
							sendResponse(id, response);
						} catch (IOException e) {
						}
					}
				}
				break;
			case ReqData.INVITE_USER:
				userId = request.getId();
				roomId = myUser.getRoomId();
				// 초대 확인/거부 물어보기 SHOW_INVITE_MSG = 1303
				response = new ResData(ResData.SHOW_INVITE_MSG, roomId,
						new Message(myUser, waitRoom.getIdRoomMap().get(roomId).getInfo().getTitle()));
				// @@@ 해당 유저가 대기실에 없음 = 유저 초대창 갱신 UPDATE_INVITE_USER_ALL = 1601
				myResponse = new ResData(ResData.NEW_INVITE_USER_ALL, waitRoom.getIdUserMap());

				idUserMap = waitRoom.getIdUserMap();
				try {
					synchronized (idUserMap) {
						boolean isOk;
						synchronized (getUser(userId)) { // 유저 위치 확인은 유저 이동과 동시에 일어날 수 없음
							isOk = idUserMap.containsKey(userId);
						}
						if (isOk) {
							sendResponse(userId, response);
						} else {
							myOut.writeObject(myResponse);
							myOut.flush();
							myOut.reset();
						}
					}
				} catch (IOException e) {
					// 해당 유저가 존재하지 않음 프로토콜 SYSTEM_INFORM_NO_USER_ERROR = 1103
					myResponse = new ResData(ResData.SYSTEM_INFORM_NO_USER_ERROR);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();

					logout(userId);
				}

				break;
			case ReqData.KICK_USER:
				userId = request.getId();

				chatRoom = waitRoom.getIdRoomMap().get(myUser.getRoomId());
				if (isHost(chatRoom)) { // 방장 맞는지 확인
					User target = getUser(userId);
					// 방에서 퇴장, 대기실 입장
					exitRoom(target);

					// 채팅방 유저리스트 업데이트 UPDATE_ROOM_USERLIST = 1201
					response = new ResData(ResData.UPDATE_ROOM_USERLIST, chatRoom, new Message(target, "퇴장"));

					idUserMap = chatRoom.getIdUserMap();
					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						for (Long id : idUserMap.keySet()) {
							try {
								sendResponse(id, response);
							} catch (IOException e) {
								// logout(id);
							}
						}
					}

					// 대기실 전체 구현 (강퇴) 프로토콜 NEW_WAITROOM_ALL_KICKED = 1305
					myResponse = new ResData(ResData.NEW_WAITROOM_ALL_KICKED, waitRoom);
					// 대기실 전체 갱신 프로토콜 UPDATE_WAITROOM_ALL = 1202
					response = new ResData(ResData.UPDATE_WAITROOM_ALL, waitRoom, new Message(target, "입장"));

					idUserMap = waitRoom.getIdUserMap();
					synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제, 룸 리스트 여러명 전송과 동시 수행 불가
						synchronized (idUserMap) {
							boolean flag = true;
							for (Long id : idUserMap.keySet()) {
								try {
									if (flag && id == userId) {
										sendResponse(userId, myResponse);
										flag = false;
									} else {
										sendResponse(id, response);
									}
								} catch (IOException e) {
								}
							}
						}
					}
				} else {
					// 채팅방 갱신 (방장 조정)
				}
				break;
			case ReqData.THANKS:
				roomId = request.getId();

				// 방 입장
				try {
					chatRoom = enterRoom(roomId);

					// 방 전체 구현 프로토콜 NEW_ROOM_ALL = 1200
					myResponse = new ResData(ResData.NEW_ROOM_ALL, chatRoom);
					// 채팅방 유저리스트 업데이트 UPDATE_ROOM_USERLIST = 1201
					response = new ResData(ResData.UPDATE_ROOM_USERLIST, chatRoom, new Message(myUser, "입장"));

					idUserMap = chatRoom.getIdUserMap();
					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						for (Long id : idUserMap.keySet()) {
							try {
								if (id == myId) {
									myOut.writeObject(myResponse);
									myOut.flush();
									myOut.reset();
								} else {
									sendResponse(id, response);
								}
							} catch (IOException e) {
								// logout(id);
							}
						}
					}
					// 대기실 전체 업데이트 프로토콜 UPDATE_WAITROOM_ALL = 1202
					response = new ResData(ResData.UPDATE_WAITROOM_ALL, waitRoom, new Message(myUser, "퇴장"));

					idUserMap = waitRoom.getIdUserMap();
					synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
						synchronized (waitRoom.getIdRoomMap()) { // 룸 추가, 삭제와 동시 실행 불가
							for (Long id : idUserMap.keySet()) {
								try {
									sendResponse(id, response);
								} catch (IOException e) {
									// logout(id);
								}
							}
						}
					}
				} catch (RoomNotExistException e) {
					// 방 존재하지 않음 프로토콜 ENTER_FAILED_DEAD_ROOM = 1208
					myResponse = new ResData(ResData.ENTER_FAILED_DEAD_ROOM, waitRoom);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				} catch (RoomIsFullException e) {
					// 만석이라 입장 불가 알림 ENTER_FAILED_FULL = 1206
					myResponse = new ResData(ResData.ENTER_FAILED_FULL);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				}
				break;
			case ReqData.NO_THANKS:
				userId = request.getId();
				// 초대 거절 알리기 INVITE_FAILED = 1304
				response = new ResData(ResData.INVITE_FAILED, myUser.getProfile().getNickname());
				sendResponse(userId, response);
				break;
			case ReqData.SEND_MSG:
				// 전체 메시지 보내기 BROADCAST = 1400
				response = new ResData(ResData.BROADCAST, new Message(myUser, request.getContent()));
				Room room = waitRoom.getIdRoomMap().get(myUser.getRoomId());
				if (room == null) {
					idUserMap = waitRoom.getIdUserMap();
				} else {
					idUserMap = room.getIdUserMap();
				}
				// synchronized 걸어야 하나? 대기실에서 보낸 메시지를 채팅방 안에서 받을까봐...
				synchronized (idUserMap) {
					for (Long id : idUserMap.keySet()) {
						sendResponse(id, response);
					}
				}
				break;
			case ReqData.SEND_MSG_TO:
				long toId = 0;
				String toName = null;

				try {
					// 귓속말 보내기 프로토콜 WHISPER = 1401
					response = new ResData(ResData.WHISPER, new Message(myUser, request.getContent()));
					toId = request.getId();
					toName = getUser(toId).getProfile().getNickname();
					sendResponse(toId, response);

					// 귓속말 보내기 성공 프로토콜 WHISPER_SUCCESSED = 1402
					myResponse = new ResData(ResData.WHISPER_SUCCESSED, toName,
							new Message(myUser, request.getContent()));
				} catch (IOException e) {
					// 유저 없음 에러메시지 표시 SYSTEM_INFORM_NO_USER_ERROR = 1103
					myResponse = new ResData(ResData.SYSTEM_INFORM_NO_USER_ERROR, toName,
							new Message(myUser, request.getContent()));
					logout(toId);
				}
				myOut.writeObject(myResponse);
				myOut.flush();
				myOut.reset();
				break;
			case ReqData.CHANGE_PROFILE:
				if (setProfile(request.getProfile())) {
					idUserMap = waitRoom.getIdUserMap();
					// 프로필 + 대기실 유저리스트 업데이트 프로토콜 UPDATE_PROFILE_USERLIST = 1501
					response = new ResData(ResData.UPDATE_PROFILE_USERLIST, idUserMap);
					synchronized (idUserMap) {
						for (Long id : idUserMap.keySet()) {
							try {
								sendResponse(id, response);
							} catch (IOException e) {
							}
						}
					}
				} else {
					// 프로필 업데이트 실패 (닉네임 중복) 프로토콜 UPDATE_PROFILE_FAILED_DUPLICATE = 1502
					myResponse = new ResData(ResData.UPDATE_PROFILE_FAILED_DUPLICATE);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();
				}
				break;
			case ReqData.SHOW_INVITE_USER:
				// 유저 초대창 생성 SHOW_INVITE_USER = 1600
				myResponse = new ResData(ResData.NEW_INVITE_USER_ALL, waitRoom.getIdUserMap());
				myOut.writeObject(myResponse);
				myOut.flush();
				myOut.reset();
				break;
			case ReqData.SHOW_BOOKMARKS:
				// 로그아웃한 유저 삭제
				bookmarks.keySet().retainAll(idSenderMap.keySet());

				// 유저 즐겨찾기창 생성 NEW_BOOKMARKS_ALL = 1602
				myResponse = new ResData(ResData.NEW_BOOKMARKS_ALL, waitRoom.getIdUserMap(), bookmarks);
				myOut.writeObject(myResponse);
				myOut.flush();
				myOut.reset();
				break;
			case ReqData.ADD_BOOKMARK:
				userId = request.getId();
				try {
					sendResponse(userId, new ResData(ResData.NETWORK_CHECK));
					bookmarks.put(userId, getUser(userId));
				} catch (IOException e) {
					// 해당 유저가 존재하지 않음 프로토콜 SYSTEM_INFORM_NO_USER_ERROR = 1103
					myResponse = new ResData(ResData.SYSTEM_INFORM_NO_USER_ERROR);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();

					logout(userId);
				}
				break;
			case ReqData.REMOVE_BOOKMARK:
				// 로그아웃한 유저 삭제
				bookmarks.keySet().retainAll(idSenderMap.keySet());
				bookmarks.remove(request.getId());

				// 유저 즐겨찾기창 갱신 UPDATE_BOOKMARKS_ALL = 1603
				myResponse = new ResData(ResData.UPDATE_BOOKMARKS_ALL, waitRoom.getIdUserMap(), bookmarks);
				myOut.writeObject(myResponse);
				myOut.flush();
				myOut.reset();
				break;
			case ReqData.INVITE_BOOKMARK_USER:
				userId = request.getId();
				roomId = myUser.getRoomId();
				// 초대 확인/거부 물어보기 SHOW_INVITE_MSG = 1303
				response = new ResData(ResData.SHOW_INVITE_MSG, roomId,
						new Message(myUser, waitRoom.getIdRoomMap().get(roomId).getInfo().getTitle()));
				// @@@ 해당 유저가 대기실에 없음 = 즐겨찾기창 갱신 = 1603
				myResponse = new ResData(ResData.UPDATE_BOOKMARKS_ALL, waitRoom.getIdUserMap(), bookmarks);

				idUserMap = waitRoom.getIdUserMap();
				try {
					synchronized (idUserMap) {
						boolean isOk;
						synchronized (getUser(userId)) {
							isOk = idUserMap.containsKey(userId);
						}
						if (isOk) {
							sendResponse(userId, response);
						} else {
							myOut.writeObject(myResponse);
							myOut.flush();
							myOut.reset();
						}
					}
				} catch (IOException e) {
					// 해당 유저가 존재하지 않음 프로토콜 SYSTEM_INFORM_NO_USER_ERROR = 1103
					myResponse = new ResData(ResData.SYSTEM_INFORM_NO_USER_ERROR);
					myOut.writeObject(myResponse);
					myOut.flush();
					myOut.reset();

					logout(userId);
				}

				break;
			}
		}
	}

	private void logout(long userId) {
		User user = getUser(userId);
		long roomId = user.getRoomId();

		Map<Long, User> idUserMap;
		ResData response;
		Message msg = new Message(user, "퇴장");

		// 있는 방에서 퇴장
		if (roomId == waitRoom.getId()) {
			// 대기실에서 종료했을 경우
			waitRoom.removeUser(user);
			idUserMap = waitRoom.getIdUserMap();
			response = new ResData(ResData.UPDATE_WAITROOM_USERLIST, idUserMap, msg);
		} else {
			// 그 외 장소에서 종료
			ChatRoom chatRoom = waitRoom.getIdRoomMap().get(roomId);
			idUserMap = chatRoom.getIdUserMap();

			synchronized (idUserMap) {
				chatRoom.removeUser(user);
				if (idUserMap.size() == 0) {
					waitRoom.removeRoom(chatRoom);
				}
			}
			response = new ResData(ResData.UPDATE_ROOM_USERLIST, chatRoom, msg);
		}
		// 있던 방 유저리스트 업데이트
		synchronized (idUserMap) {
			for (Long otherId : idUserMap.keySet()) {
				try {
					sendResponse(otherId, response);
				} catch (IOException e) {
					// logout(id);
				}
			}
		}
		// 유저 정보 삭제
		idSenderMap.remove(userId);
	}

	private void sendResponse(long id, ResData response) throws IOException {
		ObjectOutputStream out = idSenderMap.get(id);
		out.writeObject(response);
		out.flush();
		out.reset();
	}

	// @@ 자주 쓰이는 프로토콜 관련 메서드
	public void enterFailedFull() throws IOException {
		// 만석이라 입장 불가 알림 ENTER_FAILED_FULL = 1206
		ResData myResponse = new ResData(ResData.ENTER_FAILED_FULL);
		myOut.writeObject(myResponse);
		myOut.flush();
		myOut.reset();
	}

	// 닉네임 설정[변경] 메서드 - 성공하면 true, 실패하면 false
	private boolean setProfile(UserProfile profile) {
		UserProfile myProfile = myUser.getProfile();
		if (myProfile != null && myProfile.equals(profile)) {
			myUser.setProfile(profile);
			return true;
		}
		Map<Long, User> users = new HashMap<Long, User>(200);
		Map<Long, User> idUserMap = waitRoom.getIdUserMap();
		synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
			users.putAll(idUserMap);
			for (ChatRoom chatRoom : waitRoom.getIdRoomMap().values()) {
				users.putAll(chatRoom.getIdUserMap());
			}
		}
		String nickname = profile.getNickname();
		synchronized (KEY_PROFILES) { // 유저 프로필 중복검사와 변경 <- 동시 실행 불가
			for (User user : users.values()) {
				if (nickname.equals(user.getProfile().getNickname())) {
					return false;
				}
			}
			myUser.setProfile(profile);
			return true;
		}
	}

	// id로 유저 찾아오기
	private User getUser(long id) {
		Map<Long, User> users = new HashMap<Long, User>(200);
		Map<Long, User> idUserMap = waitRoom.getIdUserMap();
		synchronized (idUserMap) { // 유저 이동과 동시 실행 불가
			users.putAll(idUserMap);
			for (ChatRoom chatRoom : waitRoom.getIdRoomMap().values()) {
				users.putAll(chatRoom.getIdUserMap());
			}
		}
		User user = users.get(id);
		return user;
	}

	private boolean isHost(ChatRoom chatRoom) {
		return chatRoom.getHostId() == myId;
	}

	private ChatRoom enterRoom(long roomId) throws RoomIsFullException, RoomNotExistException {
		ChatRoom chatRoom = waitRoom.getIdRoomMap().get(roomId);
		if (chatRoom == null) {
			throw new RoomNotExistException();
		}	
		boolean flag = true;
		synchronized (myUser) { // 유저 위치 변경 및 조회는 동시실행 불가
			if (!waitRoom.getIdUserMap().containsKey(myId)) {
//				throw new InvalidMovementException();
				flag = false;
			}
		}
		if (flag) {
			synchronized (myUser) { // 유저 위치 변경 및 조회는 동시실행 불가
				Map<Long, User> idUserMap = chatRoom.getIdUserMap();
				synchronized (idUserMap) {
					if (!waitRoom.getIdRoomMap().containsKey(roomId)) {
						throw new RoomNotExistException();
					}
					chatRoom.addUser(myUser);
				}
				waitRoom.removeUser(myUser);
			}
		}
		return chatRoom;
	}

	private ChatRoom exitRoom(User user) {
		ChatRoom chatRoom = waitRoom.getIdRoomMap().get(user.getRoomId());
		// @@@ 중복 나가기 실행도 일어날 수 있나?? 강퇴 버튼 켜두고 유저 나간 사이에 또 누르기?
		boolean flag = true;
		synchronized (user) { // 유저 위치 변경 및 조회는 동시실행 불가
			if (waitRoom.getIdUserMap().containsKey(user.getId())) {
//				throw new InvalidMovementException();
				flag = false;
			}
		}
		if (flag) {
			synchronized (user) { // 유저 위치 변경 및 조회는 동시실행 불가
				try {
					waitRoom.addUser(user);
				} catch (RoomIsFullException e) {
				}
				Map<Long, User> idUserMap = chatRoom.getIdUserMap();
				synchronized (idUserMap) {
					chatRoom.removeUser(user);
					if (idUserMap.size() == 0) {
						waitRoom.getIdRoomMap().remove(chatRoom.getId());
					}
				}
			}
		}
		return chatRoom;

	}

	private ChatRoom exitRoom() {
		return exitRoom(myUser);
	}
}
