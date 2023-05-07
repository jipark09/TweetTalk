package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;

public class UIUtil {
    public static final String IMGPATH = "img/";
    public static final String PROFILEPATH = "profile";

    // 빨 주 노 초 파 보
    public static final Color[] ROOM_COLORS = new Color[] {
            new Color(255, 203, 203), new Color(255, 224, 140), new Color(206, 242, 121), new Color(178, 235, 244), new Color(217, 229, 255), new Color(225, 225, 225)};

    public static ImageIcon changeImageIcon(String imgName, int width, int height) {
        ImageIcon icon = new ImageIcon(IMGPATH + imgName);
        Image img = icon.getImage();
        Image changeImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(changeImg);
    }

    public static  ImageIcon getProfileImage(int index, int width, int height) {
        return UIUtil.changeImageIcon(PROFILEPATH + "/" + index + ".png", width, height);
    }

    public static Font getBoldFont(int size) {
        Font font = new Font("맑은 고딕", Font.BOLD, size);
        return font;
    }

    public static Font getPlainFont(int size) {
        Font font = new Font("맑은 고딕", Font.PLAIN, size);
        return font;
    }
}