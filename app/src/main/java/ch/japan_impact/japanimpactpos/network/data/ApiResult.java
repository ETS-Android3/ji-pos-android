package ch.japan_impact.japanimpactpos.network.data;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class ApiResult {
    private boolean success;
    private List<ApiError> errors;

    public ApiResult(boolean success) {
        this.success = success;
    }

    public ApiResult(List<ApiError> errors) {
        this.success = !errors.isEmpty();
        this.errors = errors;
    }

    public ApiResult() {
    }

    public static class ApiError {
        private String key;
        private List<String> messages;
        private List<JsonObject> args;

        public ApiError(String message) {
            this.messages = Collections.singletonList(message);
        }

        public ApiError(String key, List<String> messages, List<JsonObject> args) {
            this.key = key;
            this.messages = messages;
            this.args = args;
        }

        public ApiError() {
        }

        public String getKey() {
            return key;
        }

        public List<String> getMessages() {
            return messages;
        }

        public List<JsonObject> getArgs() {
            return args;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors.stream().map(err -> {
            StringBuilder sb = new StringBuilder();
            if (err.key != null) sb.append(err.key).append(" - ");
            for (String m : err.messages) {
                sb.append(m).append(" ");
            }

            return sb.toString();
        }).collect(Collectors.toList());
    }
}