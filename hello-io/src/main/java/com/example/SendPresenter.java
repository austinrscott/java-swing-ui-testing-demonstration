package com.example;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class SendPresenter {

    public interface Listener {
        void onIdle();
        void onSending();
        void onSuccess(String message);
        void onError(String message);
        void onValidationError(String field, String message);
    }

    private final RpcClient rpcClient;
    private final Executor executor;
    private final Clock clock; // reserved for future timestamps/retries/testing
    private final Listener listener;

    public SendPresenter(RpcClient rpcClient, Executor executor, Clock clock, Listener listener) {
        this.rpcClient = Objects.requireNonNull(rpcClient);
        this.executor = Objects.requireNonNull(executor);
        this.clock = Objects.requireNonNull(clock);
        this.listener = Objects.requireNonNull(listener);
        listener.onIdle();
    }

    public void submit(String userIdText, String amountText) {
        // Validate input
        String userId = userIdText == null ? "" : userIdText.trim();
        if (userId.isEmpty()) {
            listener.onValidationError("userId", "User ID is required.");
            return;
        }
        Integer amount = parseNonNegativeInt(amountText);
        if (amount == null) {
            listener.onValidationError("amount", "Amount must be a whole number >= 0.");
            return;
        }

        // Build payload (this is what we’ll verify in unit tests)
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("amount", amount);
        payload.put("currency", "USD"); // example static field to show payload composition

        listener.onSending();

        executor.execute(() -> {
            try {
                RpcClient.Result result = rpcClient.sendValues(payload);
                if (result.success()) {
                    listener.onSuccess(result.message() != null ? result.message() : "Sent OK");
                } else {
                    listener.onError(result.message() != null ? result.message() : "Remote error");
                }
            } catch (Exception ex) {
                listener.onError("Failed to send: " + ex.getMessage());
            }
        });
    }

    private Integer parseNonNegativeInt(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            int v = Integer.parseInt(text.trim());
            return v < 0 ? null : v;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}