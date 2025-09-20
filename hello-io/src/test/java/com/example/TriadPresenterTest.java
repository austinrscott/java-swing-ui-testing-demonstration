package com.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriadPresenterTest {

    static class RecordingListener implements TriadPresenter.Listener {
        TriadCalculator.Values lastValues;
        Integer lastTotal;
        TriadCalculator.Field lastErrorField;
        String lastErrorMessage;

        @Override public void onValuesChanged(TriadCalculator.Values values) { lastValues = values; }
        @Override public void onValidationError(TriadCalculator.Field field, String message) {
            lastErrorField = field; lastErrorMessage = message;
        }
        @Override public void onTotalChanged(int total) { lastTotal = total; }
    }

    @Test
    void editsUpdateValuesUsingPolicy() {
        var calc = new TriadCalculator();
        var listener = new RecordingListener();
        var presenter = new TriadPresenter(calc, listener, 100, new TriadCalculator.Values(30, 40, 30));

        presenter.onFieldEdited(TriadCalculator.Field.A, "50");
        assertThat(listener.lastValues).isEqualTo(new TriadCalculator.Values(50, 20, 30));

        presenter.onFieldEdited(TriadCalculator.Field.B, "10");
        assertThat(listener.lastValues).isEqualTo(new TriadCalculator.Values(50, 10, 40));
    }

    @Test
    void invalidInputEmitsValidationError_andDoesNotChangeValues() {
        var calc = new TriadCalculator();
        var listener = new RecordingListener();
        var presenter = new TriadPresenter(calc, listener, 100, new TriadCalculator.Values(25, 25, 50));

        presenter.onFieldEdited(TriadCalculator.Field.C, "abc");
        assertThat(listener.lastErrorField).isEqualTo(TriadCalculator.Field.C);
        assertThat(listener.lastValues).isEqualTo(new TriadCalculator.Values(25, 25, 50));
    }

    @Test
    void changingTotalRebalances() {
        var calc = new TriadCalculator();
        var listener = new RecordingListener();
        var presenter = new TriadPresenter(calc, listener, 100, new TriadCalculator.Values(40, 30, 30));

        presenter.setTotal(60);
        assertThat(listener.lastTotal).isEqualTo(60);
        // Using the presenter's simple policy: re-apply A with same value (clamped if needed)
        assertThat(listener.lastValues.a + listener.lastValues.b + listener.lastValues.c).isEqualTo(60);
    }
}
