package ui.component;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.*;

import model.ReqData;
import model.User;
import ui.Talkable;
import util.UIUtil;

public class ChatRoomUserPanel extends UserPanel {
	private boolean isHost;
	private ObjectOutputStream out;

	private JMenuItem miChangeHost;
	private JMenuItem miKickUser;

	public ChatRoomUserPanel(Talkable owner, User user, boolean isMine, boolean isHost, ObjectOutputStream out) {
		super(owner, user, isMine);
//		this.isHost = isHost;
		this.out = out;

		if (isHost) {
			// 채팅창 유저가 방장 패널을 볼 때 보이는 부분
			add(new JLabel(UIUtil.changeImageIcon("crown.png", 20, 20)));
		}
	}

	public void setHost() {
		miChangeHost = new JMenuItem("방장 위임하기");
		miKickUser = new JMenuItem("강퇴하기");

		// 채팅창 유저가 방장일 때 보이는 부분
		pMenu.add(miChangeHost);
		pMenu.add(miKickUser);

		ActionListener aListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReqData reqData;
				if (e.getSource() == miChangeHost) {
					// 방장변경 프로토콜 301
					try {
						reqData = new ReqData(ReqData.CHANGE_ROOM_HOST, user.getId());
						out.writeObject(reqData);
						out.flush();
						out.reset();

					} catch (IOException ex) {
						ex.printStackTrace();

					}
				}
				if (e.getSource() == miKickUser) {
					// 유저강퇴 프로토콜 303
					try {
						reqData = new ReqData(ReqData.KICK_USER, user.getId());
						out.writeObject(reqData);
						out.flush();
						out.flush();

					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		};

		miChangeHost.addActionListener(aListener);
		miKickUser.addActionListener(aListener);
	}
}