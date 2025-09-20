package com.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class TriadPanel extends JPanel {

    private final JTextField aField = new JTextField(6);
    private final JTextField bField = new JTextField(6);
    private final JTextField cField = new JTextField(6);
    private final JSpinner totalSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 10_000, 1));

    private final TriadPresenter presenter;

    public TriadPanel() {
        super(new GridBagLayout());
        this.presenter = new TriadPresenter(
                new TriadCalculator(),
                new UiListener(),
                (int) totalSpinner.getValue(),
                new TriadCalculator.Values(34, 33, 33)
        );
        buildUi();
        wireBindings();
    }

    private void buildUi() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel totalLabel = new JLabel("Total:");
        JLabel aLabel = new JLabel("A:");
        JLabel bLabel = new JLabel("B:");
        JLabel cLabel = new JLabel("C:");

        aField.setName("fieldA");
        bField.setName("fieldB");
        cField.setName("fieldC");
        totalSpinner.setName("totalSpinner");

        // Row 0: Total
        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0; add(totalLabel, gbc);
        gbc.gridx=1; gbc.gridy=0; gbc.weightx=1; add(totalSpinner, gbc);

        // Row 1: A
        gbc.gridx=0; gbc.gridy=1; gbc.weightx=0; add(aLabel, gbc);
        gbc.gridx=1; gbc.gridy=1; gbc.weightx=1; add(aField, gbc);

        // Row 2: B
        gbc.gridx=0; gbc.gridy=2; gbc.weightx=0; add(bLabel, gbc);
        gbc.gridx=1; gbc.gridy=2; gbc.weightx=1; add(bField, gbc);

        // Row 3: C
        gbc.gridx=0; gbc.gridy=3; gbc.weightx=0; add(cLabel, gbc);
        gbc.gridx=1; gbc.gridy=3; gbc.weightx=1; add(cField, gbc);
    }

    private void wireBindings() {
        setFieldsFrom(presenter.currentValues());

        // User edits
        addChangeListener(aField, () -> presenter.onFieldEdited(TriadCalculator.Field.A, aField.getText()));
        addChangeListener(bField, () -> presenter.onFieldEdited(TriadCalculator.Field.B, bField.getText()));
        addChangeListener(cField, () -> presenter.onFieldEdited(TriadCalculator.Field.C, cField.getText()));

        totalSpinner.addChangeListener(e -> presenter.setTotal((int) totalSpinner.getValue()));
    }

    private void addChangeListener(JTextField field, Runnable onChange) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
        });
    }

    private void setFieldsFrom(TriadCalculator.Values v) {
        presenter.beginProgrammaticUpdate();
        try {
            aField.setText(String.valueOf(v.a));
            bField.setText(String.valueOf(v.b));
            cField.setText(String.valueOf(v.c));
            int spinnerVal = (int) totalSpinner.getValue();
            if (spinnerVal != presenter.currentTotal()) {
                totalSpinner.setValue(presenter.currentTotal());
            }
            clearErrorStyles();
        } finally {
            presenter.endProgrammaticUpdate();
        }
    }

    private void clearErrorStyles() {
        aField.setBackground(UIManager.getColor("TextField.background"));
        bField.setBackground(UIManager.getColor("TextField.background"));
        cField.setBackground(UIManager.getColor("TextField.background"));
    }

    private void markError(TriadCalculator.Field field) {
        JTextField f = switch (field) {
            case A -> aField;
            case B -> bField;
            case C -> cField;
        };
        f.setBackground(new Color(255, 230, 230));
    }

    private class UiListener implements TriadPresenter.Listener {
        @Override public void onValuesChanged(TriadCalculator.Values values) {
            SwingUtilities.invokeLater(() -> setFieldsFrom(values));
        }
        @Override public void onValidationError(TriadCalculator.Field field, String message) {
            SwingUtilities.invokeLater(() -> markError(field));
            // Optional: tooltip or status label
        }
        @Override public void onTotalChanged(int total) {
            SwingUtilities.invokeLater(() -> totalSpinner.setValue(total));
        }
    }
}