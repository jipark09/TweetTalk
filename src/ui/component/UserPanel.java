package ui.component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.LineBorder;

import model.ReqData;
import model.User;
import model.UserProfile;
import model.WaitRoom;
import ui.ChatRoomForm;
import ui.WaitRoomForm;
import ui.Talkable;
import util.UIUtil;

public class UserPanel extends JPanel implements ActionListener {

    protected Talkable owner;
    protected User user;
    protected JDialog jDialog;
    private boolean isMine;

    protected JPopupMenu pMenu;
    protected JMenuItem miWhisper;
    private JMenuItem miBookmarkAdd;
    private JMenuItem miBookmarkDelete;
    private JLabel lblName;

    // 즐겨찾기 제거
    public UserPanel(JDialog jDialog, Talkable owner, User user, boolean isMine, String menuText) {
        this.owner = owner;
        this.user = user;
        this.isMine = isMine;
        this.jDialog = jDialog;

        init();
        setDisplay();
        pMenu.remove(miBookmarkAdd);
        miBookmarkDelete = new JMenuItem(menuText);
        pMenu.add(miBookmarkDelete);
        addListeners();
    }

    // 즐겨찾기 추가
    public UserPanel(Talkable owner, User user, boolean isMine) {
        this.owner = owner;
        this.user = user;
        this.isMine = isMine;

        init();
        setDisplay();
        addListeners();
    }

    protected void init() {
        pMenu = new JPopupMenu();
        miWhisper = new JMenuItem("귓속말하기");
        miBookmarkAdd = new JMenuItem("즐겨찾기 추가");
        setMaximumSize(new Dimension(300,65));
        setMinimumSize(new Dimension(265,65));
        setPreferredSize(new Dimension(265,65));
    }

    protected void setDisplay() {
        LayoutManager fLayout = new FlowLayout(FlowLayout.LEFT);

        setBackground(Color.WHITE);
        setLayout(fLayout);
//        setPreferredSize(new Dimension(265, 50));
//        setBorder(new LineBorder(Color.BLACK));

        JPanel pnlProfile = new JPanel(fLayout);
        pnlProfile.setBackground(Color.WHITE);
        JLabel lblProfile = new JLabel(getProfileImage());
        pnlProfile.add(lblProfile);

        JPanel pnlName = new JPanel(fLayout);
        pnlName.setBackground(Color.WHITE);
        lblName = new JLabel(user.getProfile().getNickname());
        pnlName.add(lblName);
//
//        JPanel pnlTotal = new JPanel();
//        pnlTotal.setBackground(Color.WHITE);
//        pnlTotal.add(pnlProfile);
//        pnlTotal.add(pnlName);
//
//        pnlBorder = new JPanel(new BorderLayout());
//        pnlBorder.add(pnlTotal, BorderLayout.WEST);
//        pnlBorder.setBackground(Color.WHITE);

        add(pnlProfile);
        add(pnlName);
//        add(pnlBorder);


        add(pMenu);
        pMenu.add(miWhisper);
        pMenu.add(miBookmarkAdd);
    }

    protected ImageIcon getProfileImage() {
        return UIUtil.getProfileImage(user.getProfile().getImgNum(), 40, 40);
    }

    protected void addListeners() {
        if (!isMine) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent me) {
                    showPopup(me);
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    showPopup(me);
//                    setBackground(Color.WHITE);
                }
            });
        }
        miWhisper.addActionListener(this);
        if(miBookmarkDelete != null) {
            miBookmarkDelete.addActionListener(this);

        }
        if(miBookmarkAdd != null) {
            miBookmarkAdd.addActionListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == miWhisper) {
            owner.whisperOn(user);
            jDialog.dispose();

        }
        if(e.getSource() == miBookmarkAdd) {
            try {
                ReqData reqData = new ReqData(
                        ReqData.ADD_BOOKMARK,
                        user.getId()
                );
                owner.getOut().writeObject(reqData);
                owner.getOut().flush();
                owner.getOut().reset();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if(e.getSource() == miBookmarkDelete) {
            try {
                ReqData reqData = new ReqData(
                        ReqData.REMOVE_BOOKMARK,
                        user.getId()
                );
                owner.getOut().writeObject(reqData);
                owner.getOut().flush();
                owner.getOut().reset();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void showPopup(MouseEvent me) {
        // 조건은 우클릭 + (운영체제에서 정함) 실행시점 (Pressed or Released)
        // isPopupTrigger() : 운영체제에 맞는 Popup 조건을 확인하는 메서드
        // Returns whether or not this mouse event is the popup menutrigger event for
        // the platform.
        if (me.isPopupTrigger()) {
            pMenu.show(this, me.getX(), me.getY());
            // void show(Component invoker, int x, int y)
        }
    }

    public Talkable getOwner() {
        return owner;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserPanel [user=" + user + "]";
    }
}