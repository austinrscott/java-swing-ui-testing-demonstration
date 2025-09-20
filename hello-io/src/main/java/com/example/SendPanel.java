package com.example;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executor;

public class SendPanel extends JPanel {

    private final JTextField userIdField = new JTextField(16);
    private final JTextField amountField = new JTextField(8);
    private final JButton sendButton = new JButton("Send");
    private final JLabel statusLabel = new JLabel("Idle");

    private final SendPresenter presenter;

    public SendPanel(RpcClient rpcClient, Executor executor) {
        super(new GridBagLayout());
        this.presenter = new SendPresenter(
                rpcClient,
                executor,
                java.time.Clock.systemUTC(),
                new UiListener()
        );
        buildUi();
        wire();
    }

    public SendPanel() {
        this(
                // Default placeholder client (can be replaced in Main):
                payload -> RpcClient.Result.ok("Mocked OK for " + payload.get("userId")),
                Runnable::run // Direct executor for simplicity; replace with a background executor in real app
        );
    }

    private void buildUi() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userIdLabel = new JLabel("User ID:");
        JLabel amountLabel = new JLabel("Amount:");

        userIdField.setName("userIdField");
        amountField.setName("amountField");
        sendButton.setName("sendButton");
        statusLabel.setName("statusLabel");

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; add(userIdLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; add(userIdField, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; add(amountLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; add(amountField, gbc);

        // Row 2
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(sendButton);
        buttons.add(statusLabel);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1; add(buttons, gbc);
    }

    private void wire() {
        sendButton.addActionListener(e -> {
            clearErrorStyles();
            presenter.submit(userIdField.getText(), amountField.getText());
        });
    }

    private void clearErrorStyles() {
        userIdField.setBackground(UIManager.getColor("TextField.background"));
        amountField.setBackground(UIManager.getColor("TextField.background"));
    }

    private void markError(String fieldName) {
        switch (fieldName) {
            case "userId" -> userIdField.setBackground(new Color(255, 230, 230));
            case "amount" -> amountField.setBackground(new Color(255, 230, 230));
        }
    }

    private class UiListener implements SendPresenter.Listener {
        @Override public void onIdle() {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Idle");
                sendButton.setEnabled(true);
            });
        }

        @Override public void onSending() {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Sending...");
                sendButton.setEnabled(false);
            });
        }

        @Override public void onSuccess(String message) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(message != null ? message : "Success");
                sendButton.setEnabled(true);
            });
        }

        @Override public void onError(String message) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(message != null ? message : "Error");
                sendButton.setEnabled(true);
            });
        }

        @Override public void onValidationError(String field, String message) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(message);
                if (field != null) markError(field);
            });
        }
    }
}