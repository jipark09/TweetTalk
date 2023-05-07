package client;

import model.*;
import ui.*;
import util.IOUtil;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 서버에서 전송 온 데이터 읽기
public class ClientReceiver extends Thread {
    private Talkable nowRoom;
    private ChatRoom chatRoom;

    private LoginForm loginForm;
    private WaitRoomForm waitRoomForm;
    private ChatRoomForm chatRoomForm;
    private UserListForm userListForm;

    private Socket socket;
    private ResData resData;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private long myId;

    private User myUser;

    public ClientReceiver(LoginForm loginForm, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.loginForm = loginForm;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {

            while (true) {
                listener();
            }

        } catch (IOException e) {
            allDispose(waitRoomForm, chatRoomForm);
            loginForm.setVisible(true);
            loginForm.changeErrorPanel("네트워크 오류");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtil.allClose(in, out, socket);
        }
    }

    public void allDispose(JFrame ... frames) {
        for(JFrame frame : frames) {
            if(frame != null) {
                frame.dispose();
            }
        }
    }

    // 서버로부터 온 응답 받고 처리
    private void listener() throws ClassNotFoundException, IOException {
        resData = (ResData) in.readObject();
        System.out.println(resData);

        long roomId;

        switch (resData.getProtocol()) {

            // 1101 로그인 성공 => 대기실 목록 추가
            case ResData.LOGIN_SUCCESSED:
                WaitRoom waitRoom = resData.getWaitRoom();
                Map<Long, ChatRoom> idRoomMap = waitRoom.getIdRoomMap();
                Map<Long, User> idUserMap = waitRoom.getIdUserMap();

                myId = resData.getId(); // resData가 아이디를 주면 내 자신인지 다른사람인지 구분가능 -> User

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        waitRoomForm = new WaitRoomForm(
                                out,
                                getUser(idUserMap),
                                socket,
                                getRoomList(idRoomMap)
                        );
                        waitRoomForm.updateUserList(getUserList(idUserMap));
                        nowRoom = waitRoomForm;
                        loginForm.dispose();
                    }
                });
                break;

            // 1100 로그인 실패: 닉네임 중복
            case ResData.LOGIN_FAILED_DUPLICATE:
                duplicateName(loginForm);
                break;

            // 1102 유저 추가: 대기실 화면 갱신(대기실 인원목록) -> 기존 클라이언트 in 대기실
            case ResData.UPDATE_WAITROOM_USERLIST:
                idUserMap = resData.getIdUserMap();
                Message message = resData.getMessage();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	nowRoom.infoMessage(message);
                        waitRoomForm.updateUserList(getUserList(idUserMap));
                    }
                });
                break;

            // 1103 탈퇴한 유저 귓속말 실패
            case ResData.SYSTEM_INFORM_NO_USER_ERROR:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                                null,
                                "없는 유저 입니다."
                        );
                    }
                });
                break;

            // 1200 챗룸 입장 클라이언트 채팅방 화면 구현
            case ResData.NEW_ROOM_ALL:
                chatRoom = resData.getChatRoom();
                openChatRoom(chatRoom, myUser, out);
                break;

            // 1201 기존 클라이언트 채팅방 화면 갱신 (챗룸 유저리스트 & 새유저 입장 메세지)
            case ResData.UPDATE_ROOM_USERLIST:
                chatRoom = resData.getChatRoom();
                message = resData.getMessage();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	nowRoom.infoMessage(message);
                        updateChatRoom(chatRoom);
                    }
                });
                break;

            // 1202 대기실 화면 갱신 -> 기존 클라이언트
            case ResData.UPDATE_WAITROOM_ALL:
                message = resData.getMessage();
                waitRoom = resData.getWaitRoom();
                idUserMap =  waitRoom.getIdUserMap();
                idRoomMap = waitRoom.getIdRoomMap();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	nowRoom.infoMessage(message);
                        updateWaitRoomList(idUserMap, idRoomMap);
                    }
                });
                break;

            // 1203 대기실 화면 구현 -> 대기실에 진입하는 클라이언트
            case ResData.NEW_WAITROOM_ALL:
                waitRoom = resData.getWaitRoom();
                idUserMap = waitRoom.getIdUserMap();
                idRoomMap = waitRoom.getIdRoomMap();

                System.out.println("내 유저정보 : " + getUser(idUserMap));
                System.out.println(myId);
                System.out.println("대기실 유저리스트 : " + idUserMap);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        waitRoomForm = new WaitRoomForm(
                                out,
                                getUser(idUserMap),
                                socket,
                                getRoomList(idRoomMap)
                        );
                        waitRoomForm.updateUserList(getUserList(idUserMap));
                        nowRoom = waitRoomForm;
                        chatRoomForm.dispose();
                        waitRoomForm.setVisible(true);
                    }
                });
                break;

            // 1204 비밀번호 확인창 구현
            case ResData.REQUEST_PW:
                roomId = resData.getId();
                String pwText = JOptionPane.showInputDialog("방 비밀번호를 입력해 주세요:");
                if (pwText != null && !pwText.isEmpty()) {
                    ReqData reqData = new ReqData(
                            ReqData.SEND_PW,
                            roomId,
                            pwText
                    );
                    out.writeObject(reqData);
                    out.flush();
                    out.reset();

                } else {
                    JOptionPane.showMessageDialog(waitRoomForm, "비밀번호를 입력하세요.");
                }
                break;

            // 1205 방 비번 불일치 입장실패
            case ResData.ENTER_FAILED_PW:
                JOptionPane.showMessageDialog(
                        waitRoomForm,
                        "비밀번호가 일치하지 않습니다."
                );
                break;

            // 1206 방 인원초과 입장 실패
            case ResData.ENTER_FAILED_FULL:
                JOptionPane.showMessageDialog(
                        waitRoomForm,
                        "방 인원을 초과하였습니다."
                );
                break;

            // 1207 방장용 ui 채팅방 화면 구현
            case ResData.NEW_ROOM_HOST:
                chatRoom = resData.getChatRoom();
                openChatRoom(chatRoom, myUser, out);
                break;

            // 1300 기존 클라이언트 챗방화면 갱신 (방장 방정보 변경 시)
            case ResData.UPDATE_ROOM_ALL:
                chatRoom = resData.getChatRoom();
                updateChatRoom(chatRoom);
                break;

            // 1301 방장으로 변경된 클라이언트 채팅방 화면 갱신
            case ResData.UPDATE_ROOM_HOST:
                chatRoom = resData.getChatRoom();
                updateChatRoom(chatRoom);
