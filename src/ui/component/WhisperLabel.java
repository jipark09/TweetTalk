package ui.component;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import model.User;

public class WhisperLabel extends JLabel {
	private User whisperTo;
	
	public WhisperLabel() {
		setBackground(new Color(255, 247, 242));
		setOpaque(true);
		setFont(new Font("맑은 고딕", Font.BOLD, 12));
		//lblWhisper.setSize(new Dimension(13, 13));
		setBorder(new EmptyBorder(0, 5, 0, 5));
		
		whisperOff();
	}
	public void whisperOn(User whisperTo) {
		this.whisperTo = whisperTo;
		setText("@" + whisperTo.getProfile().getNickname());
		setVisible(true);
	}
	public void whisperOff() {
		setText("");
		whisperTo = null;
		setVisible(false);
	}
	public long getUserId() {
		if(whisperTo == null) {
			return 0;
		}
		return whisperTo.getId();
	}
}
