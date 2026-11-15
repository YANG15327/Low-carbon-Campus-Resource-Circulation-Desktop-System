package com.campus.recycle.ui;

import com.campus.recycle.exception.BusinessException;
import com.campus.recycle.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.BiConsumer;

/** Sign-in and student registration window. */
final class LoginFrame extends JFrame {

    private final AppServices services;
    private final BiConsumer<AppServices, User> onLogin;
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    LoginFrame(AppServices services, BiConsumer<AppServices, User> onLogin) {
        super("低碳校园资源循环流转系统 - 登录");
        this.services = services;
        this.onLogin = onLogin;

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        form.add(new JLabel("用户名"));
        form.add(usernameField);
        form.add(new JLabel("密码"));
        form.add(passwordField);

        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册新账号");
        JPanel actions = new JPanel();
        actions.add(loginButton);
        actions.add(registerButton);

        add(new JLabel("校园闲置物品，让资源循环起来", JLabel.CENTER), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        loginButton.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());
        registerButton.addActionListener(event -> register());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        try {
            User user = services.getUserService().login(usernameField.getText(),
                    new String(passwordField.getPassword()));
            dispose();
            onLogin.accept(services, user);
        } catch (BusinessException e) {
            showError(e);
        }
    }

    private void register() {
        JTextField username = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        JPasswordField confirm = new JPasswordField(18);
        JTextField nickname = new JTextField(18);
        JTextField email = new JTextField(18);
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("用户名"));
        form.add(username);
        form.add(new JLabel("密码（6-32 位，含字母和数字）"));
        form.add(password);
        form.add(new JLabel("确认密码"));
        form.add(confirm);
        form.add(new JLabel("昵称"));
        form.add(nickname);
        form.add(new JLabel("邮箱（选填）"));
        form.add(email);

        int result = JOptionPane.showConfirmDialog(this, form, "注册学生账号",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String rawPassword = new String(password.getPassword());
        if (!rawPassword.equals(new String(confirm.getPassword()))) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致",
                    "注册失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = services.getUserService().register(username.getText(), rawPassword,
                    nickname.getText(), email.getText());
            dispose();
            onLogin.accept(services, user);
        } catch (BusinessException e) {
            showError(e);
        }
    }

    private void showError(BusinessException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "操作失败",
                JOptionPane.WARNING_MESSAGE);
    }
}
