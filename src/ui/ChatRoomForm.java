package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import model.ChatRoom;
import model.Message;
import model.ReqData;
import model.RoomInfo;
import model.User;
import ui.component.ChatRoomUserPanel;
import ui.component.MessagePanel;
import ui.component.WhisperLabel;
import util.UIUtil;

public class ChatRoomForm extends JFrame implements Talkable {
	private User myUser;
	private ChatRoom chatRoom;
	private RoomInfo roomInfo;

	private JLabel lblFriend;
	private JLabel lblInvite;
	private JLabel lblPeople;
	private JLabel lblrootName;
	private JPanel pnlCenter;
	private JPanel pnlEast;
	private JLabel lblSetting;
	private JLabel lblExit;

	private JPopupMenu popmenu;
	private JMenuItem mi1;
	private JMenuItem mi2;
	private JMenuItem mi3;

	private JLabel lblUser;
	private JLabel lblTotal;
	private JPanel pnlSearch;
	private JPanel pnlChat;
	private JPanel pnlBox;

	private JTextField tfChat;
	private RoundedButton btnSend;
	private JScrollPane pane;
	private JPanel pnlUserList;
	private WhisperLabel lblWhisper;
	private ObjectOutputStream out;
	private boolean isScroll;
	
	private Color theme;
	private JPanel pnlMain;
	private boolean isHost;

	public ChatRoomForm(ChatRoom chatRoom, User myUser, ObjectOutputStream out) {
		this.chatRoom = chatRoom;
		roomInfo = chatRoom.getInfo();
		this.myUser = myUser;
		this.out = out;
		
		theme = UIUtil.ROOM_COLORS[roomInfo.getColorNum()];
		isHost = chatRoom.getHostId() == myUser.getId();

		init();
		setDisplay();

		updateRoom(chatRoom);

		addListeners();
		showFrame();
	}

