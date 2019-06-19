package ch.japan_impact.japanimpactpos.network;

import android.content.Context;
import android.graphics.Path;
import android.util.Log;
import ch.japan_impact.japanimpactpos.data.PosConfiguration;
import ch.japan_impact.japanimpactpos.data.PosConfigurationList;
import ch.japan_impact.japanimpactpos.network.auth.AuthService;
import ch.japan_impact.japanimpactpos.network.data.ApiResult;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Louis Vialar
 */
@Singleton
public class BackendService {
    private static final String TAG = "Backend";
    private static final String PREFERENCE_KEY = "saved_login_tokens";
    private static final String API_URL = "https://shop.japan-impact.ch/api";

    private final RequestQueue queue;
    private final Context ctx;
    private final AuthService service;
    private final TokenStorage storage;

    @Inject
    public BackendService(Context ctx, AuthService service) {
        this.queue = Volley.newRequestQueue(ctx.getApplicationContext());
        this.service = service;
        this.ctx = ctx;
        this.storage = new TokenStorage(ctx.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE));
    }

    public TokenStorage getStorage() {
        return storage;
    }

    public void login(String email, String password, Consumer<Optional<String>> callback) throws JSONException {
        this.service.login(email, password, token -> {
            Log.i(TAG, "Got ticket " + token);

            JsonRequest<JSONObject> request = new JsonRequest<JSONObject>(
                    Request.Method.POST,
                    API_URL + "/users/login",
                    token,
                    response -> {
                        if (response.has("idToken") && response.has("refreshToken")) {
                            try {
                                storage.setTokens(response.getString("refreshToken"), response.getString("idToken"));
                                callback.accept(Optional.empty());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.accept(Optional.of(AuthService.ErrorCodes.UNKNOWN_ERROR.message));
                            }
                        } else callback.accept(Optional.of(AuthService.ErrorCodes.UNKNOWN_ERROR.message));
                    },
                    error -> {
                        if (error instanceof NetworkError) {
                            callback.accept(Optional.of(AuthService.ErrorCodes.NETWORK_ERROR.message));
                        } else if (error instanceof ServerError) {
                            //Indicates that the server responded with a error response
                            try {
                                String json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers, "utf-8"));
                                Log.e(TAG, json);
                            } catch (UnsupportedEncodingException e) {
                                Log.e(TAG, "Encoding error", e);
                            }
                            callback.accept(Optional.of(AuthService.ErrorCodes.UNKNOWN_ERROR.message));
                        } else {
                            Log.e(TAG, "VolleyError. " + error.toString() + " -- " + error.networkResponse, error.getCause());
                            callback.accept(Optional.of(AuthService.ErrorCodes.UNKNOWN_ERROR.message));
                        }
                    }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        String jsonString =
                                new String(
                                        response.data,
                                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                        return Response.success(
                                new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }

                @Override
                public String getBodyContentType() {
                    return "text/plain";
                }
            };
            Log.i(TAG, "Sending " + new String(request.getBody()));
            queue.add(request);
        }, error -> callback.accept(Optional.of(error.message)));
    }

    private void refreshIdToken(ApiCallback<Void> callback) {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                API_URL + "/users/refresh",
                null,
                response -> {
                    if (response.has("idToken") && response.has("refreshToken")) {
                        try {
                            storage.setTokens(response.getString("refreshToken"), response.getString("idToken"));
                            callback.onSuccess(null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.failure(e.getMessage());
                        }
                    } else callback.failure("Missing data");
                }, callback::failure) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>(super.getHeaders());
                if (!storage.isLoggedIn()) {
                    throw new AuthFailureError("Vous devez être connecté pour faire cela.");
                }
                headers.put("authorization", "Refresh " + storage.getRefreshToken());
                return headers;
            }
        };

        queue.add(req);
    }

    public void getConfigs(ApiCallback<List<PosConfigurationList>> callback) throws AuthFailureError {
        getConfigs(callback, false);
    }

    private <T> void tryRefresh(VolleyError error, boolean isRetry, ApiCallback<T> callback, Consumer<ApiCallback<T>> retry) {
        if (error instanceof AuthFailureError && !isRetry) {
            Log.i(TAG, "Trying to refresh token...");
            this.refreshIdToken(new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {;
                    retry.accept(callback);
                }

                @Override
                public void onFailure(List<String> errors) {
                    callback.onFailure(errors);
                }
            });
        } else {
            callback.failure(error);
        }
    }

    private void getConfigs(ApiCallback<List<PosConfigurationList>> callback, boolean isRetry) throws AuthFailureError {
        if (!storage.isLoggedIn()) {
            throw new AuthFailureError("Vous devez être connecté pour faire cela.");
        }

        TypeToken<List<PosConfigurationList>> tt = new TypeToken<List<PosConfigurationList>>() {
        };
        JavaObjectRequest<List<PosConfigurationList>> request =
                new JavaObjectRequest<>(Request.Method.GET, API_URL + "/pos/configurations",
                        callback::onSuccess, error -> tryRefresh(error, isRetry, callback, c -> {
                            try {
                                getConfigs(c, true);
                            } catch (AuthFailureError authFailureError) {
                                callback.failure(authFailureError);
                            }
                        }), tt.getType());

        request.setAuthToken(storage.getIdToken());
        queue.add(request);
    }

    public interface ApiCallback<T> {
        default void failure(String error) {
            onFailure(Collections.singletonList(error));
        }

        default void failure(VolleyError error) {
            ApiResult result = JavaObjectRequest.parseVolleyError(error);

            if (result == null) failure(error.getMessage());
            else onFailure(result.getErrors());
        }

        void onSuccess(T data);

        void onFailure(List<String> errors);

    }
}
