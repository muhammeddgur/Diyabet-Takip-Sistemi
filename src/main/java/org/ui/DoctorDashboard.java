package org.ui;

import org.model.User;
import javax.swing.*;

public class DoctorDashboard extends JFrame{
    private User doctor;

    public DoctorDashboard(User doctor) {
        this.doctor = doctor;
        setTitle("Doktor Paneli");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        add(new JLabel("Hoşgeldiniz Dr. " + doctor.getEmail()));
        // Tablo, grafik ve filtre kontrolleri eklenebilir
    }
}
