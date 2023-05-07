package ui;
import model.ReqData;
import model.ResData;
import model.UserProfile;
import ui.component.LoginPanel;
import util.UIUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class ProfileEditForm extends JDialog {
	private JLabel lblProfilePick;
	private JTextField tfNickname;
	private RoundedButton btnNickChange;
	private RoundedButton btnCancel;

	private ReqData reqData;
	private ObjectOutputStream out;

	private int profileNum = 0;
	private ImageIcon profileImage = UIUtil.changeImageIcon(profileNum + ".png", 220, 220);

	public ProfileEditForm(ObjectOutputStream out, WaitRoomForm owner, String nick, int profileNum) {
		super(owner, "Profile Edit", true);
		this.profileNum = profileNum;
		this.out = out;
		init();
		setDisplay();
		tfNickname.setText(nick);
		tfNickname.selectAll();

		addListeners();
		showFrame();
	}

	public String getTfNickname() {
		return tfNickname.getText();
	}

	public Icon getLblProfilePick() {
		return lblProfilePick.getIcon();
	}

	private void init() {
		lblProfilePick = new JLabel(UIUtil.getProfileImage(profileNum, 220, 220));
		tfNickname = new JTextField(15);
		tfNickname.setBackground(Color.WHITE);
		tfNickname.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));
		btnNickChange = new RoundedButton("수정");
		btnCancel = new RoundedButton("취소");
	}

	private void setDisplay() {
		JPanel pnlMain = new JPanel(new BorderLayout());

		JPanel pnlProfilePick = new JPanel();
		pnlProfilePick.add(lblProfilePick);
		pnlProfilePick.setBackground(Color.white);

		JPanel pnlNickname = new JPanel();
		pnlNickname.add(tfNickname);
		pnlNickname.setBackground(Color.white);

		JPanel pnlCenter = new JPanel(new BorderLayout());
		pnlCenter.add(pnlProfilePick, BorderLayout.CENTER);
		pnlCenter.add(pnlNickname, BorderLayout.SOUTH);
		pnlCenter.setBackground(Color.white);

		JPanel pnlBtn = new JPanel();
		pnlBtn.add(btnNickChange);
		pnlBtn.add(btnCancel);
		pnlBtn.setBackground(Color.white);

		pnlMain.add(pnlCenter, BorderLayout.CENTER);
		pnlMain.add(pnlBtn, BorderLayout.SOUTH);
		pnlMain.setBackground(Color.white);
		pnlMain.setBorder(new EmptyBorder(25, 10, 20, 10));

		add(pnlMain);

	}

	private void addListeners() {
		lblProfilePick.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				new ProfileForm(ProfileEditForm.this);
			}
		});

		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnNickChange || e.getSource() == tfNickname) {
					boolean isOk = true;
					if ((tfNickname.getText().trim().length() == 0)) {
						JOptionPane.showMessageDialog(null, "닉네임을 입력해 주세요.");
						tfNickname.setText("");
						tfNickname.requestFocus();
						isOk = false;
					}
					if (isOk && tfNickname.getText().length() > 10) {
						JOptionPane.showMessageDialog(null, "닉네임은 10자 이하로 설정해주십시오.");
						tfNickname.setText("");
						tfNickname.requestFocus();
						isOk = false;
					}
					if (isOk) {
						reqData = new ReqData(
								ReqData.CHANGE_PROFILE,
								new UserProfile(tfNickname.getText(), profileNum)
								);
						dispose();
						try {
							out.writeObject(reqData);
							out.flush();
							out.reset();
							
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}	
				}
				if (e.getSource() == btnCancel) {
					dispose();
				}
			}
		};
		btnNickChange.addActionListener(aListener);
		btnCancel.addActionListener(aListener);
		tfNickname.addActionListener(aListener);
	}


	private void showFrame() {
		setSize(280, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);

	}

	public void changeProfileImage(int index) {
		profileNum = index;
		profileImage = UIUtil.getProfileImage(profileNum, 220, 220);
		lblProfilePick.setIcon(profileImage);
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