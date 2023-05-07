package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import model.ChatRoom;
import model.Message;
import model.ReqData;
import model.RoomInfo;
import model.User;
import ui.component.UserPanel;
import ui.component.WhisperLabel;
import util.IOUtil;
import util.UIUtil;

public class WaitRoomForm extends JFrame implements Talkable {
	private User myUser; // 유저
	private User[] userList; // 대기실 유저리스트
	private ChatRoom[] roomList; // 각 방의 정보들
	private ObjectOutputStream out;
	private Socket socket;
	private ReqData reqData;

	private JPanel pnlUserList;

	private JLabel lblSetting;
	private JLabel lblProfile;
	private JLabel lblNickname;
	private JLabel lblFriendList;

	private JTextField tfUserSearch;
	private RoundedButton btnUserSearch;

	private RoundedButton btnMakeRoom;
	private RoundedButton btnRandomIn;
	private JTextField tfSearch;
	private RoundedButton btnSearch;
	private JCheckBox cbOnlyOpenRoom;
	private JCheckBox cbOnlyEnterable;
	private JTextArea taResult;
	private JScrollPane spResult;
	private JTextField tfInput;
	private RoundedButton btnSend;
	private ImageIcon imgProfile;
	private int profileNum;

	// @@@@ 페이지뷰 추가 @@@
	private JPanel pnlRoomList;
	private JPanel pnlRoomPages;
	private JPanel pnlRoomNums;

	private int pageNum;
	private CardLayout cardLayout;
	private JLabel lblPrev;
	private JLabel lblNext;
	private JLabel[] lblPageNums;
	private JLabel lblFirstNum;
	private JLabel lblLastNum;
	private int pagesLength;

	// @@
	private WhisperLabel lblWhisper;

	// 유저검색패널
	private JPanel pnlUserSearch;

	// 색상 모음
	public static final Color TRANSPARENT = new Color(255, 0, 0, 0); // 투명도
	public static final Color ROOMS_BACKGROUND = new Color(249, 249, 249);

	public WaitRoomForm(ObjectOutputStream out, User myUser, Socket socket, ChatRoom[] roomList) {
		this.out = out;
		this.socket = socket;
		setMyUser(myUser);
		// setRoomList(roomList);

		init();
		setDisplay();
		updateRoomList(roomList);
		addListener();
		showFrame();
	}

	public User getMyUser() {
		return myUser;
	}

	public void setMyUser(User myUser) {
		this.myUser = myUser;
	}

	public void setImgProfile(ImageIcon imgProfile) {
		this.imgProfile = imgProfile;
	}

	public ChatRoom[] getRoomList() {
		return roomList;
	}

	public void setRoomList(ChatRoom[] roomList) {
		this.roomList = roomList;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public void updateProfile(String nickname, int profileNum) {
		lblNickname.setText(nickname);
		changeProfileNum(profileNum);
		revalidate();
	}

	@Override
	public void infoMessage(Message message) {
		taResult.append(
				"<< " + message.getFrom().getProfile().getNickname() + "님이 " + message.getContent() + "하셨습니다 >> \n");
//		taResult.setCaretPosition(taResult.getDocument().getLength());
		revalidate();
	}

	@Override
	public void addMessage(Message message) {
		taResult.append("[" + message.getFrom().getProfile().getNickname() + "] : " + message.getContent() + "\n");
//		taResult.setCaretPosition(taResult.getDocument().getLength());
		revalidate();
	}

	@Override
	public void whisper(Message message) {
		taResult.append(
				"[" + message.getFrom().getProfile().getNickname() + "] : <귓속말> " + message.getContent() + "\n");
//		taResult.setCaretPosition(taResult.getDocument().getLength());
		revalidate();

	}

	@Override
	public void whisperReturn(Message message, String whisperTo) {
		taResult.append("[@" + whisperTo + "] : <귓속말> " + message.getContent() + "\n");
//		taResult.setCaretPosition(taResult.getDocument().getLength());
		revalidate();
	}

	private void init() {
		ImageIcon imgSetting = new ImageIcon();
		imgSetting = UIUtil.changeImageIcon("gear.png", 30, 30);
		lblSetting = new JLabel(imgSetting, JLabel.CENTER);

		imgProfile = new ImageIcon();
		profileNum = myUser.getProfile().getImgNum();
		imgProfile = getProfileImage();
		lblProfile = new JLabel(imgProfile, JLabel.CENTER);

		lblNickname = new JLabel(myUser.getProfile().getNickname(), JLabel.CENTER);
		lblNickname.setFont(UIUtil.getBoldFont(20));
		lblNickname.setBorder(new EmptyBorder(15, 0, 15, 0));

		ImageIcon imgFriendList = new ImageIcon();
		imgFriendList = UIUtil.changeImageIcon("friend.png", 30, 30);
		lblFriendList = new JLabel(" 즐겨찾기", imgFriendList, JLabel.CENTER);
		lblFriendList.setFont(UIUtil.getBoldFont(15));

		tfUserSearch = new JTextField(20);
		tfUserSearch.setBackground(Color.WHITE);
		tfUserSearch.setText("");
		tfUserSearch.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));

