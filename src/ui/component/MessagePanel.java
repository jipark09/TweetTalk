package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import model.Message;
import model.User;
import ui.ChatRoomForm;
import ui.Talkable;
import util.UIUtil;

public class MessagePanel extends JPanel implements ActionListener {
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("a hh:mm");

	public static final int CHAT = 0;
	public static final int WHISPER = 1;
	public static final int INFO = 2;

	private Talkable owner;
//	private ChatRoomForm chatRoomForm;
	private boolean isMine;

	private String to;

	private Message message;
	private User from;
	private int messageType;

	private JLabel lblImg;
	private JLabel lblName;
	private JTextArea taMessage;
	private JLabel lblTime;

	private PopupMenu pMenu;
	private MenuItem miWhisper;
	
	public static final Font MESSAGE_FONT = new Font("맑은 고딕", Font.BOLD, 13);

	public MessagePanel(Talkable owner, Message message, int messageType, boolean isMine) {
		this.owner = owner;
		this.message = message;
		this.messageType = messageType;
		this.isMine = isMine;
		from = message.getFrom();

		init();
		setDisplay();
		addListeners();
	}

	public MessagePanel(Talkable owner, Message message, int messageType, boolean isMine, String to) {
		this.owner = owner;
		this.message = message;
		this.messageType = messageType;
		this.isMine = isMine;
		from = message.getFrom();
		this.to = to;

		init();
		setDisplay();
		addListeners();
	}

	private void init() {
		setLayout(new BorderLayout());
		setOpaque(false);

//		Font font = new Font("맑은 고딕", Font.BOLD, 13);

		lblImg = new JLabel();
		lblImg.setVerticalAlignment(JLabel.TOP);

		lblName = new JLabel();
		lblName.setFont(MESSAGE_FONT);
		taMessage = new JTextArea(message.getContent());
		taMessage.setFont(MESSAGE_FONT);
//		taMessage.setOpaque(false);
//		taMessage.setBackground(new Color(150, 0, 0, 0));
//		if(owner instanceof ChatRoomForm) {
//			ChatRoomForm chatRoomForm = (ChatRoomForm) owner;
//			taMessage.setBackground(chatRoomForm.getTheme());
//		}
		taMessage.setBorder(new EmptyBorder(3, 5, 3, 5));
		taMessage.setLineWrap(true);
		taMessage.setWrapStyleWord(true);
		taMessage.setEditable(false);

		String time = TIME_FORMAT.format(message.getTime());
		lblTime = new JLabel(time);
		lblTime.setVerticalAlignment(JLabel.BOTTOM);
		lblTime.setFont(MESSAGE_FONT);

		pMenu = new PopupMenu();
		miWhisper = new MenuItem("귓속말하기");
	}

