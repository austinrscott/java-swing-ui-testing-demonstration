package com.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriadCalculatorTest {

    private final TriadCalculator calc = new TriadCalculator();
    private static final int T = 100;

    @Test
    void editA_adjustsB_keepsC() {
        TriadCalculator.Values cur = new TriadCalculator.Values(30, 40, 30);
        var out = calc.adjust(cur, TriadCalculator.Field.A, 50, T);
        // keep C=30, so B = 100 - 50 - 30 = 20
        assertThat(out).isEqualTo(new TriadCalculator.Values(50, 20, 30));
    }

    @Test
    void editB_adjustsC_keepsA() {
        TriadCalculator.Values cur = new TriadCalculator.Values(10, 20, 70);
        var out = calc.adjust(cur, TriadCalculator.Field.B, 60, T);
        // keep A=10, so C = 100 - 10 - 60 = 30
        assertThat(out).isEqualTo(new TriadCalculator.Values(10, 60, 30));
    }

    @Test
    void editC_adjustsA_keepsB() {
        TriadCalculator.Values cur = new TriadCalculator.Values(25, 25, 50);
        var out = calc.adjust(cur, TriadCalculator.Field.C, 10, T);
        // keep B=25, so A = 100 - 25 - 10 = 65
        assertThat(out).isEqualTo(new TriadCalculator.Values(65, 25, 10));
    }

    @Test
    void clampWhenEditedExceedsTotal() {
        TriadCalculator.Values cur = new TriadCalculator.Values(10, 20, 70);
        var out = calc.adjust(cur, TriadCalculator.Field.A, 200, T);
        // A clamped to 100, others 0
        assertThat(out).isEqualTo(new TriadCalculator.Values(100, 0, 0));
    }

    @Test
    void clampWhenAdjustedGoesNegative_thenRedistributeToRemaining() {
        TriadCalculator.Values cur = new TriadCalculator.Values(80, 15, 5);
        var out = calc.adjust(cur, TriadCalculator.Field.A, 90, T);
        // keep C=5 -> B = 100 - 90 - 5 = 5 (non-negative) OK
        assertThat(out).isEqualTo(new TriadCalculator.Values(90, 5, 5));

        // harder case: keep C but B would go negative -> clamp B=0 and increase C
        cur = new TriadCalculator.Values(90, 9, 1);
        out = calc.adjust(cur, TriadCalculator.Field.A, 95, T);
        // desired B = 100 - 95 - 1 = 4 (>=0) -> OK, not clamped
        assertThat(out).isEqualTo(new TriadCalculator.Values(95, 4, 1));
    }

    @Test
    void zeroTotalEdgeCase() {
        TriadCalculator.Values cur = new TriadCalculator.Values(0, 0, 0);
        var out = calc.adjust(cur, TriadCalculator.Field.B, 1, 0);
        assertThat(out).isEqualTo(new TriadCalculator.Values(0, 0, 0));
    }
}