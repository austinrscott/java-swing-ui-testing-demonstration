package com.example;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SendPresenterTest {

    static class RecordingListener implements SendPresenter.Listener {
        String state = "Idle";
        String message;

        @Override
        public void onIdle() {
            state = "Idle";
            message = null;
        }

        @Override
        public void onSending() {
            state = "Sending";
            message = null;
        }

        @Override
        public void onSuccess(String message) {
            state = "Success";
            this.message = message;
        }

        @Override
        public void onError(String message) {
            state = "Error";
            this.message = message;
        }

        @Override
        public void onValidationError(String field, String message) {
            state = "Validation:" + field;
            this.message = message;
        }
    }

    private static Executor direct() {
        return Runnable::run;
    }

    @Test
    void validatesInputs_beforeCallingRpc() throws Exception {
        RpcClient client = mock(RpcClient.class);
        RecordingListener listener = new RecordingListener();
        SendPresenter presenter = new SendPresenter(client, direct(), Clock.systemUTC(), listener);

        presenter.submit("", "10");
        assertThat(listener.state).startsWith("Validation:userId");

        listener.onIdle();
        presenter.submit("alice", "-1");
        assertThat(listener.state).startsWith("Validation:amount");

        verifyNoInteractions(client);
    }

    @Test
    void buildsExpectedPayload_andHandlesSuccess() throws Exception {
        RpcClient client = mock(RpcClient.class);
        RecordingListener listener = new RecordingListener();
        SendPresenter presenter = new SendPresenter(client, direct(), Clock.systemUTC(), listener);

        when(client.sendValues(anyMap())).thenReturn(RpcClient.Result.ok("OK"));

        presenter.submit("alice", "42");

        ArgumentCaptor<Map<String, Object>> payloadCap = ArgumentCaptor.forClass(Map.class);
        verify(client).sendValues(payloadCap.capture());

        Map<String, Object> payload = payloadCap.getValue();
        assertThat(payload.get("userId")).isEqualTo("alice");
        assertThat(payload.get("amount")).isEqualTo(42);
        assertThat(payload.get("currency")).isEqualTo("USD");

        assertThat(listener.state).isEqualTo("Success");
        assertThat(listener.message).isEqualTo("OK");
    }

    @Test
    void handlesRemoteError_andException() throws Exception {
        RpcClient client = mock(RpcClient.class);
        RecordingListener listener = new RecordingListener();
        SendPresenter presenter = new SendPresenter(client, direct(), Clock.systemUTC(), listener);

        when(client.sendValues(anyMap())).thenReturn(RpcClient.Result.error("Bad Request"));

        presenter.submit("bob", "1");
        assertThat(listener.state).isEqualTo("Error");
        assertThat(listener.message).contains("Bad Request");

        // Now simulate exception
        listener.onIdle();
        reset(client);
        when(client.sendValues(anyMap())).thenThrow(new RuntimeException("boom"));

        presenter.submit("carol", "2");
        assertThat(listener.state).isEqualTo("Error");
        assertThat(listener.message).contains("boom");
    }
}