package ui;

import model.User;
import ui.component.ChatRoomInvitePanel;
import ui.component.UserPanel;
import util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class UserListForm extends JDialog {
    private User user;
    private ChatRoomForm chatRoomForm;
    private WaitRoomForm waitRoomForm;

    private JPanel pnlUserList;
    private JPanel pnlIdUserMap;
    private JPanel pnlIdOtherMap;

    private JButton btnSearch;
    private JTextField tfSearch;
    private String bookmarkDelete = "즐겨찾기 제거";

    // 유저 초대 -> ChatRoom
    public UserListForm(ChatRoomForm chatRoomForm, Map<Long, User> idUserMap, User user) {
        super(chatRoomForm, "유저 초대", true);
        this.chatRoomForm = chatRoomForm;
        this.user = user;

        setDisplayInvite();
        updateRoom(idUserMap);
        addLitenersInvite(idUserMap);
        showFrame();
    }
    // 즐겨찾기 -> WaitRoomForm
    public UserListForm(WaitRoomForm waitRoomForm, Map<Long, User> idUserMap, Map<Long, User> idOtherMap, User user) {
        super(waitRoomForm, "즐겨찾기", true);
        this.waitRoomForm = waitRoomForm;
        this.user = user;

        setDisplayBookmark();
        updateWaitRoom(idUserMap, idOtherMap);
        addLitenersBookmarkWait(idUserMap, idOtherMap);
        showFrame();
    }
    // 즐겨찾기 -> ChatRoomForm
    public UserListForm(ChatRoomForm chatRoomForm, Map<Long, User> idUserMap, Map<Long, User> idOtherMap, User user) {
        super(chatRoomForm, "즐겨찾기", true);
        this.chatRoomForm = chatRoomForm;
        this.user = user;

        setDisplayBookmark();
        updateChatRoom(idUserMap, idOtherMap);
        addLitenersBookmarkChat(idUserMap, idOtherMap);
        showFrame();
    }

    public void setDisplayBookmark() {
        JPanel pnlWaitRoom = new JPanel(new BorderLayout());
        JLabel lblWait = new JLabel("   대기실");
        lblWait.setFont(UIUtil.getBoldFont(13));
        lblWait.setBackground(new Color(240, 238, 237, 50));
        lblWait.setBorder(new EmptyBorder(5,0,5,0));

        pnlIdUserMap = new JPanel();
        pnlIdUserMap.setLayout(new BoxLayout(pnlIdUserMap, BoxLayout.Y_AXIS));
        pnlIdUserMap.setBackground(Color.WHITE);

        // 대기실
        pnlWaitRoom.add(lblWait, BorderLayout.NORTH);
        pnlWaitRoom.add(pnlIdUserMap, BorderLayout.CENTER);

        JPanel pnlChatRoom = new JPanel(new BorderLayout());
        pnlChatRoom.setBorder(new MatteBorder(3,0,0,0,Color.WHITE));
        JLabel lblChat = new JLabel("   채팅방");
        lblChat.setFont(UIUtil.getBoldFont(13));
        lblChat.setBackground(new Color(240, 238, 237, 50));
        lblChat.setBorder(new EmptyBorder(5,0,5,0));


        pnlIdOtherMap = new JPanel();
        pnlIdOtherMap.setLayout(new BoxLayout(pnlIdOtherMap, BoxLayout.Y_AXIS));
        pnlIdOtherMap.setBackground(Color.WHITE);

        // 채팅방
        pnlChatRoom.add(lblChat, BorderLayout.NORTH);
        pnlChatRoom.add(pnlIdOtherMap, BorderLayout.CENTER);

        JPanel pnlTotal = new JPanel(new BorderLayout());
        pnlTotal.setBorder(new EmptyBorder(2,2,2,2));
        pnlTotal.add(pnlWaitRoom, BorderLayout.NORTH);
        pnlTotal.add(pnlChatRoom, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(pnlTotal, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(pnlSearch(), BorderLayout.NORTH);// 검색
        add(scroll, BorderLayout.CENTER); // 대기실 + 채팅방


    }
    public void setDisplayInvite() {

        pnlUserList = new JPanel();
//        pnlUserList.setPreferredSize(new Dimension(360,20));
        pnlUserList.setLayout(new BoxLayout(pnlUserList, BoxLayout.Y_AXIS));
        pnlUserList.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(pnlUserList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(pnlSearch(), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

    }

    public JPanel pnlSearch() {
        JPanel pnlSearch = new JPanel(new FlowLayout());
        pnlSearch.setBackground(Color.WHITE);
        tfSearch = new JTextField(23);
        tfSearch.setText(" ");
        tfSearch.setBorder(new TitledBorder(new LineBorder(new Color(242, 242, 242))));

//        btnSearch = new JButton(UIUtil.changeImageIcon("search.png",17,17));
//        btnSearch.setBackground(Color.WHITE);
//        btnSearch.setPreferredSize(new Dimension(23,23));
        btnSearch = new RoundedButton("검색");
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setFont(UIUtil.getBoldFont(13));
        btnSearch.setPreferredSize(new Dimension(45, 30));

        pnlSearch.add(tfSearch);
        pnlSearch.add(btnSearch);

        return pnlSearch;
    }

    private void addLitenersInvite(Map<Long, User> idUserMap) {
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlUserList.removeAll();
                for (User searchUser : idUserMap.values()) {
                    if(searchUser.getProfile().getNickname().contains(tfSearch.getText())) {
                        pnlUserList.add(
                                new ChatRoomInvitePanel(
                                        UserListForm.this,
                                        chatRoomForm,
                                        searchUser,
                                        searchUser.equals(user)
                                )
                        );
                        revalidate();
                        pnlUserList.updateUI();
                    }
                }
            }
        });
    }

    private void addLitenersBookmarkChat(Map<Long, User> idUserMap, Map<Long, User> idOtherMap) {
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlRemoveAll();

                for(User waitUser : idUserMap.values()) {
                    if(waitUser.getProfile().getNickname().contains(tfSearch.getText())) {
                        pnlIdUserMap.add(
                                new ChatRoomInvitePanel(
                                        UserListForm.this,
                                        chatRoomForm,
                                        waitUser,
                                        waitUser.equals(user),
                                        bookmarkDelete
                                )
                        );
                    }
                    friendchatRoomHere(idOtherMap);
                    pnlUpdate(pnlIdUserMap);
                    pnlUpdate(pnlIdOtherMap);
                }
            }
        });
    }
    private void addLitenersBookmarkWait(Map<Long, User> idUserMap, Map<Long, User> idOtherMap) {
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlRemoveAll();

                for(User waitUser : idUserMap.values()) {
                    if(waitUser.getProfile().getNickname().contains(tfSearch.getText())) {
                        pnlIdUserMap.add(
                                new UserPanel(
                                        UserListForm.this,
                                        waitRoomForm,
                                        waitUser,
                                        waitUser.equals(user),
                                        bookmarkDelete
                                )
                        );
                    }
                    friendchatRoomHere(idOtherMap);
                    pnlUpdate(pnlIdUserMap);
                    pnlUpdate(pnlIdOtherMap);
                }
            }
        });
    }

    public void updateChatRoom(Map<Long, User> idUserMap, Map<Long, User> idOtherMap) {
        pnlRemoveAll();

        for(User waitUser : idUserMap.values()) {
            pnlIdUserMap.add(
                    new ChatRoomInvitePanel(
                            this,
                            chatRoomForm,
                            waitUser,
                            waitUser.equals(user),
                            bookmarkDelete
                    )
            );
        }

        pnlUpdate(pnlIdUserMap);
        for(User chatUser : idOtherMap.values()) {
            pnlIdOtherMap.add(
                    new UserPanel(
                            UserListForm.this,
                            chatRoomForm,
                            chatUser,
                            chatUser.equals(user),
                            bookmarkDelete
                    )
            );
        }
        pnlUpdate(pnlIdOtherMap);

    }
    public void updateWaitRoom(Map<Long, User> idUserMap, Map<Long, User> idOtherMap) {
        pnlRemoveAll();

        for(User waitUser : idUserMap.values()) {
            pnlIdUserMap.add(
                    new UserPanel(
                            UserListForm.this,
                            waitRoomForm,
                            waitUser,
                            waitUser.equals(user),
                            bookmarkDelete
                    )
            );
        }
        pnlUpdate(pnlIdUserMap);

        for(User chatUser : idOtherMap.values()) {
            pnlIdOtherMap.add(
                    new UserPanel(
                            UserListForm.this,
                            waitRoomForm,
                            chatUser,
                            chatUser.equals(user),
                            bookmarkDelete
                    )
            );
        }
        pnlUpdate(pnlIdOtherMap);
    }

    private void pnlRemoveAll() {
        pnlIdUserMap.removeAll();
        pnlIdOtherMap.removeAll();
    }
    private void pnlUpdate(JPanel pnl) {
        revalidate();
        pnl.updateUI();

    }

    // 대기실에 있는 유저를 파라미터로 받음(유저 초대)
    public void updateRoom(Map<Long, User> idUserMap) {
        pnlUserList.removeAll();
        for (User searchUser : idUserMap.values()) {
            pnlUserList.add(
                    new ChatRoomInvitePanel(
                            this,
                            chatRoomForm,
                            searchUser,
                            searchUser.equals(user)
                    )
            );
        }
        revalidate();
        pnlUserList.updateUI();
    }

    private void friendchatRoomHere(Map<Long, User> idOtherMap) {
        for(User chatUser : idOtherMap.values()) {
            if(chatUser.getProfile().getNickname().contains(tfSearch.getText())) {
                pnlIdOtherMap.add(
                        new UserPanel(
                                UserListForm.this,
                                chatRoomForm,
                                chatUser,
                                chatUser.equals(user),
                                bookmarkDelete
                        )
                );
            }
            break;
        }
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

    private void showFrame() {
        setSize(340,420);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}