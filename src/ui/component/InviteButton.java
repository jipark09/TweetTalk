package ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JButton;

import model.ReqData;
import model.User;

public class InviteButton extends JButton {
	private boolean isBookmark;

	public InviteButton(boolean isBookmark) {
		super("초대하기");
		setFont(new Font("맑은 고딕", Font.BOLD, 13));
		setPreferredSize(new Dimension(70, 30));
		this.isBookmark = isBookmark;
		decorate();
	}
	public void invite(ObjectOutputStream out, User user) {
		int protocol = (isBookmark) ? ReqData.INVITE_USER : ReqData.INVITE_BOOKMARK_USER;

		try {
			ReqData reqData = new ReqData(
					protocol,
					user.getId()
			);
			out.writeObject(reqData);
			out.flush();
			out.reset();

		} catch (IOException e) {
			e.printStackTrace();
		}
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