	private void setDisplay() {
		int max = 27;

		JPanel pnlPadding = new JPanel();
		pnlPadding.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlPadding.setBackground(new Color(255, 0, 0, 0));

		JPanel pnlImg = new JPanel(new BorderLayout());
		pnlImg.add(lblImg, BorderLayout.NORTH);
		pnlImg.setOpaque(false);
		pnlImg.setBorder(new EmptyBorder(0, 0, 0, 10));

		JPanel pnlText = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				int width = getWidth();
				int height = getHeight();
				g.setColor(getBackground());
				g.fillRoundRect(0, 0, width, height, 10, 10);
			}
		};
		pnlText.add(taMessage);
		pnlText.setBorder(new EmptyBorder(0, 5, 0, 5));
		pnlText.setOpaque(false);

		JPanel pnlName = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
		pnlName.add(lblName);
		pnlName.setOpaque(false);
		pnlName.setBorder(new EmptyBorder(0, 50, 0, 5));
		
		JPanel pnlTime = new JPanel(new BorderLayout());
		pnlTime.add(lblTime, BorderLayout.SOUTH);
		pnlTime.setOpaque(false);
		pnlTime.setBorder(new EmptyBorder(0, 5, 0, 5));
		//pnlTime.add(new JPanel(), BorderLayout.CENTER);

		String strFrom = from.getProfile().getNickname();
		JPanel pnlContent = new JPanel(new BorderLayout());
		pnlContent.setOpaque(false);

		if (messageType == INFO) {
//			taMessage.setBackground(chatRoomForm.getTheme());
			
//			JPanel pnl = new JPanel();
//			pnl.setOpaque(false);
//			pnl.add(taMessage);
//			pnlPadding.add(pnl);
			JLabel lblInfo = new JLabel("<< " + message.getFrom().getProfile().getNickname() + "님이 " +
					message.getContent() + "하셨습니다 >>", JLabel.CENTER);
			lblInfo.setFont(MESSAGE_FONT);
			pnlPadding.add(lblInfo);
//			taMessage.setText("<< " + message.getFrom().getProfile().getNickname() + "님이 " +
//					message.getContent() + "하셨습니다 >>"
//			);
		}
		
		if (messageType == CHAT) {
			taMessage.setOpaque(false);
			lblName.setText(strFrom);
			if (isMine) {
				pnlPadding.setLayout(new FlowLayout(FlowLayout.RIGHT));
//				pnlPadding.add(pnlTime);
				pnlPadding.add(pnlContent);

				pnlContent.add(pnlTime, BorderLayout.WEST);
				pnlContent.add(pnlText, BorderLayout.CENTER);

				pnlText.setBackground(new Color(173, 205, 234));

				max = 25;
			} else {
				pnlPadding.setLayout(new FlowLayout(FlowLayout.LEFT));
//				pnlPadding.add(pnlImg, BorderLayout.WEST);
				pnlPadding.add(pnlContent);
//				pnlPadding.add(pnlTime, BorderLayout.EAST);

				pnlContent.add(pnlImg, BorderLayout.WEST);
				pnlContent.add(pnlName, BorderLayout.NORTH);
				pnlContent.add(pnlText, BorderLayout.CENTER);
				pnlContent.add(pnlTime, BorderLayout.EAST);

				lblImg.setIcon(UIUtil.getProfileImage(from.getProfile().getImgNum(), 40, 40));

				//pnlContent.setBackground(Color.WHITE);
				pnlText.setBackground(Color.WHITE);

				max = 22;
				
				// 왕관 추가
				if(owner instanceof ChatRoomForm && ((ChatRoomForm)owner).isHost(from.getId())) {
					JLabel lblCrown = new JLabel();
					lblCrown.setIcon(UIUtil.changeImageIcon("crown.png", 17, 17));
					pnlName.add(lblCrown);
				}
			}
		} 

		if (messageType == WHISPER) {
			taMessage.setOpaque(false);
			lblName.setForeground(Color.WHITE);
			taMessage.setForeground(Color.WHITE);
			lblTime.setForeground(Color.WHITE);

			if (isMine) {
				lblName.setText("@" + to + " : ");
			} else {
				lblName.setText(strFrom + " : ");
			}
			pnlText.add(lblName);
			pnlText.add(taMessage);
			pnlText.add(pnlTime);
			
			pnlText.setBackground(new Color(125, 139, 139, 139));
			
			pnlPadding.add(pnlText);
			
			max = 18;
		}
		add(pnlPadding, BorderLayout.CENTER);
		
		int length = taMessage.getText().length();
		taMessage.setColumns((length < max ? length : max) + (messageType == INFO ? 0 : 1));
	}

	private void addListeners() {
		if (!isMine) {
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent me) {
					showPopup(me);
				}

				@Override
				public void mouseReleased(MouseEvent me) {
					showPopup(me);
				}
			});
		}
		miWhisper.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miWhisper) {
			owner.whisperOn(from);
		}
	}

	private void showPopup(MouseEvent me) {
		if (me.isPopupTrigger()) {
			pMenu.show(this, me.getX(), me.getY());
		}
	}

	public Talkable getOwner() {
		return owner;
	}

	public User getUser() {
		return from;
	}
}