package com.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanityUnitTest {
    @Test
    void addition_works() {
        int sum = 2 + 3;
        assertThat(sum).isEqualTo(5);
    }
}
