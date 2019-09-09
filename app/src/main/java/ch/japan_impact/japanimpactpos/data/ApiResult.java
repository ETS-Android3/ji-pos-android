package ch.japan_impact.japanimpactpos.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class ApiResult {
    private boolean success;
    @Nullable
    private List<ApiError> errors;

    public ApiResult() {
    }

    public ApiResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public @NonNull List<ApiError> getErrors() {
        return errors == null ? Collections.emptyList() : errors;
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

}
