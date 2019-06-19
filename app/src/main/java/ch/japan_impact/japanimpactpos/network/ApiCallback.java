package ch.japan_impact.japanimpactpos.network;

import ch.japan_impact.japanimpactpos.network.data.ApiResult;
import com.android.volley.VolleyError;

import java.util.Collections;
import java.util.List;

public interface ApiCallback<T> {
    static <T> void failure(ApiCallback callback, String error) {
        callback.onFailure(Collections.singletonList(new ApiResult.ApiError(error)));
    }

    static <T> void failure(ApiCallback callback, VolleyError error) {
        ApiResult result = JavaObjectRequest.parseVolleyError(error);

        if (result == null) failure(callback, error.getMessage());
        else callback.onFailure(result.getErrors());
    }

    void onSuccess(T data);

    void onFailure(List<ApiResult.ApiError> errors);

}