package com.example;

import java.util.Objects;
import java.util.function.Consumer;


/**
 * Presenter/ViewModel for the triad UI.
 * Responsibilities:
 * - Hold current state (total and Values).
 * - Parse and validate user input (string -> int).
 * - Call TriadCalculator to enforce constraints.
 * - Notify the View (Listener) about state changes and validation errors.
 *
 * Teaching points:
 * - MVP/MVVM separation: Presenter has no Swing code; View is thin.
 * - Prevent UI feedback loops via programmaticUpdate guards.
 * - Easy to unit test without any UI tooling.
 */
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

    /**
     * Sets the new total and rebalances values deterministically.
     * Current policy: re-apply A with the same value (clamped) to compute new B/C.
     */
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

    /**
     * Called by the View when the user edits a specific field.
     * - Parses the value
     * - Emits validation errors
     * - Updates values via the calculator and notifies the View
     */
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