		btnUserSearch = new RoundedButton("검색");
		btnUserSearch.setBackground(Color.WHITE);
		btnUserSearch.setFont(UIUtil.getBoldFont(13));
		btnUserSearch.setPreferredSize(new Dimension(45, 30));

		btnMakeRoom = new RoundedButton("방만들기");
		btnMakeRoom.setBackground(Color.WHITE);
		btnMakeRoom.setFont(UIUtil.getBoldFont(13));
		btnMakeRoom.setPreferredSize(new Dimension(90, 30));

		btnRandomIn = new RoundedButton("랜덤입장");
		btnRandomIn.setFont(UIUtil.getBoldFont(13));
		btnRandomIn.setPreferredSize(new Dimension(90, 30));

		tfSearch = new JTextField(50);
		tfSearch.setText("");
		tfSearch.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));

		btnSearch = new RoundedButton("검색");
		btnSearch.setBackground(Color.WHITE);
		btnSearch.setFont(UIUtil.getBoldFont(13));
		btnSearch.setPreferredSize(new Dimension(45, 30));

		cbOnlyOpenRoom = new JCheckBox("공개방만");
		cbOnlyOpenRoom.setBackground(Color.WHITE);
		cbOnlyOpenRoom.setFont(UIUtil.getBoldFont(12));

		cbOnlyEnterable = new JCheckBox("입장가능한 방만");
		cbOnlyEnterable.setBackground(Color.WHITE);
		cbOnlyEnterable.setFont(UIUtil.getBoldFont(12));

		taResult = new JTextArea();
		taResult.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		taResult.setFont(UIUtil.getPlainFont(13));
		taResult.setLineWrap(true);
		taResult.setWrapStyleWord(true);
		taResult.setEditable(false);
		taResult.setBorder(new EmptyBorder(0, 0, 0, 0));
		spResult = new JScrollPane(taResult);
		spResult.setBackground(Color.WHITE);
		spResult.setBorder(new EmptyBorder(0, 0, 0, 0));
		spResult.setPreferredSize(new Dimension(81, 250));
//		spResult.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 1)));
		spResult.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		spResult.getVerticalScrollBar().setUnitIncrement(16); // 스크롤 속도

		tfInput = new JTextField(84);
		tfInput.setText("");
		tfInput.setBorder(new EmptyBorder(0, 0, 0, 0));

		btnSend = new RoundedButton("전송");
		btnSend.setFont(UIUtil.getBoldFont(13));
		btnSend.setPreferredSize(new Dimension(70, 25));
		// btnSend.setBorder(new EmptyBorder(0, 10, 0, 0));

		pnlUserList = new JPanel();
		pnlUserList.setLayout(new BoxLayout(pnlUserList, BoxLayout.Y_AXIS)); // Y_AXIS는 세로 방향으로 나열
//		pnlUserList = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		pnlUserList.setBackground(Color.WHITE);

		// @@@@ RoomList
		pnlRoomList = new JPanel(new BorderLayout());
		pnlRoomList.setBackground(ROOMS_BACKGROUND);
