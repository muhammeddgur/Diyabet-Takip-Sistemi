package ui;

import service.AuthenticationService;
import model.User;

import javax.swing.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField tcField;
    private JPasswordField passField;
    private JButton loginBtn;
    private AuthenticationService authService;

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        setTitle("Diyabet Takip - Giriş");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel tcLabel = new JLabel("TC Kimlik:"); tcLabel.setBounds(20,20,80,25);
        tcField = new JTextField(); tcField.setBounds(110,20,150,25);
        JLabel passLabel = new JLabel("Şifre:"); passLabel.setBounds(20,60,80,25);
        passField = new JPasswordField(); passField.setBounds(110,60,150,25);
        loginBtn = new JButton("Giriş"); loginBtn.setBounds(110,100,80,25);

        add(tcLabel); add(tcField);
        add(passLabel); add(passField);
        add(loginBtn);

        loginBtn.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String tc = tcField.getText();
        String pwd = new String(passField.getPassword());
        User user = authService.authenticate(tc, pwd);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Hoşgeldiniz, " + user.getRole());
            dispose();
            if ("DOCTOR".equals(user.getRole())) new DoctorDashboard(user).setVisible(true);
            else new PatientDashboard(user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Hatalı TC veya şifre.");
        }
    }
}
