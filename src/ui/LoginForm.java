package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import ui.component.ErrorPanel;
import ui.component.LoginPanel;
import util.UIUtil;

public class LoginForm extends JFrame {
    private LoginPanel loginPanel;
    private ErrorPanel errorPanel;

    private JLabel lblExplain;

    public LoginForm() {
        loginPanel = new LoginPanel(this);

        setDisplay();
        addListeners();
        showFrame();
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    private void setDisplay() {
        lblExplain = new JLabel(UIUtil.changeImageIcon("question.png", 30, 30));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBorder(new EmptyBorder(10,10,0,10));
        pnlTop.setBackground(Color.ORANGE);
        pnlTop.add(lblExplain, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);
        add(loginPanel, BorderLayout.CENTER);
    }

    private void addListeners() {
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JLabel select = (JLabel) e.getSource();

                if(select == lblExplain) {
                    lblExplain.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
        };
        lblExplain.addMouseListener(mouseListener);

        WindowListener wListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                int answer = JOptionPane.showConfirmDialog(
                        null,
                        "종료하시겠습니까?",
                        "종료",
                        JOptionPane.YES_NO_OPTION
                );
                if(answer == 0) {
                    System.exit(0);
                }
            }
        };
        this.addWindowListener(wListener);
    }

    private void showFrame() {
        setTitle("짹톡");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // 사이즈 변경
        setSize(400,630);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

    }

    // 네트워크 오류 IOException에다가 이거 쓰면 될둣
    public void changeErrorPanel(String errorMessage) {
        remove(loginPanel);
        errorPanel = new ErrorPanel(this, errorMessage);
        add(errorPanel);
        revalidate();
        repaint();
    }

    // ErrorPanel에서 돌아가기 버튼 시 로그인 폼으로
    public void changeLoginPanel() {
        remove(errorPanel);
        add(loginPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm();
            }
        });
    }
}