	public Color getTheme() {
		return theme;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public ChatRoom getChatRoom() {
		return chatRoom;
	}
	
	public boolean isHost(long userId) {
		return chatRoom.getHostId() == userId;
	}

	private void init() {
		lblFriend = new JLabel("즐겨찾기", UIUtil.changeImageIcon("friend.png", 30, 30), JLabel.LEFT);
		lblFriend.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		// 이 부분 초대 사진으로 변경해야 함!
		lblInvite = new JLabel("유저초대", UIUtil.changeImageIcon("search.png", 30, 30), JLabel.RIGHT);
		lblInvite.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		lblInvite.setBorder(new EmptyBorder(0, 0, 0, 5));

		lblPeople = new JLabel(UIUtil.changeImageIcon("people.png", 30, 30));

		lblrootName = new JLabel(roomInfo.getTitle(), JLabel.CENTER);
		lblrootName.setFont(new Font("맑은 고딕", Font.BOLD, 15));

		lblUser = new JLabel();
		lblUser.setFont(new Font("맑은 고딕", Font.BOLD, 17));
		lblTotal = new JLabel(String.valueOf(roomInfo.getMaxUser()));
		lblTotal.setFont(new Font("맑은 고딕", Font.BOLD, 17));

		lblSetting = new JLabel(UIUtil.changeImageIcon("gear.png", 30, 30));
		lblExit = new JLabel(UIUtil.changeImageIcon("out.png", 30, 30));

		tfChat = new JTextField(37);
//		tfChat.setBorder(new EmptyBorder(0,0,0,0));

		btnSend = new RoundedButton("전송");
		btnSend.setFont(new Font("맑은 고딕", Font.BOLD, 13));

		popmenu = new JPopupMenu();

		mi1 = new JMenuItem("귓속말 보내기");
		mi2 = new JMenuItem("방장 위임하기");
		mi3 = new JMenuItem("강퇴하기");

		popmenu.add(mi1);
		popmenu.add(mi2);
		popmenu.add(mi3);

		lblWhisper = new WhisperLabel();

		// @@@ 유저리스트 세로 배치 확인!!!!!!!!!
		pnlUserList = new JPanel();
		pnlUserList.setLayout(new BoxLayout(pnlUserList, BoxLayout.Y_AXIS));
		pnlUserList.setBackground(Color.WHITE);
	}

	private void setDisplay() {
//		Color colorTheme = new Color(250, 223, 127);
		Color colorTheme = theme;

		JPanel pnlSearchIn = new JPanel(new GridLayout(1, 2));
		pnlSearchIn.add(lblFriend);
		pnlSearchIn.add(lblInvite);
		pnlSearchIn.setBackground(Color.WHITE);
		pnlSearchIn.setBorder(new EmptyBorder(10, 20, 10, 20));

		pnlSearch = new JPanel(new BorderLayout());
		pnlSearch.setBorder(new MatteBorder(0, 0, 2, 0, new Color(234, 234, 234)));
		pnlSearch.setBackground(Color.WHITE);
		pnlSearch.add(pnlSearchIn, BorderLayout.CENTER);
		pnlSearch.setPreferredSize(new Dimension(210, 51));

		JScrollPane scroll = new JScrollPane(pnlUserList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		scroll.setBackground(Color.WHITE);
		scroll.getVerticalScrollBar().setUnitIncrement(16); // 스크롤 속도

		pnlEast = new JPanel(new BorderLayout());
		pnlEast.setPreferredSize(new Dimension(240, 500));
		pnlEast.setBorder(new MatteBorder(0, 2, 0, 0, new Color(234, 234, 234)));
		pnlEast.add(pnlSearch, BorderLayout.NORTH);
		pnlEast.add(scroll, BorderLayout.CENTER);
		pnlEast.setBackground(Color.WHITE);

		JPanel pnlExit = new JPanel();
		pnlExit.add(lblExit);
//		pnlExit.setBackground(colorTheme);
		pnlExit.setOpaque(false);

		JPanel pnlRoomNamePlus2 = new JPanel();
		pnlRoomNamePlus2.add(pnlExit);
		pnlRoomNamePlus2.add(lblSetting);
//		pnlRoomNamePlus2.setBackground(colorTheme);
		pnlRoomNamePlus2.setOpaque(false);

		JPanel pnlUserNumber = new JPanel();
//		pnlUserNumber.setBackground(colorTheme);
		pnlUserNumber.setOpaque(false);
		pnlUserNumber.add(lblUser);
		pnlUserNumber.add(lblTotal);
		pnlUserNumber.setBorder(new EmptyBorder(7, 0, 0, 0));

		JPanel pnlSetting = new JPanel(new BorderLayout(10, 0));
		pnlSetting.setOpaque(false);
//		pnlSetting.setBackground(colorTheme);
		pnlSetting.add(lblPeople, BorderLayout.EAST);
		pnlSetting.add(pnlUserNumber, BorderLayout.WEST);
		pnlSetting.setBorder(new EmptyBorder(3, 0, 3, 20));

		JPanel pnlRoomInfo = new JPanel(new BorderLayout());
		pnlRoomInfo.add(pnlRoomNamePlus2, BorderLayout.WEST);
		pnlRoomInfo.add(lblrootName, BorderLayout.CENTER);
		pnlRoomInfo.add(pnlSetting, BorderLayout.EAST);
		pnlRoomInfo.setBorder(new MatteBorder(0, 0, 2, 0, Color.WHITE));
		pnlRoomInfo.setOpaque(false);
//		pnlRoomInfo.setBackground(colorTheme);

		pnlBox = new JPanel();
//		pnlBox.setBackground(colorTheme);
		pnlBox.setOpaque(false);
		pnlBox.setLayout(new BoxLayout(pnlBox, BoxLayout.Y_AXIS));

		JPanel pnlChattingBox = new JPanel(new BorderLayout());
		pnlChattingBox.setOpaque(false);
//		pnlChattingBox.setBackground(colorTheme);

		pnlChat = new JPanel(new BorderLayout());
		pnlChat.add(pnlBox, BorderLayout.NORTH);
		pnlChat.add(pnlChattingBox, BorderLayout.CENTER);
		// pnlChat.setLayout(new BoxLayout(pnlChat, BoxLayout.Y_AXIS));

//		pnlChat.setOpaque(false);
		pnlChat.setBackground(colorTheme);

		pane = new JScrollPane(pnlChat, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JViewport viewport = pane.getViewport(); // JScrollPane의 JViewport 객체 가져오기
		Rectangle rect = viewport.getViewRect(); // 뷰포트에 표시되는 영역의 크기와 위치 정보를 가진 Rectangle 객체 생성
		rect.y = viewport.getViewSize().height - viewport.getHeight(); // Rectangle 객체의 y 값을 스크롤바를 맨 아래로 내리기 위한 값으로 변경
		viewport.setViewPosition(rect.getLocation()); // 뷰포트의 위치를 변경하여 스크롤을 맨 아래로 내림
		pane.setBorder(null);
		pane.setBackground(Color.WHITE);
		pane.getVerticalScrollBar().setUnitIncrement(16);
//		pane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
//		    public void adjustmentValueChanged(AdjustmentEvent e) {
//		        e.getAdjustable().setValue(e.getAdjustable().getMaximum());
//		    }
//		});
		JPanel pnlSend = new JPanel(new BorderLayout());
//		tfChat.setBackground(new Color(0xFEF4C0));
		tfChat.setBackground(Color.WHITE);
		tfChat.setBorder(new MatteBorder(1, 1, 1, 1, Color.WHITE));
		
		btnSend.setBackground(new Color(255, 0, 0, 0));
		
		pnlSend.add(lblWhisper, BorderLayout.WEST);
		pnlSend.add(tfChat, BorderLayout.CENTER);
		pnlSend.add(btnSend, BorderLayout.EAST);
		pnlSend.setBackground(Color.WHITE);

		pnlCenter = new JPanel(new BorderLayout());
//		pnlCenter.setBackground(new Color(206, 255, 199));
		pnlCenter.setBackground(colorTheme);

		pnlCenter.add(pnlRoomInfo, BorderLayout.NORTH);
		pnlCenter.add(pane, BorderLayout.CENTER);
		pnlCenter.add(pnlSend, BorderLayout.SOUTH);

		pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBackground(colorTheme);
		pnlMain.add(pnlCenter, BorderLayout.CENTER);
		pnlMain.add(pnlEast, BorderLayout.EAST);

		add(pnlMain);
	}

	public void updateRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
		isHost = chatRoom.getHostId() == myUser.getId();
		
		pnlUserList.removeAll();
		List<User> userList = new ArrayList<User>(chatRoom.getIdUserMap().values());
		userList.sort(new Comparator<User>() {
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
		User chatUser;
		if (isHost) {
			for (int idx = 0; idx < userList.size(); idx++) {
				chatUser = userList.get(idx);
				pnlUserList.add(new ChatRoomUserPanel(this, chatUser, chatUser.equals(myUser),
						chatUser.getId() == chatRoom.getHostId(), out) {
					@Override
					protected void init() {
						super.init();
						setHost();
					}
				});
			}
		} else {
			for (int idx = 0; idx < userList.size(); idx++) {
				chatUser = userList.get(idx);
				pnlUserList.add(new ChatRoomUserPanel(this, chatUser, chatUser.equals(myUser),
						chatUser.getId() == chatRoom.getHostId(), out));
			}
		}
		
		roomInfo = chatRoom.getInfo();
		theme = UIUtil.ROOM_COLORS[roomInfo.getColorNum()];
		pnlCenter.setBackground(theme);
		pnlMain.setBackground(theme);
		pnlChat.setBackground(theme);
		
		revalidate();
		pnlUserList.updateUI();

		lblrootName.setText(chatRoom.getInfo().getTitle());
		lblUser.setText(String.valueOf(chatRoom.getIdUserMap().size()));
		lblTotal.setText("/ " + String.valueOf(chatRoom.getInfo().getMaxUser()));
		lblSetting.setVisible(isHost);
	}

	private void addListeners() {
		WindowListener wListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				ReqData reqData;
				int answer = JOptionPane.showConfirmDialog(null, "대기실로 돌아가시겠습니까?", "질문", JOptionPane.YES_NO_OPTION);
				if (answer == 0) {
					try {
						reqData = new ReqData(ReqData.EXIT_ROOM);
						out.writeObject(reqData);
						out.flush();
						out.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		};
		this.addWindowListener(wListener);

		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					ReqData reqData;
					if (lblWhisper.getUserId() == 0) {
						// 메시지 보내기 프로토콜
						if (!(tfChat.getText().length() == 0)) {
							reqData = new ReqData(ReqData.SEND_MSG, tfChat.getText());
							System.out.println(tfChat.getText());
							out.writeObject(reqData);
							out.flush();
							out.reset();
							tfChat.setText("");
							tfChat.requestFocus();

						} else {
							// 실행X
						}
					} else {
						// 귓속말 보내기 프로토콜
						reqData = new ReqData(ReqData.SEND_MSG_TO, lblWhisper.getUserId(), tfChat.getText());
						out.writeObject(reqData);
						out.flush();
						out.reset();
						tfChat.setText("");
						tfChat.requestFocus();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		btnSend.addActionListener(aListener);
		tfChat.addActionListener(aListener);

		MouseListener mListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				ReqData reqData;

				if (me.getSource() == lblPeople) {
					if (pnlEast.isVisible()) {
						pnlEast.setVisible(false);
						setSize(510, 500);
					} else {
						pnlEast.setVisible(true);
						setSize(750, 500);
					}
				}
				if (me.getSource() == lblSetting) {
					new RoomMakeForm(ChatRoomForm.this);
				}
				if (me.getSource() == lblInvite) {
					try {
						reqData = new ReqData(ReqData.SHOW_INVITE_USER);
						out.writeObject(reqData);
						out.flush();
						out.reset();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (me.getSource() == lblFriend) {
					try {
						reqData = new ReqData(ReqData.SHOW_BOOKMARKS);
						out.writeObject(reqData);
						out.flush();
						out.reset();

					} catch (IOException e) {
						e.printStackTrace();
					}
					// new 즐겨찾기 폼

				}

				if (me.getSource() == lblExit) {
					int answer = JOptionPane.showConfirmDialog(null, "대기실로 돌아가시겠습니까?", "질문", JOptionPane.YES_NO_OPTION);
					if (answer == 0) {
						try {
							reqData = new ReqData(ReqData.EXIT_ROOM);
							out.writeObject(reqData);
							out.flush();
							out.reset();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				if (me.getSource() == lblWhisper) {
					lblWhisper.whisperOff();
				}
			}

			@Override
			public void mouseEntered(MouseEvent me) {
				if (me.getSource() == lblPeople) {
					lblPeople.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else if (me.getSource() == lblFriend) {
					lblFriend.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else if (me.getSource() == lblInvite) {
					lblInvite.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else if (me.getSource() == lblExit) {
					lblExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					lblSetting.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			}
		};
		lblPeople.addMouseListener(mListener);
		lblFriend.addMouseListener(mListener);
		lblInvite.addMouseListener(mListener);
		lblExit.addMouseListener(mListener);
		lblSetting.addMouseListener(mListener);
		lblWhisper.addMouseListener(mListener);

	}

	private void showFrame() {
		setTitle("chat Room");
		setSize(750, 500);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}

	class RoundedButton extends JButton {

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
//    	super(action);
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

	public void scroll() {
		pane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (isScroll == true) {
					pane.getVerticalScrollBar().setValue(e.getAdjustable().getMaximum());
				}
				isScroll = false;

			}
		});
	}

	@Override
	public void infoMessage(Message message) {
		isScroll = true;
		pnlBox.add(new MessagePanel(this, message, MessagePanel.INFO, false));
		pnlBox.updateUI();
		scroll();
	}

	public void addMessage(Message message) {
		isScroll = true;
		pnlBox.add(new MessagePanel(this, message, MessagePanel.CHAT, message.getFrom().equals(myUser)));
		pnlBox.updateUI();
		scroll();
	}

	public void whisper(Message message) {
		isScroll = true;
		pnlBox.add(new MessagePanel(this, message, MessagePanel.WHISPER, false));
		pnlBox.updateUI();
		scroll();
	}

	public void whisperReturn(Message message, String whisperTo) {
		isScroll = true;
		pnlBox.add(new MessagePanel(this, message, MessagePanel.WHISPER, true, whisperTo));
		pnlBox.updateUI();
		scroll();
	}

	@Override
	public void whisperOn(User whisperTo) {
		lblWhisper.whisperOn(whisperTo);
	}

	@Override
	public void whisperOff() {
		lblWhisper.whisperOff();
	}

}