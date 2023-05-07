package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import model.ChatRoom;
import model.ReqData;
import model.RoomInfo;
import util.UIUtil;

public class RoomMakeForm extends JDialog { // JDialog 로 바꿀거임
	private WaitRoomForm owner;
	private ChatRoomForm chatRoomForm;

	private JLabel lblTitle;
	private JTextField tfTitle;
	// 인원수
	private JLabel lblMaxNum;
	private JComboBox<Integer> maxNum;
	private Vector<Integer> maxNums;
	private JLabel lblChangeColor;
	// 컬러 누르는 레이블
	private JComboBox<Integer> pickColor;
	// 비밀방 체크
	private JCheckBox cbSecretCheck;
	// 비밀번호 패널
	private JLabel lblpw;
	private JPasswordField pw;
	// 열쇠 이미지
	private JLabel pwIcon;
	private JLabel pwIconUnlock;
	private RoundedButton btnOk;
	private RoundedButton btnCancel;
	// 비밀번호 설정 글자 수 알려주는 레이블
	private JLabel lblPwInfo;
	// Lock 마크를 누를 때 사용
	private boolean isPwVisible = true;
	private Dimension sizeOfButton = new Dimension(80, 25);

	private boolean isWaitRoom;
	private int currentUserNum;
	private JCheckBox cbChangePw;
	private JPanel pnlColorArea;
	private JPanel pnlPwArea;

	// WaitRoomForm
	public RoomMakeForm(WaitRoomForm owner) {
		super(owner, "Make a room", true);
		isWaitRoom = true;
		this.owner = owner;

		currentUserNum = 2;

		init();
		setDisplay();
		addListeners();
		showFrame();
	}

	// ChatRoomForm
	public RoomMakeForm(ChatRoomForm chatRoomForm) {
		super(chatRoomForm, "Edit a room", true);
		isWaitRoom = false;
		this.chatRoomForm = chatRoomForm;

		currentUserNum = chatRoomForm.getChatRoom().getIdUserMap().size();
		currentUserNum = currentUserNum < 2 ? 2 : currentUserNum;

		init();
		setDisplay();
		addListeners();
		showFrame();
	}

	public void setTfTitle(JTextField tfTitle) {
		this.tfTitle = tfTitle;
	}

	public JTextField getTfTitle() {
		return tfTitle;
	}

	public JComboBox<Integer> getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(JComboBox<Integer> maxNum) {
		this.maxNum = maxNum;
	}

	public Vector<Integer> getMaxNums() {
		return maxNums;
	}

	public void setMaxNums(Vector<Integer> maxNums) {
		this.maxNums = maxNums;
	}

	public JComboBox<Integer> getPickColor() {
		return pickColor;
	}

	public void setPickColor(JComboBox<Integer> pickColor) {
		this.pickColor = pickColor;
	}

	public JPasswordField getPw() {
		return pw;
	}

	public void setPw(JPasswordField pw) {
		this.pw = pw;
	}