//		pnlRoomList.setBackground(Color.RED);
		pnlRoomList.setBorder(new EmptyBorder(15, 20, 15, 20));

		cardLayout = new CardLayout();

		pnlRoomPages = new JPanel(new BorderLayout());
		pnlRoomPages.setLayout(cardLayout);
//		pnlRoomPages.setBackground(Color.WHITE);
		// pnlRoomPages.setBorder(new EmptyBorder(10, 0, 0, 0));

		pnlRoomNums = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
//		pnlRoomNums.setBackground(Color.WHITE);
		pnlRoomNums.setBackground(ROOMS_BACKGROUND);
		pnlRoomNums.setBorder(new EmptyBorder(10, 0, 0, 0));

		pnlRoomList.add(pnlRoomPages, BorderLayout.CENTER);
		pnlRoomList.add(pnlRoomNums, BorderLayout.SOUTH);

		pageNum = 1;
		pagesLength = 1;

		Font font = new Font("맑은 고딕", Font.BOLD, 15);
		lblPrev = new JLabel("<");
		lblPrev.setFont(font);
		lblNext = new JLabel(">");
		lblNext.setFont(font);
		lblPageNums = new JLabel[5];
		for (int idx = 0; idx < 5; idx++) {
			lblPageNums[idx] = new JLabel();
			lblPageNums[idx].setFont(font);
		}
		lblFirstNum = new JLabel("1");
		lblFirstNum.setFont(font);
		lblLastNum = new JLabel();
		lblLastNum.setFont(font);

		lblWhisper = new WhisperLabel();
