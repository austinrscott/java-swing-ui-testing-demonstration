package com.example;

import java.util.Objects;

/**
 * Pure calculation logic for a trio of integers (A, B, C) that must always sum to a fixed total.
 * - No Swing, no threading, no I/O: easy to unit test.
 * - Deterministic policy (first iteration):
 *   * Edit A  -> adjust B, keep C
 *   * Edit B  -> adjust C, keep A
 *   * Edit C  -> adjust A, keep B
 * - Values are clamped to be non-negative and never exceed the total.
 *
 * Teaching points:
 * - Keep business rules separate from the UI to test quickly and deterministically.
 * - Return immutable value objects (Values) to simplify reasoning and equality checks.
 */

public final class TriadCalculator {

    public enum Field { A, B, C }

    /**
     * Immutable triple of non-negative integers.
     * Using a tiny value class instead of a Map keeps the code type-safe and self-documenting.
     */
    public static final class Values {
        public final int a, b, c;

        public Values(int a, int b, int c) {
            if (a < 0 || b < 0 || c < 0) throw new IllegalArgumentException("Values must be >= 0");
            this.a = a; this.b = b; this.c = c;
        }

        @Override public String toString() { return "Values{" + a + "," + b + "," + c + "}"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Values v)) return false;
            return a == v.a && b == v.b && c == v.c;
        }
        @Override public int hashCode() { return Objects.hash(a, b, c); }
    }

    /**
     * Adjusts the triple to satisfy A + B + C == total after one field is edited.
     * - The edited field is clamped to [0, total].
     * - Exactly one other field is adjusted; the third is kept constant where possible.
     *
     * This method contains no UI or threading and is safe for unit testing.
     */

    public Values adjust(Values current, Field edited, int newValue, int total) {
        if (total < 0) throw new IllegalArgumentException("total must be >= 0");
        int a = current.a, b = current.b, c = current.c;

        // Clamp edited value to [0, total]
        int e = Math.max(0, Math.min(newValue, total));

        return switch (edited) {
            case A -> solve(e, b, c, total, "A");
            case B -> solve(a, e, c, total, "B");
            case C -> solve(a, b, e, total, "C");
        };
    }

    // Internal helper: depending on which is edited, we adjust the "next" field and try to keep the "remaining" field.
    // For edited A: (A=e, adjust B, keep C)
    // For edited B: (B=e, adjust C, keep A)
    // For edited C: (C=e, adjust A, keep B)
    private Values solve(int a, int b, int c, int total, String edited) {
        switch (edited) {
            case "A": {
                int newA = a;
                int keepC = c;
                int newB = total - newA - keepC;
                if (newB < 0) {
                    newB = 0;
                    int newC = total - newA - newB;
                    if (newC < 0) newC = 0;
                    return new Values(newA, newB, newC);
                }
                return new Values(newA, newB, keepC);
            }
            case "B": {
                int newB = b;
                int keepA = a;
                int newC = total - keepA - newB;
                if (newC < 0) {
                    newC = 0;
                    int newA = total - newB - newC;
                    if (newA < 0) newA = 0;
                    return new Values(newA, newB, newC);
                }
                return new Values(keepA, newB, newC);
            }
            case "C": {
                int newC = c;
                int keepB = b;
                int newA = total - keepB - newC;
                if (newA < 0) {
                    newA = 0;
                    int newB = total - newA - newC;
                    if (newB < 0) newB = 0;
                    return new Values(newA, newB, newC);
                }
                return new Values(newA, keepB, newC);
            }

            default:
                throw new IllegalStateException("Unexpected edited flag");
        }
    }
}