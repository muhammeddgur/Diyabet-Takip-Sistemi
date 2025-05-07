package org.ui;

import org.model.User;
import javax.swing.*;

public class PatientDashboard extends JFrame{
    private User patient;

    public PatientDashboard(User patient) {
        this.patient = patient;
        setTitle("Hasta Paneli");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        add(new JLabel("Hoşgeldiniz " + patient.getEmail()));
        // Ölçüm girişi ve grafik bileşenleri eklenebilir
    }
}
