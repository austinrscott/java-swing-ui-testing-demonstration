package com.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Ensure UI is created on the EDT
        SwingUtilities.invokeLater(() -> {
            // Optional: set a stable Look & Feel for consistent visuals
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Fall back to system default if Nimbus isn't available
            }

            JFrame frame = new JFrame("Triad Demo");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setContentPane(new TriadPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}