package com.example;

import java.util.Objects;

public final class TriadCalculator {

    public enum Field { A, B, C }

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
     * Adjusts the triple so that:
     * - The edited field is set to the (possibly clamped) new value.
     * - Exactly one other field is adjusted.
     * - The remaining field is kept as constant as possible.
     * Policy:
     *   A edited -> adjust B, keep C
     *   B edited -> adjust C, keep A
     *   C edited -> adjust A, keep B
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