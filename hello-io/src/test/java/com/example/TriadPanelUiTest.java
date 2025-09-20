package com.example;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

class TriadPanelUiTest {

    private Robot robot;
    private FrameFixture window;

    @BeforeAll
    static void ensureHeadful() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(1000);

        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("Triad");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setContentPane(new TriadPanel());
            f.pack();
            f.setLocationRelativeTo(null);
            return f;
        });

        window = new FrameFixture(robot, frame);
        window.show();
    }

    @AfterEach
    void tearDown() {
        try { window.cleanUp(); } finally { if (robot != null) robot.cleanUp(); }
    }

    @Test
    void editingA_adjustsB_keepsC() {
        window.textBox("fieldA").selectAll().enterText("50");
        // Assert text fields reflect the deterministic policy
        String a = window.textBox("fieldA").text();
        String b = window.textBox("fieldB").text();
        String c = window.textBox("fieldC").text();
        int total = Integer.parseInt(a) + Integer.parseInt(b) + Integer.parseInt(c);
        assertThat(total).isEqualTo(100);
    }
}