	private void init() {
		lblTitle = new JLabel("제목  ", JLabel.CENTER);
		tfTitle = new JTextField(18);
		tfTitle.setPreferredSize(new Dimension(20, 25));
		tfTitle.setHorizontalAlignment(SwingConstants.CENTER);
		tfTitle.setBackground(Color.WHITE);
		tfTitle.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));
		lblTitle.setFont(UIUtil.getPlainFont(13));

		lblMaxNum = new JLabel("인원 수  ", JLabel.CENTER);
		lblMaxNum.setFont(UIUtil.getPlainFont(13));

		// 인원 수 콤보박스
		maxNums = new Vector<Integer>();
		for (int i = currentUserNum; i <= 10; i++) {
			maxNums.add(i);
		}

		maxNum = new JComboBox<>(maxNums);
		maxNum.setBackground(Color.WHITE);
		maxNum.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));

		// 컬러 바꾸는 콤보박스
		lblChangeColor = new JLabel("방 배경색");
		pickColor = new JComboBox<Integer>(new Integer[] { 0, 1, 2, 3, 4, 5 });
		pickColor.setBackground(Color.WHITE);
		pickColor.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));
		pickColor.setRenderer(new ListCellRenderer<Object>() {
			private JPanel pnl = new JPanel();

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				int colorNum = (int) value;
				pnl.setPreferredSize(new Dimension(20, 20));
				pnl.setBackground(UIUtil.ROOM_COLORS[colorNum]);
				
				return pnl;
			}
		});
		pickColor.setEditor(new BasicComboBoxEditor() {
			private JPanel pnl = new JPanel();
			
			private Object selectedItem;

			@Override
			public Component getEditorComponent() {
				return this.pnl;
			}

			@Override
			public Object getItem() {
				return selectedItem;
			}

			@Override
			public void setItem(Object item) {
				selectedItem = item;
				int colorNum = item instanceof Integer ? (int) item : 0;
				colorNum = colorNum == -1 ? 0 : colorNum;
				pnl.setBackground(UIUtil.ROOM_COLORS[colorNum]);
			}
		});
		pickColor.setEditable(true);

		lblChangeColor.setFont(UIUtil.getPlainFont(13));

		cbSecretCheck = new JCheckBox(" 비밀방 체크");
		cbSecretCheck.setBackground(Color.WHITE);
		cbSecretCheck.setFont(UIUtil.getPlainFont(11));

		lblpw = new JLabel(" 비밀번호 ");
		lblpw.setFont(UIUtil.getPlainFont(13));
		pw = new JPasswordField(10);
		pw.setPreferredSize(new Dimension(20, 20));
		pw.setBackground(new Color(242, 242, 242));
		pw.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));
		pw.setEditable(false);
		pw.setEchoChar('♥');
		ImageIcon iconLock = UIUtil.changeImageIcon("lock.png", 20, 20);
		ImageIcon iconUnlock = UIUtil.changeImageIcon("unlocked.png", 20, 20);

		pwIcon = new JLabel(iconLock);

		pwIconUnlock = new JLabel(iconUnlock);
		pwIconUnlock.setVisible(false);

		btnOk = new RoundedButton("확인");
		btnOk.setPreferredSize(sizeOfButton);
		btnOk.setBackground(new Color(0xD5D5D5));
		btnOk.setBorder(new LineBorder(Color.gray));
		btnOk.setFont(UIUtil.getPlainFont(13));

		btnCancel = new RoundedButton("취소");
		btnCancel.setPreferredSize(sizeOfButton);
		btnCancel.setBackground(new Color(0xD5D5D5));
		btnCancel.setBorder(new LineBorder(Color.gray));
		btnCancel.setFont(UIUtil.getPlainFont(13));

		lblPwInfo = new JLabel("비밀번호는 4자~6자까지 설정 가능합니다", JLabel.CENTER);
		lblPwInfo.setFont(UIUtil.getPlainFont(11));
		lblPwInfo.setForeground(Color.red);
	}

	private void setDisplay() {
		// 테두리 여백 패널
		JPanel pnlMain = new JPanel(new BorderLayout());

		JPanel pnlTitle1 = new JPanel();
		pnlTitle1.add(lblTitle);
		pnlTitle1.setBackground(Color.WHITE);

		JPanel pnlTitle2 = new JPanel();
		pnlTitle2.add(tfTitle);
		pnlTitle2.setBackground(Color.WHITE);

		JPanel pnlMaxNum1 = new JPanel();
		pnlMaxNum1.add(lblMaxNum);
		pnlMaxNum1.setBackground(Color.WHITE);

		JPanel pnlMaxNum2 = new JPanel();
		pnlMaxNum2.add(maxNum);
		pnlMaxNum2.setBackground(Color.WHITE);

		JPanel pnlChangeColor1 = new JPanel();
		pnlChangeColor1.add(lblChangeColor);
		pnlChangeColor1.setBackground(Color.WHITE);

		JPanel pnlChangeColor2 = new JPanel();
		pnlChangeColor2.add(pickColor);
		pnlChangeColor2.setBackground(Color.WHITE);

		JPanel pnlSecret1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlSecret1.add(cbSecretCheck);
		pnlSecret1.setBackground(Color.WHITE);

		JPanel PutPanel = new JPanel();
		PutPanel.add(pwIcon);
		PutPanel.add(pwIconUnlock);
		PutPanel.setBackground(Color.WHITE);

		JPanel pnlSecret2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlSecret2.add(lblpw);
		pnlSecret2.add(pw);
		pnlSecret2.add(PutPanel);
		pnlSecret2.setBackground(Color.WHITE);

		pnlPwArea = new JPanel(new BorderLayout());
		pnlPwArea.add(pnlSecret1, BorderLayout.NORTH);
		pnlPwArea.add(pnlSecret2, BorderLayout.CENTER);
		pnlPwArea.add(lblPwInfo, BorderLayout.SOUTH);
		pnlPwArea.setBackground(Color.WHITE);

		JPanel pnlNorth1 = new JPanel(new BorderLayout());
		pnlNorth1.add(pnlTitle1, BorderLayout.CENTER);
		pnlNorth1.add(pnlTitle2, BorderLayout.SOUTH);
		pnlNorth1.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlNorth1.setBackground(Color.WHITE);

		JPanel pnlNorth2 = new JPanel(new BorderLayout());
		pnlNorth2.add(pnlMaxNum1, BorderLayout.CENTER);
		pnlNorth2.add(pnlMaxNum2, BorderLayout.SOUTH);
		pnlNorth2.setBackground(Color.WHITE);

		JPanel pnlNorth = new JPanel(new BorderLayout());
		pnlNorth.add(pnlNorth1, BorderLayout.CENTER);
		pnlNorth.add(pnlNorth2, BorderLayout.SOUTH);

		pnlColorArea = new JPanel(new BorderLayout());
		pnlColorArea.add(pnlChangeColor1, BorderLayout.NORTH);
		pnlColorArea.add(pnlChangeColor2, BorderLayout.CENTER);
		pnlColorArea.setBorder(new EmptyBorder(7, 7, 7, 7));
		pnlColorArea.setBackground(Color.WHITE);

		JPanel pnlCenter = new JPanel(new BorderLayout());
//		pnlCenter.add(pnlCenter1, BorderLayout.NORTH);
		pnlCenter.add(pnlColorArea, BorderLayout.NORTH);
		pnlCenter.add(pnlPwArea, BorderLayout.CENTER);
		pnlCenter.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlCenter.setBackground(Color.WHITE);

		JPanel pnlSouth = new JPanel();
		pnlSouth.add(btnOk);
		pnlSouth.add(btnCancel);
		pnlSouth.setBackground(Color.WHITE);

		pnlMain.add(pnlNorth, BorderLayout.NORTH);
		pnlMain.add(pnlCenter, BorderLayout.CENTER);
		pnlMain.add(pnlSouth, BorderLayout.SOUTH);
		pnlMain.setBorder(new EmptyBorder(5, 5, 7, 5));
		pnlMain.setBackground(Color.WHITE);

		add(pnlMain, BorderLayout.CENTER);
		
		
		// 방정보 변경창 세팅
		cbChangePw = new JCheckBox("비밀방 설정 변경");
		cbChangePw.setBorder(new EmptyBorder(13, 0, 7, 0));
		if (!isWaitRoom) {
			ChatRoom chatRoom = chatRoomForm.getChatRoom();
			RoomInfo info = chatRoom.getInfo();
			tfTitle.setText(info.getTitle());
			maxNum.setSelectedItem(info.getMaxUser());
			pickColor.setSelectedItem(info.getColorNum());
			cbSecretCheck.setSelected(chatRoom.isPrivate());
			pw.setEditable(chatRoom.isPrivate());
			
			cbChangePw.setBackground(Color.WHITE);
			pnlColorArea.add(cbChangePw, BorderLayout.SOUTH);

//			pnlPwArea.setVisible(false);
			pnlPwArea.updateUI();
		}
	}

	private void addListeners() {
		MouseListener mListener = new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (!cbSecretCheck.isSelected()) {
					isPwVisible = false;
				}
				if (isPwVisible) {
					pw.setEchoChar((char) 0);
					pwIcon.setVisible(false);
					pwIconUnlock.setVisible(true);
					isPwVisible = !isPwVisible;
				} else {
					pw.setEchoChar('♥');
					pwIcon.setVisible(true);
					pwIconUnlock.setVisible(false);
					isPwVisible = !isPwVisible;
				}
			}
		};
		pwIcon.addMouseListener(mListener);
		pwIconUnlock.addMouseListener(mListener);

		// 
		ItemListener iListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if(ie.getSource() == cbSecretCheck) {
					if (cbSecretCheck.isSelected()) {
						pw.setEditable(true);
						pw.setBackground(Color.WHITE);
						pwIconUnlock.setVisible(false);
						pwIcon.setVisible(true);
						pw.requestFocus();
					} else {
						pw.setBackground(new Color(242, 242, 242));
						pw.setText(null);
						pw.setEditable(false);
						pwIcon.setVisible(true);
						pwIconUnlock.setVisible(false);
						isPwVisible = false;
					}
				}
				if(ie.getSource() == cbChangePw) {
					if(cbChangePw.isSelected()) {
//						pnlPwArea.setVisible(true);
//						ChatRoom chatRoom = chatRoomForm.getChatRoom();
//						cbSecertCheck.setSelected(chatRoom.isPrivate());
//						pw.setText("");
//						pw.setEditable(chatRoom.isPrivate());
						RoomMakeForm.this.setSize(new Dimension(265, 436));
					} else {
//						pnlPwArea.setVisible(false);
						ChatRoom chatRoom = chatRoomForm.getChatRoom();
						cbSecretCheck.setSelected(chatRoom.isPrivate());
						pw.setText("");
						pw.setEditable(chatRoom.isPrivate());
						RoomMakeForm.this.setSize(new Dimension(265, 341));
					}
					pnlPwArea.updateUI();
					//pack();
//					System.out.println(pnlPwArea.getSize());
				}
			}
		};
		
		// 체크박스 체크안하면 이미지 변환안하게 만들기
		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnOk) {
					boolean flag = true;
					
					if(!(tfTitle.getText().trim().length() > 0)) {
						JOptionPane.showMessageDialog(null, "방 제목은 1자 이상으로 설정해주세요.");
						flag = false;
						tfTitle.requestFocus();
						tfTitle.selectAll();
					}
					
					if ((chatRoomForm == null && cbSecretCheck.isSelected()) || (cbChangePw.isSelected() && cbSecretCheck.isSelected())) {
						if (pw.getPassword().length < 4 || pw.getPassword().length > 6) {
							JOptionPane.showMessageDialog(null, "비밀번호를 다시 확인해주시기 바랍니다.");
							flag = false;
							pw.setText("");
							pw.requestFocus();
						}
					}

					if (flag) {
						System.out.println("방 제목: " + tfTitle.getText()+ ", 인원 수:" + maxNum.getSelectedItem().toString() + ","
									+ " 컬러:" + pickColor.getSelectedItem() + ", 비밀방 체크: " + isPwVisible + " 패스워드 : " + String.valueOf(pw.getPassword()));

						if(isWaitRoom) {
							waitRoomOut();
						} else {
							chatRoomOut();
						}
						dispose();
					}
				}
				if(e.getSource() == btnCancel) {
					dispose();
				}
			}
		};
		cbSecretCheck.addItemListener(iListener);
		cbChangePw.addItemListener(iListener);
		btnOk.addActionListener(aListener);
		btnCancel.addActionListener(aListener);
	}

	private void waitRoomOut() {
		try {

			ReqData reqData = new ReqData(ReqData.MAKE_ROOM,
					new RoomInfo(tfTitle.getText(), (int) pickColor.getSelectedItem(),
							Integer.parseInt(maxNum.getSelectedItem().toString()), cbSecretCheck.isSelected()),
					cbSecretCheck.isSelected() ? String.valueOf(pw.getPassword()) : null);
			owner.getOut().writeObject(reqData);
			owner.getOut().flush();
			owner.getOut().reset();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private void chatRoomOut() {
		try {

			ReqData reqData = new ReqData(ReqData.CHANGE_ROOM_INFO,
					new RoomInfo(tfTitle.getText(), (int) pickColor.getSelectedItem(),
							Integer.parseInt(maxNum.getSelectedItem().toString()), cbSecretCheck.isSelected()),
					cbChangePw.isSelected() && cbSecretCheck.isSelected() ? String.valueOf(pw.getPassword()) : null);
			chatRoomForm.getOut().writeObject(reqData);
			chatRoomForm.getOut().flush();
			chatRoomForm.getOut().reset();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void showFrame() {
//		pack();
		if(isWaitRoom) {
			setSize(new Dimension(265, 390));
		} else {
			setSize(new Dimension(265, 341));
		}
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
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