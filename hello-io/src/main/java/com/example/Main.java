package com.example;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            JFrame frame = new JFrame("UI Demo");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Triad", new TriadPanel());

            // Real XML-RPC client wired to Python server
            RpcClient xmlRpcClient = new ApacheXmlRpcClient("http://127.0.0.1:7777");
            ExecutorService background = Executors.newSingleThreadExecutor();
            tabs.addTab("Send XMLRPC", new SendPanel(xmlRpcClient, background));

            frame.setContentPane(tabs);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Optional: add a shutdown hook to cleanly stop the executor
            Runtime.getRuntime().addShutdownHook(new Thread(background::shutdown));
        });
    }
}