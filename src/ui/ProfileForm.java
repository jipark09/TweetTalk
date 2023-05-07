package ui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import util.UIUtil;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class ProfileForm extends JDialog {
    private LoginForm loginForm;
    private ProfileEditForm profileEditForm;

    private boolean isLoginForm = true;

    public ProfileForm(LoginForm loginForm) {
        super(loginForm, "Profile",true);
        this.loginForm = loginForm;
        setDisplay();
        addListeners();
        showFrame();
    }
    public ProfileForm(ProfileEditForm profileEditForm) {
        super(profileEditForm, "Profile", true);
        this.profileEditForm = profileEditForm;
        isLoginForm = false;
        setDisplay();
        showFrame();
    }

    private void setDisplay() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setViewportView(panel);

        File path = new File("img/" + UIUtil.PROFILEPATH);
        int numberOfProfile = path.list().length;
        int rowLength = numberOfProfile % 3 == 0 ? numberOfProfile / 3 : numberOfProfile / 3 + 1;
        panel.setLayout(new GridLayout(rowLength, 3, 0, 12));

        JLabel[] lblProfile = new JLabel[numberOfProfile];

        for (int i = 0; i < numberOfProfile; i++) {
            lblProfile[i] = new JLabel(UIUtil.getProfileImage(i, 100, 100));
            lblProfile[i].setForeground(Color.WHITE);
            lblProfile[i].setText("" + i);
            int finalI = i;

            // 사진 클릭하면 로그인폼 프로필사진에 붙여짐
            lblProfile[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    JLabel profile = (JLabel) event.getSource();
                    if(isLoginForm) {
                        loginForm.getLoginPanel().changeProfileImage(Integer.parseInt(profile.getText()));
                    } else {
                        profileEditForm.changeProfileImage(Integer.parseInt(profile.getText()));
                    }
                    dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    lblProfile[finalI].setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            });
            panel.add(lblProfile[i]);
        }
        add(scrollPane);

    }

    private void addListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

    }

    private void showFrame() {
        setSize(new Dimension(380, 368));
        setLocation(new Point(906, 279));
//        setLocationRelativeTo();
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setVisible(true);

    }
}
