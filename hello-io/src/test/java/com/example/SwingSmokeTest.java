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
import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;


public class SwingSmokeTest {

    private Robot robot;
    private FrameFixture window;

    @BeforeAll
    static void ensureHeadful() {
        // Make sure AWT is not headless; also helpful if Surefire config is overridden
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        robot = BasicRobot.robotWithNewAwtHierarchy();
        robot.settings().delayBetweenEvents(100);

        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("UI Smoke");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            JButton button = new JButton("Press me");
            button.setName("pressButton");
            JLabel label = new JLabel("Idle");
            label.setName("statusLabel");
            button.addActionListener(e -> label.setText("Clicked"));
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(button);
            panel.add(label);
            f.setContentPane(panel);
            f.pack();
            f.setLocationRelativeTo(null);
            return f;
        });

        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test
    }

    @AfterEach
    void tearDown() {
        try {
            window.cleanUp();
        } finally {
            if (robot != null) robot.cleanUp();
        }
    }

    @Test
    void clickingButton_updatesLabel() {
        window.button("pressButton").click();
        String text = window.label("statusLabel").text();
        assertThat(text).isEqualTo("Clicked");
    }

}