//		lblWhisper = new JLabel("@");
//		lblWhisper.setBackground(new Color(255,247,242));
//		lblWhisper.setOpaque(true);
//		lblWhisper.setFont(new Font("맑은 고딕", Font.BOLD, 12));
//		//lblWhisper.setSize(new Dimension(13, 13));
//		lblWhisper.setBorder(new EmptyBorder(0, 5, 0, 5));
	}

	private void setDisplay() {
		Toolkit kit = Toolkit.getDefaultToolkit();

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBackground(Color.WHITE);
		pnlMain.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlMain.setBorder(new MatteBorder(1, 1, 1, 1, new Color(234, 234, 234)));

		JPanel pnlWest = new JPanel(new BorderLayout());
		pnlWest.setBackground(Color.WHITE);
		pnlWest.setBorder(new MatteBorder(0, 0, 0, 1, new Color(234, 234, 234)));

		JPanel pnlProfile = new JPanel(new BorderLayout());
		pnlProfile.setBackground(Color.WHITE);
		pnlProfile.setBorder(new MatteBorder(0, 0, 1, 0, new Color(234, 234, 234)));

		JPanel pnlSetting = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlSetting.setBackground(Color.WHITE);
		pnlSetting.add(lblSetting);

		pnlProfile.add(pnlSetting, BorderLayout.NORTH);
		pnlProfile.add(lblProfile, BorderLayout.CENTER);
		pnlProfile.add(lblNickname, BorderLayout.SOUTH);

		JPanel pnlFriend = new JPanel();
		pnlFriend.setBackground(Color.WHITE);
		pnlFriend.setBorder(new EmptyBorder(0, 10, 0, 10));
		pnlFriend.setBorder(new MatteBorder(0, 0, 1, 0, new Color(234, 234, 234)));
//		pnlFriend.setPreferredSize(new Dimension(265, 15));
		pnlFriend.add(lblFriendList);

		pnlUserSearch = new JPanel(new BorderLayout());
		pnlUserSearch.setBackground(Color.WHITE);

		JPanel pnlInput = new JPanel();
		pnlInput.setBackground(Color.WHITE);
//		pnlInput.setBorder(new MatteBorder(0, 0, 1, 0, new Color(255, 204, 0)));
		pnlInput.add(tfUserSearch);
		pnlInput.add(btnUserSearch);

		pnlUserSearch.add(pnlInput, BorderLayout.NORTH);

//		JPanel pnlWaitUserList = new JPanel(new BorderLayout());
//		pnlWaitUserList.setBackground(Color.WHITE);

		JScrollPane scroll = new JScrollPane(pnlUserList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		scroll.setPreferredSize(new Dimension(282, 367));
		scroll.setBackground(Color.WHITE);
		scroll.getVerticalScrollBar().setUnitIncrement(16); // 스크롤 속도

//		pnlWaitUserList.add(pnlWaitUserListTop, BorderLayout.NORTH);
		pnlUserSearch.add(scroll, BorderLayout.CENTER);

		pnlWest.add(pnlProfile, BorderLayout.NORTH);
		pnlWest.add(pnlFriend, BorderLayout.CENTER);
		pnlWest.add(pnlUserSearch, BorderLayout.SOUTH);

		JPanel pnlEast = new JPanel(new BorderLayout());
		pnlEast.setBackground(Color.WHITE);
		pnlMain.add(pnlWest, BorderLayout.WEST);
		// @@@@ CENTER로 수정 @@@@
		pnlMain.add(pnlEast, BorderLayout.CENTER);

		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setBackground(Color.WHITE);
		pnlTop.setBorder(new MatteBorder(0, 0, 1, 0, new Color(234, 234, 234)));

		JPanel pnlcb = new JPanel(new BorderLayout());
		pnlcb.setBackground(Color.WHITE);
		pnlcb.add(cbOnlyOpenRoom, BorderLayout.CENTER);
		pnlcb.add(cbOnlyEnterable, BorderLayout.SOUTH);
		pnlcb.setBorder(new EmptyBorder(0, 10, 0, 0));

		JPanel pnlRoomBtns = new JPanel();
		pnlRoomBtns.setBackground(Color.WHITE);
		pnlRoomBtns.add(btnMakeRoom);
		pnlRoomBtns.add(btnRandomIn);
		pnlRoomBtns.setBorder(new EmptyBorder(10, 0, 0, 0));

		JPanel pnlSearchBtns = new JPanel();
		pnlSearchBtns.setBackground(Color.WHITE);
		pnlSearchBtns.add(btnSearch);
		pnlSearchBtns.add(pnlcb);

		JPanel pnlSearchInput = new JPanel(new BorderLayout());
//		pnlSearchInput.setLayout();
		pnlSearchInput.setBackground(Color.WHITE);
		pnlSearchInput.add(tfSearch, BorderLayout.CENTER);
		pnlSearchInput.setBorder(new EmptyBorder(18, 0, 18, 0));

		pnlTop.add(pnlRoomBtns, BorderLayout.WEST);
		pnlTop.add(pnlSearchInput, BorderLayout.CENTER);
		pnlTop.add(pnlSearchBtns, BorderLayout.EAST);

		JPanel pnlBottom = new JPanel(new BorderLayout());
		pnlBottom.setBackground(Color.WHITE);
		pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(234, 234, 234)));

		JPanel pnlSend = new JPanel(new BorderLayout());
		pnlSend.setBackground(Color.WHITE);
		pnlSend.setBorder(new MatteBorder(1, 0, 0, 0, new Color(234, 234, 234)));

		pnlEast.add(pnlTop, BorderLayout.NORTH);
		pnlEast.add(pnlRoomList, BorderLayout.CENTER);
		pnlEast.add(pnlBottom, BorderLayout.SOUTH);

		pnlBottom.add(spResult, BorderLayout.CENTER);

		pnlSend.add(lblWhisper, BorderLayout.WEST);
		pnlSend.add(tfInput, BorderLayout.CENTER);
		pnlSend.add(btnSend, BorderLayout.EAST);
		pnlBottom.add(pnlSend, BorderLayout.SOUTH);

		add(pnlMain);
	}

	// @@@@@@@@@@