//                openChatRoom(chatRoom, myUser, out);
                break;

            // 1302 방정보 바꼈을 때 대기실 정보 갱신
            case ResData.UPDATE_WAITROOM_ROOMLIST:
                waitRoom = resData.getWaitRoom();
                idUserMap = waitRoom.getIdUserMap();
                idRoomMap = waitRoom.getIdRoomMap();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateWaitRoomList(idUserMap, idRoomMap);
                    }
                });
                break;
                // 1303 초대받은 클라이언트 초대 확인/거부 물어보기
            case ResData.SHOW_INVITE_MSG:
                message = resData.getMessage();
                roomId = resData.getId(); // -> 수락하면 서버에게 다시 보내기

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int choice = JOptionPane.showConfirmDialog(
                                waitRoomForm,
                                message.getFrom().getProfile().getNickname() + "님께서 [" + message.getContent() + "] 방에 초대하셨습니다.\n 수락하시겠습니까?",
                                "방 초대",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (choice == JOptionPane.YES_OPTION) {
                            try {
                                ReqData reqData = new ReqData(
                                        ReqData.THANKS,
                                        roomId
                                );
                                out.writeObject(reqData);
                                out.flush();
                                out.reset();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                ReqData reqData = new ReqData(
                                        ReqData.NO_THANKS,
                                        message.getFrom().getId()
                                );
                                out.writeObject(reqData);
                                out.flush();
                                out.reset();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;

            // 1304 초대 거절 알리기 -> 초대 거절당한 클라이언트
            case ResData.INVITE_FAILED:
                String nickname = resData.getContent();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                                chatRoomForm,
                                "[" + nickname + "]님이 초대를 거절하였습니다."
                        );
                    }
                });
                break;

            // 1305 강퇴당해서 대기실에 진입하는 클라이언트 대기실 화면구현
            case ResData.NEW_WAITROOM_ALL_KICKED:
                waitRoom = resData.getWaitRoom();
                idUserMap = waitRoom.getIdUserMap();
                idRoomMap = waitRoom.getIdRoomMap();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        waitRoomForm = new WaitRoomForm(
                                out,
                                getUser(idUserMap),
                                socket,
                                getRoomList(idRoomMap)
                        );
                        nowRoom = waitRoomForm;
                        chatRoomForm.dispose();
                        JOptionPane.showMessageDialog(waitRoomForm, "방장에 의해 강퇴당하였습니다.");
                    }
                });
                break;

            // 1400 방 안 유저들에게 메시지 보내기
            case ResData.BROADCAST:
                message = resData.getMessage();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        nowRoom.addMessage(message);
                    }
                });
                break;

            // 1401 귓속말 보내기
            case ResData.WHISPER:
                message = resData.getMessage();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        nowRoom.whisper(message);
                    }
                });
                break;

            // 1402 귓속말 성공
            case ResData.WHISPER_SUCCESSED:
                message = resData.getMessage();
                String to = resData.getContent();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        nowRoom.whisperReturn(message, to);
                    }
                });
                break;

            // 1501 유저 정보 변경: 대기실 화면 갱신(유저프로필 + 대기실 인원 목록)
            case ResData.UPDATE_PROFILE_USERLIST:
                idUserMap = resData.getIdUserMap();
                myUser = getUser(idUserMap);
                waitRoomForm.setMyUser(myUser);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        waitRoomForm.updateProfile(myUser.getProfile().getNickname(), myUser.getProfile().getImgNum());
                        waitRoomForm.updateUserList(getUserList(idUserMap));
                    }
                });
                break;

            // 1502 유저 정보 변경 실패: 닉네임 중복
            case ResData.UPDATE_PROFILE_FAILED_DUPLICATE:
                duplicateName(waitRoomForm);
                break;

            // 1600 유저 초대창 생성 -> 유저 검색창 띄우려는 클라이언트
            case ResData.NEW_INVITE_USER_ALL:
                idUserMap = resData.getIdUserMap();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        userListForm = new UserListForm(
                            chatRoomForm,
                            idUserMap,
                            myUser
                        );
                        userListForm.setVisible(true);

                    }
                });
                break;

            // 1602 유저 즐겨찾기창 생성 -> 유저 즐겨찾기창 띄운 클라이언트
            case ResData.NEW_BOOKMARKS_ALL:
                idUserMap = resData.getIdUserMap();
                Map<Long, User> waitMap = new HashMap<>();
                Map<Long, User> chatMap = new HashMap<>();
                Map<Long, User> bookmarks = resData.getBookmarks();


                bookmarkList(bookmarks, idUserMap, waitMap, chatMap);


                if(nowRoom == waitRoomForm) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            userListForm = new UserListForm(
                                    waitRoomForm,
                                    waitMap,
                                    chatMap,
                                    myUser
                            );
                            userListForm.setVisible(true);
                        }
                    });

                } else {
                    // nowRoom == chatRoomForm
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            userListForm = new UserListForm(
                                    chatRoomForm,
                                    waitMap,
                                    chatMap,
                                    myUser
                            );
                            userListForm.setVisible(true);
                        }
                    });
                }

                break;

            // 1603 유저 즐겨찾기창 갱신 -> 유저 즐겨찾기 추가/삭제 한 클라이언트
            case ResData.UPDATE_BOOKMARKS_ALL:
                idUserMap = resData.getIdUserMap();
                bookmarks = resData.getBookmarks();
                waitMap = new HashMap<>();
                chatMap = new HashMap<>();

                bookmarkList(bookmarks, idUserMap, waitMap, chatMap);

                if(nowRoom == waitRoomForm) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(userListForm);
                            userListForm.updateWaitRoom(waitMap, chatMap);
                        }
                    });

                } else {
                    // nowRoom == chatRoomForm
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            userListForm.updateChatRoom(waitMap, chatMap);
                        }
                    });
                }
                break;

            case ResData.ENTER_FAILED_DEAD_ROOM:
                JOptionPane.showMessageDialog(null, "존재하지 않는 방입니다.");
                waitRoom = resData.getWaitRoom();
                idUserMap = waitRoom.getIdUserMap();
                idRoomMap = waitRoom.getIdRoomMap();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateWaitRoomList(idUserMap, idRoomMap);
                    }
                });

        }
    }


    private void bookmarkList(
            Map<Long, User> bookmarks,
            Map<Long, User> idUserMap,
            Map<Long, User> waitMap,
            Map<Long, User> chatMap
    ) {
        User value;
        for(Long key : bookmarks.keySet()) {
            if(idUserMap.containsKey(key)) {
                value = bookmarks.get(key);
                waitMap.put(key, value);
            } else {
                value = bookmarks.get(key);
                chatMap.put(key, value);
            }
        }
    }

    private void openChatRoom(ChatRoom chatRoom, User user, ObjectOutputStream out) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatRoomForm = new ChatRoomForm(chatRoom, user, out);
                nowRoom = chatRoomForm;
                waitRoomForm.dispose();
            }
        });
    }

    private void updateChatRoom(ChatRoom chatRoom) {
        nowRoom = chatRoomForm;
        chatRoomForm.updateRoom(chatRoom);

    }

    private void updateWaitRoomList(Map<Long, User> idUserMap, Map<Long, ChatRoom> idRoomMap) {
        waitRoomForm.updateUserList(getUserList(idUserMap));
        waitRoomForm.updateRoomList(getRoomList(idRoomMap));

    }

    private ChatRoom[] getRoomList(Map<Long, ChatRoom> idRoomMap) {
        ArrayList<ChatRoom> roomList = new ArrayList<>(idRoomMap.values());
        ChatRoom[] chatRooms = roomList.toArray(new ChatRoom[0]);
        return chatRooms;
    }

    private User[] getUserList(Map<Long, User> idUserMap) {
        ArrayList<User> userList = new ArrayList<>(idUserMap.values());
        User[] users = userList.toArray(new User[0]);
        return users;

    }

    private User getUser(Map<Long, User> idUserMap) {
        myUser = idUserMap.get(myId);
        return myUser;
    }

    private void duplicateName(JFrame frame) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(
                        frame,
                        "중복인 닉네임 입니다. 다른 닉네임을 사용해주세요."
                );
            }
        });
    }
}
