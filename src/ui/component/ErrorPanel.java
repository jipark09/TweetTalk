package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ui.LoginForm;
import util.UIUtil;

public class ErrorPanel extends JPanel {

	public ErrorPanel(LoginForm loginForm, String errorMessage) {
		setBackground(Color.ORANGE);
		setLayout(new BorderLayout());

		JPanel pnlTop = new JPanel(new GridLayout(0, 1));
		pnlTop.setBackground(Color.ORANGE);
		pnlTop.setBorder(new EmptyBorder(80,0,0,0));

		// dieBird
		JPanel pnlBird = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlBird.setBackground(Color.ORANGE);
		JLabel lblDieBird = new JLabel(UIUtil.changeImageIcon("deadBird.png",160, 160));
		lblDieBird.setBorder(new EmptyBorder(-10,0,5,0));
		pnlBird.add(lblDieBird);

		// ErrorText
		JPanel pnlText = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlText.setBackground(Color.ORANGE);
		JLabel lblText = new JLabel(UIUtil.changeImageIcon("NetworkError.png",250,90));
		lblText.setBorder(new EmptyBorder(-3,0,0,0));
		pnlText.add(lblText);

		pnlTop.add(pnlBird);
		pnlTop.add(pnlText);

		// btnBack
		JPanel pnlbtn = new JPanel();
		pnlbtn.setBackground(Color.ORANGE);
		JButton btnBack = new JButton("돌아가기");
		btnBack.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
		btnBack.setBackground(Color.WHITE);
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loginForm.changeLoginPanel();
			}
		});
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		});
		pnlbtn.add(btnBack);

		add(pnlTop, BorderLayout.NORTH);
		add(pnlbtn, BorderLayout.CENTER);
	}
}