//	public JLabel getLblWhisper() {
//		return lblWhisper;
//	}
//
//	public void setLblWhisper(JLabel lblWhisper) {
//		this.lblWhisper = lblWhisper;
//
//		pnlSend.removeAll();
//		pnlSend.add(lblWhisper);
//		pnlSend.add(tfInput);
//		pnlSend.add(btnSend);
////		pnlBottom.add(pnlSend, BorderLayout.SOUTH);
//		revalidate();
//		pnlSend.updateUI();
//	}
	@Override
	public void whisperOn(User whisperTo) {
		lblWhisper.whisperOn(whisperTo);
	}

	@Override
	public void whisperOff() {
		lblWhisper.whisperOff();
	}

	public void updateUserList(User[] userList) {
		this.userList = userList;
		pnlUserList.removeAll();

		Arrays.sort(userList, new Comparator<User>() {
			@Override
			public int compare(User user1, User user2) {
				if (user1.equals(myUser)) {
					return -1;
				}
				if (user2.equals(myUser)) {
					return 1;
				}
				return user1.compareTo(user2) * -1;
			}
		});

		for (int idx = 0; idx < userList.length; idx++) {
			pnlUserList.add(new UserPanel(this, userList[idx], userList[idx].equals(myUser)));
		}
		revalidate();
		pnlUserList.updateUI();
	}

	public void searchUsers(String keyword) {
		pnlUserList.removeAll();
		List<User> resultUsers = new ArrayList<User>();

		for (int i = 0; i < userList.length; i++) {
			if (userList[i].getProfile().getNickname().contains(keyword)) {
				resultUsers.add(userList[i]);
			}
		}

		resultUsers.sort(new Comparator<User>() {
			@Override
			public int compare(User user1, User user2) {
				if (user1.equals(myUser)) {
					return -1;
				}
				if (user2.equals(myUser)) {
					return 1;
				}
				return user1.compareTo(user2) * -1;
			}
		});

		for (int idx = 0; idx < resultUsers.size(); idx++) {
			pnlUserList.add(new UserPanel(this, resultUsers.get(idx), resultUsers.get(idx).equals(myUser)));
		}
		revalidate();
		pnlUserList.updateUI();
	}

	public void updateRoomList(ChatRoom[] roomList) {
		this.roomList = roomList;
		updateRoomPanel(roomList);
	}

	private void updateRoomPanel(ChatRoom[] roomList) {
		pnlRoomPages.removeAll();
		pnlRoomNums.removeAll();

		List<ChatRoom> selectedRooms = Arrays.asList(roomList);

		if (cbOnlyOpenRoom.isSelected()) {
			selectedRooms = getOpenRooms(selectedRooms);
		}
		if (cbOnlyEnterable.isSelected()) {
			selectedRooms = getEnterableRooms(selectedRooms);
		}

		int num = 0;
		JPanel pnlPage = new JPanel(new GridLayout(2, 5, 40, 20));
		pnlPage.setBackground(ROOMS_BACKGROUND);
		JPanel pnl;

		selectedRooms.sort(new Comparator<ChatRoom>() {
			@Override
			public int compare(ChatRoom room1, ChatRoom room2) {
				return room1.compareTo(room2);
			}
		});
		for (int idx = 0; idx < selectedRooms.size(); idx++) {
			System.out.println(selectedRooms.get(idx));
			pnl = new RoomPanel(selectedRooms.get(idx));
			pnlPage.add(pnl);
			num++;

			if (num % 10 == 0) {
				pnlRoomPages.add(pnlPage, String.valueOf(num / 10));
				pnlPage = new JPanel(new GridLayout(2, 5, 40, 20));
				pnlPage.setBackground(ROOMS_BACKGROUND);
			}
		}
		int count = 10 - (num % 10);
		for (int i = 0; i < count; i++) {
			pnl = new JPanel();
			pnl.setBackground(ROOMS_BACKGROUND);
			pnlPage.add(pnl);
		}

		pagesLength = (num / 10) + (num % 10 > 0 ? 1 : 0);
		pnlRoomPages.add(String.valueOf(pagesLength), pnlPage);

		pnlRoomNums.add(lblPrev);
		int max = (pagesLength < 3) ? pagesLength : 3;
		for (int i = 0; i < max; i++) {
			lblPageNums[i].setText(String.valueOf(i + 1));
			pnlRoomNums.add(lblPageNums[i]);
		}
		lblLastNum.setText(String.valueOf(pagesLength));
		if (pagesLength > 3) {
			pnlRoomNums.add(new JLabel("..."));
			pnlRoomNums.add(lblLastNum);
		}
		pnlRoomNums.add(lblNext);
//
		if (pageNum != 1) {
			showSelectedPage();
		}

		pnlRoomList.updateUI();
	}

	public List<ChatRoom> getOpenRooms(List<ChatRoom> roomList) {
		List<ChatRoom> list = new ArrayList<ChatRoom>();
		for (ChatRoom room : roomList) {
			if (!room.isPrivate()) {
				list.add(room);
			}
		}
		return list;
	}

	public List<ChatRoom> getEnterableRooms(List<ChatRoom> roomList) {
		List<ChatRoom> list = new ArrayList<ChatRoom>();
		for (ChatRoom room : roomList) {
			if (room.getIdUserMap().size() < room.getInfo().getMaxUser()) {
				list.add(room);
			}
		}
		return list;
	}

	public void searchRooms(String keyword) {
		List<ChatRoom> resultRooms = new ArrayList<ChatRoom>();
		for (ChatRoom room : roomList) {
			if (room.getInfo().getTitle().contains(keyword)) {
				resultRooms.add(room);
			}
		}
		updateRoomPanel(resultRooms.toArray(new ChatRoom[0]));
	}

	public void enterRandomRoom() {
		List<ChatRoom> openRooms = getOpenRooms(Arrays.asList(roomList));
		openRooms = getEnterableRooms(openRooms);

		if (openRooms.size() == 0) {
			JOptionPane.showMessageDialog(this, "현재 들어갈 수 있는 공개방이 없습니다.", "랜덤 입장 불가", JOptionPane.INFORMATION_MESSAGE);
		} else {
			int idx = (int) (Math.random() * openRooms.size());
			enterRoom(openRooms.get(idx));
		}
	}

	public void enterRoom(ChatRoom room) {
		System.out.println("방 입장 : " + room.getInfo().getTitle());
		// 방 입장 프로토콜 ENTER_ROOM = 201 만들고 서버에 전송하는 부분
		try {
			reqData = new ReqData(ReqData.ENTER_ROOM, room.getId());
			out.writeObject(reqData);
			out.flush();
			out.reset();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class RoomPanel extends JPanel {
		private ChatRoom room;

		public RoomPanel(ChatRoom room) {
			this.room = room;

			setLayout(new BorderLayout());
			setBackground(TRANSPARENT);

			RoomInfo info = room.getInfo();

			JPanel pnlColor = new JPanel(new BorderLayout()) {
				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);

					int width = getWidth();
					int height = getHeight();

					g.setColor(UIUtil.ROOM_COLORS[info.getColorNum()]);
					g.fillRoundRect(0, 0, width, height, 20, 20);
				}
			};
			pnlColor.setBackground(TRANSPARENT);

			if (room.isPrivate()) {
				JLabel lblLock = new JLabel(UIUtil.changeImageIcon("private.png", 60, 60));
				pnlColor.add(lblLock);
			}

			JPanel pnlTexts = new JPanel(new BorderLayout());
			pnlTexts.setOpaque(false);
			pnlTexts.setBorder(new EmptyBorder(10, 0, 10, 0));
			JLabel lblTitle = new JLabel(info.getTitle(), JLabel.CENTER);
			lblTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
			JLabel lblCount = new JLabel(room.getIdUserMap().size() + " / " + info.getMaxUser(), JLabel.CENTER);
			lblCount.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

			pnlTexts.add(lblTitle, BorderLayout.CENTER);
			pnlTexts.add(lblCount, BorderLayout.SOUTH);

			add(pnlColor, BorderLayout.CENTER);
			add(pnlTexts, BorderLayout.SOUTH);

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					((Component) me.getSource()).setCursor(new Cursor(Cursor.HAND_CURSOR));
				}

				@Override
				public void mousePressed(MouseEvent me) {
					enterRoom(room);
				}
			});
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			int width = getWidth();
			int height = getHeight();

			g.setColor(Color.WHITE);
			g.fillRoundRect(0, 0, width, height, 20, 20);
		}
	}

	private void addListener() {
		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (ae.getSource() == tfInput || ae.getSource() == btnSend) {
					String text = tfInput.getText();
					tfInput.requestFocus();
					tfInput.setText("");
					if (!(tfInput.getText().length() != 0)) {
						try {
							if (lblWhisper.getUserId() != 0) {
								reqData = new ReqData(ReqData.SEND_MSG_TO, lblWhisper.getUserId(), text);
								out.writeObject(reqData);
								out.flush();
								out.reset();
							} else {
								// 모두에게 보냄
								reqData = new ReqData(ReqData.SEND_MSG, text);
								out.writeObject(reqData);
								out.flush();
								out.reset();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if (ae.getSource() == tfSearch || ae.getSource() == btnSearch) {
					String keyword = tfSearch.getText();
					searchRooms(keyword);

					tfSearch.requestFocus();
					tfSearch.setText("");
				}
				if (ae.getSource() == btnMakeRoom) {
					new RoomMakeForm(WaitRoomForm.this);
				}
				if (ae.getSource() == btnRandomIn) {
					enterRandomRoom();
				}
				if (ae.getSource() == cbOnlyOpenRoom || ae.getSource() == cbOnlyEnterable) {
					updateRoomPanel(roomList);
				}
				if (ae.getSource() == btnUserSearch || ae.getSource() == tfUserSearch) {
					searchUsers(tfUserSearch.getText());
				}
			}
		};
		tfUserSearch.addActionListener(aListener);
		btnUserSearch.addActionListener(aListener);

		tfInput.addActionListener(aListener);
		btnSend.addActionListener(aListener);

		tfSearch.addActionListener(aListener);
		btnSearch.addActionListener(aListener);

		btnMakeRoom.addActionListener(aListener);
		btnRandomIn.addActionListener(aListener);
		cbOnlyOpenRoom.addActionListener(aListener);
		cbOnlyEnterable.addActionListener(aListener);

		MouseListener mListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent me) {
				// if(me.getSource() instanceof JLabel) {
				Component src = (Component) me.getSource();
				src.setCursor(new Cursor(Cursor.HAND_CURSOR));

//					Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
//					lblFriendList.setCursor(handCursor);
//					lblSearchUser.setCursor(handCursor);
//					lblSetting.setCursor(handCursor);
				// }
			}

			@Override
			public void mouseExited(MouseEvent me) {
				// if(me.getSource() instanceof JLabel) {
				Component src = (Component) me.getSource();
				src.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

//					Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
//					lblFriendList.setCursor(handCursor);
//					lblSearchUser.setCursor(handCursor);
//					lblSetting.setCursor(handCursor);
				// }
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getSource() == lblSetting) {
					// 설정 버튼 눌렀을 때, 닉넴변경과 이미지변경됨
					new ProfileEditForm(out, WaitRoomForm.this, lblNickname.getText(), myUser.getProfile().getImgNum());
				}
				if (e.getSource() == lblWhisper) {
					lblWhisper.whisperOff();
				}
				// 601 즐겨찾기 클릭 시
				if (e.getSource() == lblFriendList) {
					try {
						reqData = new ReqData(ReqData.SHOW_BOOKMARKS);
						out.writeObject(reqData);
						out.flush();
						out.reset();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				boolean pageSelected = false;
				if (e.getSource() == lblPrev) {
					if (pageNum > 1) {
						pageNum--;
						pageSelected = true;
					}
				}
				if (e.getSource() == lblNext) {
					if (pageNum < pagesLength) {
						pageNum++;
						pageSelected = true;
					}
				}
				for (JLabel lbl : lblPageNums) {
					if (e.getSource() == lbl) {
						pageNum = Integer.parseInt(lbl.getText());
						pageSelected = true;
					}
				}
				if (e.getSource() == lblFirstNum) {
					pageNum = 1;
					pageSelected = true;
				}
				if (e.getSource() == lblLastNum) {
					pageNum = pagesLength;
					pageSelected = true;
				}
				if (pageSelected) {
					// 선택한 페이지 띄우기
					showSelectedPage();
				}
			}
		};
		lblFriendList.addMouseListener(mListener);
		lblSetting.addMouseListener(mListener);

		lblWhisper.addMouseListener(mListener);

		lblPrev.addMouseListener(mListener);
		lblNext.addMouseListener(mListener);
		for (JLabel lbl : lblPageNums) {
			lbl.addMouseListener(mListener);
		}
		lblFirstNum.addMouseListener(mListener);
		lblLastNum.addMouseListener(mListener);

		WindowListener wListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				int answer = JOptionPane.showConfirmDialog(null, "첫 화면으로 돌아가시겠습니까?", "질문", JOptionPane.YES_NO_OPTION);
				if (answer == 0) {
					exit();
					dispose();
					new LoginForm();
				}
			}
		};
		this.addWindowListener(wListener);
	}

	private void showSelectedPage() {
		cardLayout.show(pnlRoomPages, String.valueOf(pageNum));

		// 페이지 번호 패널 갱신
		pnlRoomNums.removeAll();
		pnlRoomNums.add(lblPrev);
		if (pageNum - 3 >= 1) {
			pnlRoomNums.add(lblFirstNum);
		}
		if (pageNum - 3 > 1) {
			pnlRoomNums.add(new JLabel("..."));
		}
		int start = (pageNum - 2 < 1) ? 1 : pageNum - 2;
		int end = (pageNum + 2 > pagesLength) ? pagesLength : pageNum + 2;
		for (int idx = 0; idx < end - start + 1; idx++) {
			lblPageNums[idx].setText(String.valueOf(idx + start));
			pnlRoomNums.add(lblPageNums[idx]);
		}
		if (pageNum + 3 < pagesLength) {
			pnlRoomNums.add(new JLabel("..."));
		}
		if (pageNum + 3 <= pagesLength) {
			pnlRoomNums.add(lblLastNum);
		}
		pnlRoomNums.add(lblNext);
	}

	private void showFrame() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(1300, 800);
		setResizable(false);
		setVisible(true);
		tfInput.requestFocus();
	}

	// 연결끊음
	private void exit() {
		IOUtil.allClose(out, socket);
	}

	public void changeProfileNum(int profileNum) {
		this.profileNum = profileNum;
		imgProfile = getProfileImage();
		lblProfile.setIcon(imgProfile);
	}

	private ImageIcon getProfileImage() {
		return UIUtil.getProfileImage(profileNum, 220, 220);
	}

	// 버튼 디자인
	public class RoundedButton extends JButton {

		public RoundedButton() {
			super();
			decorate();
		}

		public RoundedButton(ImageIcon imageIcon) {
			super(imageIcon);
			decorate();
		}

		public RoundedButton(String text) {
			super(text);
			decorate();
		}

		public RoundedButton(Action action) {
			super(action);
			decorate();
		}

		public RoundedButton(Icon icon) {
			super(icon);
			decorate();
		}

		public RoundedButton(String text, Icon icon) {
			super(text, icon);
			decorate();
		}

		protected void decorate() {
			setBorderPainted(false);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Color c = new Color(240, 238, 237, 200); // 배경색 결정
			Color o = new Color(69, 69, 69); // 글자색 결정
			int width = getWidth();
			int height = getHeight();
			Graphics2D graphics = (Graphics2D) g;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (getModel().isArmed()) {
				graphics.setColor(c.darker());
			} else if (getModel().isRollover()) {
				graphics.setColor(c.brighter());
			} else {
				graphics.setColor(c);
			}
			graphics.fillRoundRect(0, 0, width, height, 10, 10);
			FontMetrics fontMetrics = graphics.getFontMetrics();
			Rectangle stringBounds = fontMetrics.getStringBounds(this.getText(), graphics).getBounds();
			int textX = (width - stringBounds.width) / 2;
			int textY = (height - stringBounds.height) / 2 + fontMetrics.getAscent();
			graphics.setColor(o);
			graphics.setFont(getFont());
			graphics.drawString(getText(), textX, textY);
			graphics.dispose();
			super.paintComponent(g);
		}
	}
}
