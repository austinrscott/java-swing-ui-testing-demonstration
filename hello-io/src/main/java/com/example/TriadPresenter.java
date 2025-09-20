package com.example;

import java.util.Objects;
import java.util.function.Consumer;

public final class TriadPresenter {

    public interface Listener {
        void onValuesChanged(TriadCalculator.Values values);
        void onValidationError(TriadCalculator.Field field, String message);
        void onTotalChanged(int total);
    }

    private final TriadCalculator calculator;
    private final Listener listener;
    private int total;
    private TriadCalculator.Values values;
    private boolean programmaticUpdate = false;

    public TriadPresenter(TriadCalculator calculator, Listener listener, int initialTotal, TriadCalculator.Values initialValues) {
        this.calculator = Objects.requireNonNull(calculator);
        this.listener = Objects.requireNonNull(listener);
        if (initialTotal < 0) throw new IllegalArgumentException("total must be >= 0");
        this.total = initialTotal;
        this.values = Objects.requireNonNull(initialValues);
        emit(l -> l.onTotalChanged(total));
        emit(l -> l.onValuesChanged(values));
    }

    public void setTotal(int newTotal) {
        if (newTotal < 0) {
            emit(l -> l.onValidationError(null, "Total must be >= 0"));
            return;
        }
        this.total = newTotal;
        // Re-adjust keeping the last edited field concept isn’t tracked here; we re-fit by editing A with the same value.
        values = calculator.adjust(values, TriadCalculator.Field.A, values.a, newTotal);
        emit(l -> l.onTotalChanged(total));
        emit(l -> l.onValuesChanged(values));
    }

    public void onFieldEdited(TriadCalculator.Field field, String text) {
        if (programmaticUpdate) return;
        Integer parsed = parseNonNegativeInt(text);
        if (parsed == null) {
            emit(l -> l.onValidationError(field, "Please enter a whole number >= 0"));
            return;
        }
        values = calculator.adjust(values, field, parsed, total);
        emit(l -> l.onValuesChanged(values));
    }

    public TriadCalculator.Values currentValues() {
        return values;
    }

    public int currentTotal() {
        return total;
    }

    // For the view to call before it sets text programmatically, to avoid loops:
    public void beginProgrammaticUpdate() { programmaticUpdate = true; }
    public void endProgrammaticUpdate() { programmaticUpdate = false; }

    private Integer parseNonNegativeInt(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return v < 0 ? null : v;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void emit(Consumer<Listener> action) { action.accept(listener); }
}