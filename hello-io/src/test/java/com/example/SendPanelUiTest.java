package com.example;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.*;

class SendPanelUiTest {

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
    }

    @AfterEach
    void tearDown() {
        try {
            if (window != null) window.cleanUp();
        } finally {
            if (robot != null) robot.cleanUp();
        }
    }

    private static Executor direct() {
        return Runnable::run;
    }

    @Test
    void clickingSend_withValidInputs_callsRpc_andShowsSuccess() throws Exception {
        // Mock RpcClient to return OK and capture payload
        RpcClient client = mock(RpcClient.class);
        when(client.sendValues(anyMap())).thenReturn(RpcClient.Result.ok("OK from mock"));

        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("SendPanel Test");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setContentPane(new SendPanel(client, direct()));
            f.pack();
            f.setLocationRelativeTo(null);
            return f;
        });

        window = new FrameFixture(robot, frame);
        window.show();

        // Deterministically set input values (avoid partial/append states)
        window.textBox("userIdField").setText("alice");
        window.textBox("amountField").setText("42");

        // Sanity-check field contents before submit
        assertThat(window.textBox("userIdField").text()).isEqualTo("alice");
        assertThat(window.textBox("amountField").text()).isEqualTo("42");

        // Click send
        window.button("sendButton").click();

        // Await status label to show success
        await().atMost(3, SECONDS).untilAsserted(() -> assertThat(window.label("statusLabel").text()).contains("OK from mock"));

        // Verify payload contents
        ArgumentCaptor<Map<String, Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(client, times(1)).sendValues(cap.capture());

        Map<String, Object> payload = cap.getValue();
        assertThat(payload.get("userId")).isEqualTo("alice");
        assertThat(payload.get("amount")).isEqualTo(42);
        assertThat(payload.get("currency")).isEqualTo("USD");
    }

    @Test
    void clickingSend_withInvalidInput_showsValidation_andDoesNotCallRpc() throws Exception {
        RpcClient client = mock(RpcClient.class);

        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("SendPanel Validation Test");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setContentPane(new SendPanel(client, direct()));
            f.pack();
            f.setLocationRelativeTo(null);
            return f;
        });

        window = new FrameFixture(robot, frame);
        window.show();

        // Leave userId empty, enter amount
        window.textBox("amountField").enterText("5");
        window.button("sendButton").click();

        // Await validation message
        await().atMost(2, SECONDS).untilAsserted(() -> assertThat(window.label("statusLabel").text()).contains("User ID is required"));

        verifyNoInteractions(client);
    }

    @Test
    void rpcError_isShownInStatus() throws Exception {
        RpcClient client = mock(RpcClient.class);
        when(client.sendValues(anyMap())).thenReturn(RpcClient.Result.error("Remote failure"));

        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("SendPanel Error Test");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setContentPane(new SendPanel(client, direct()));
            f.pack();
            f.setLocationRelativeTo(null);
            return f;
        });

        window = new FrameFixture(robot, frame);
        window.show();

        window.textBox("userIdField").setText("bob");
        window.textBox("amountField").setText("1");
        assertThat(window.textBox("userIdField").text()).isEqualTo("bob");
        assertThat(window.textBox("amountField").text()).isEqualTo("1");

        window.button("sendButton").click();

        await().atMost(3, SECONDS).untilAsserted(() -> assertThat(window.label("statusLabel").text()).contains("Remote failure"));
    }

}