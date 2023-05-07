package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import client.ClientReceiver;
import model.ReqData;
import model.UserProfile;
import ui.LoginForm;
import ui.ProfileForm;
import util.IOUtil;
import util.UIUtil;

public class LoginPanel extends JPanel {
	private LoginForm loginForm;

	private Socket socket;
	private ReqData reqData;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	private JTextField tfName;
	private JButton btnConnect;
	private String tfNameText = "닉네임을 입력해주세요.";

	private int profileNum = 0;
	private ImageIcon profileImage = getProfileImage();
	private JLabel lblProfile;

	public LoginPanel(LoginForm loginForm) {
		this.loginForm = loginForm;
		setDisplay();
		addListeners();
	}

	public JTextField getTfName() {
		return tfName;
	}

	public int getProfileNum() {
		return profileNum;
	}

	private void setDisplay() {
		setLayout(new BorderLayout());
		setBackground(Color.ORANGE);
		setBorder(new EmptyBorder(10, 30, 20, 30));

		// title
		JPanel pnlTitle = new JPanel(new BorderLayout());
		pnlTitle.setBackground(Color.ORANGE);
		JLabel lblTitle = new JLabel(UIUtil.changeImageIcon("TweetFont.png", 230, 50));
		lblTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
//      lblTitle.setFont(new Font("맑은 고딕", Font.BOLD, 35));
		JLabel lblBird = new JLabel(UIUtil.changeImageIcon("bird.png", 90, 90));
		lblBird.setBorder(new EmptyBorder(15, 0, -3, 0));
		pnlTitle.add(lblTitle, BorderLayout.CENTER);
		pnlTitle.add(lblBird, BorderLayout.SOUTH);

		// profile
		JPanel pnlProfile = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		pnlProfile.setBackground(Color.ORANGE);
		lblProfile = new JLabel(profileImage);
		pnlProfile.add(lblProfile);

		JPanel pnlSouth = new JPanel(new BorderLayout());

		// nickName
		JPanel pnlName = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlName.setBackground(Color.ORANGE);
		tfName = new JTextField(tfNameText);
		tfName.setHorizontalAlignment(SwingConstants.CENTER); // 포인트 가운데
		tfName.setPreferredSize(new Dimension(230, 40));
		tfName.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		tfName.setForeground(Color.LIGHT_GRAY);
		pnlName.add(tfName);

		// connect
		JPanel pnlConnect = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlConnect.setBorder(new EmptyBorder(10, 0, 10, 0));
		pnlConnect.setBackground(Color.ORANGE);
		btnConnect = new JButton("CONNECT");
		btnConnect.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		btnConnect.setBackground(Color.WHITE);
		pnlConnect.add(btnConnect);

		pnlSouth.add(pnlName, BorderLayout.CENTER);
		pnlSouth.add(pnlConnect, BorderLayout.SOUTH);

		add(pnlTitle, BorderLayout.NORTH);
		add(pnlProfile, BorderLayout.CENTER);
		add(pnlSouth, BorderLayout.SOUTH);

	}

	private void addListeners() {
		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isOk = true;
				if ((tfName.getText().equals(tfNameText) || tfName.getText().trim().length() == 0)) {
					JOptionPane.showMessageDialog(LoginPanel.this, "닉네임을 입력해 주세요.");
					tfName.setText("");
					tfName.requestFocus();
					isOk = false;
				}
				if (isOk && tfName.getText().length() > 10) {
					JOptionPane.showMessageDialog(null, "닉네임은 10자 이하로 설정해주십시오.");
					tfName.setText("");
					tfName.requestFocus();
					isOk = false;
				}
				if (isOk) {
					try {
						// 소켓 연결
						socket = new Socket(IOUtil.Host, IOUtil.PORT);
						out = new ObjectOutputStream(socket.getOutputStream());
						in = new ObjectInputStream(socket.getInputStream());

						// 서버에게 로그인 신호 보냄
						reqData = new ReqData(ReqData.LOGIN, new UserProfile(tfName.getText(), profileNum));
						out.writeObject(reqData);
						out.flush();
						out.reset();

						// 서버 응답을 받기 위해 스레드 생성
						new ClientReceiver(loginForm, socket, in, out).start();

					} catch (IOException ex) {
						loginForm.changeErrorPanel("네트워크 오류");
					}
				}
			}
		};
		btnConnect.addActionListener(aListener);
		tfName.addActionListener(aListener);

		tfName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (tfName.getText().equals(tfNameText)) {
					tfName.setText("");
				}
				tfName.setForeground(Color.BLACK);
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (tfName.getText().trim().equals("")) {
					tfName.setText(tfNameText);
					tfName.setForeground(Color.LIGHT_GRAY);
				}
			}
		});

		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getSource() == lblProfile) {
					new ProfileForm(loginForm);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (e.getSource() == lblProfile) {
					lblProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
				if (e.getSource() == btnConnect) {
					btnConnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			}
		};
		lblProfile.addMouseListener(mouseListener);
		btnConnect.addMouseListener(mouseListener);

	}

	public void changeProfileImage(int index) {
		profileNum = index;
		profileImage = getProfileImage();
		lblProfile.setIcon(profileImage);
	}

	private ImageIcon getProfileImage() {
		return UIUtil.getProfileImage(profileNum, 220, 220);
	}
}
