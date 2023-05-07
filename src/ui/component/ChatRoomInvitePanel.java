package ui.component;


import model.ReqData;
import model.User;
import ui.ChatRoomForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ChatRoomInvitePanel extends UserPanel {
    //    private JMenuItem miInvite;
    private InviteButton inviteButton;
//    private JDialog jDialog;
//    private ChatRoomForm owner;


    // 초대 패널
    public ChatRoomInvitePanel(JDialog jDialog, ChatRoomForm owner, User user, boolean isMine) {
        super(owner, user, isMine);
        this.jDialog = jDialog;
//        this.owner = owner;
        addInviteBtn(false);
    }

    // 즐찾 패널
    public ChatRoomInvitePanel(JDialog jDialog, ChatRoomForm owner, User user, boolean isMine, String menuText) {
        super(jDialog, owner, user, isMine, menuText);
        addInviteBtn(true);
    }

    private void addInviteBtn(boolean isBookmark) {
        inviteButton = new InviteButton(isBookmark);
        add(inviteButton);
        inviteButton.addActionListener(this);
    }

    //    @Override
//    protected void init() {
//        super.init();
//        inviteButton = new InviteButton();
////        miInvite = new JMenuItem("초대하기");
////        pMenu.add(miInvite);
//    }
//    @Override
//    protected void setDisplay() {
//        super.setDisplay();
//        add(inviteButton);
//
//    }
//
//    @Override
//    protected void addListeners() {
//        super.addListeners();
//        inviteButton.addActionListener(this);
//
////        miInvite.addActionListener(this);
//    }
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if(e.getSource() == miWhisper) {
//            owner.whisperOn(user);
            jDialog.dispose();
        }

        if(e.getSource() == inviteButton) {
            inviteButton.invite(owner.getOut(), user);
            JOptionPane.showMessageDialog(null, "초대장 발송 완료!");
        }
    }
}
