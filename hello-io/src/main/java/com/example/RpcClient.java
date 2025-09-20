package com.example;

import java.util.Map;

public interface RpcClient {

    Result sendValues(Map<String, Object> payload) throws Exception;

    record Result(boolean success, String message) {
        public static Result ok(String message) { return new Result(true, message); }
        public static Result error(String message) { return new Result(false, message); }
    }
}
