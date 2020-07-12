package ch.japan_impact.japanimpactpos.network;

import android.content.Context;
import android.util.Log;
import androidx.annotation.Nullable;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.data.ApiResult;
import ch.japan_impact.japanimpactpos.data.PaymentMethod;
import ch.japan_impact.japanimpactpos.data.pos.CheckedOutItem;
import ch.japan_impact.japanimpactpos.data.pos.PosConfigResponse;
import ch.japan_impact.japanimpactpos.data.pos.PosConfigurationList;
import ch.japan_impact.japanimpactpos.data.pos.PosOrderResponse;
import ch.japan_impact.japanimpactpos.data.scan.ScanConfigurationList;
import ch.japan_impact.japanimpactpos.data.scan.ScanResult;
import ch.japan_impact.japanimpactpos.network.exceptions.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Louis Vialar
 */
@Singleton
public class BackendService {
    private static final String TAG = "Backend";
    private static final String PREFERENCE_KEY = "saved_login_tokens";
    private final String API_URL;

    private final RequestQueue queue;
    private final Context ctx;
    private final TokenStorage storage;

    @Inject
    public BackendService(Context ctx) {
        this.queue = Volley.newRequestQueue(ctx.getApplicationContext());
        this.ctx = ctx;
        this.API_URL = ctx.getResources().getString(R.string.api_url);
        this.storage = new TokenStorage(ctx.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE));
    }

    public TokenStorage getStorage() {
        return storage;
    }

    public void login(String accessToken, Consumer<Optional<String>> callback) throws JSONException {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                API_URL + "/users/login",
                (String response) -> {
                    storage.setToken(response);

                            callback.accept(Optional.empty());
                },
                error -> {
                    if (error instanceof NetworkError) {
                        callback.accept(Optional.of(ErrorCodes.NETWORK_ERROR.message));
                    } else if (error instanceof ServerError) {
                        //Indicates that the server responded with a error response
                        try {
                            String json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers, "utf-8"));
                            Log.e(TAG, json);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, "Encoding error", e);
                        }
                        callback.accept(Optional.of(ErrorCodes.UNKNOWN_ERROR.message));
                    } else {
                        Log.e(TAG, "VolleyError. " + error.toString() + " -- " + error.networkResponse, error.getCause());
                        callback.accept(Optional.of(ErrorCodes.UNKNOWN_ERROR.message));
                    }
                }) {

            @Override
            public byte[] getBody() {
                try {
                    return URLEncoder.encode(accessToken, "UTF-8").getBytes();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "text/plain";
            }
        };
        Log.i(TAG, "Sending " + accessToken);
        queue.add(request);
    }

    public void getPosConfigs(ApiCallback<List<PosConfigurationList>> callback) {
        TypeToken<List<PosConfigurationList>> tt = new TypeToken<List<PosConfigurationList>>() {
        };
        sendAuthenticatedRequest(tt.getType(), Request.Method.GET, API_URL + "/pos/configurations", callback);
    }

    public void getScanConfigs(ApiCallback<List<ScanConfigurationList>> callback) {
        TypeToken<List<ScanConfigurationList>> tt = new TypeToken<List<ScanConfigurationList>>() {
        };
        sendAuthenticatedRequest(tt.getType(), Request.Method.GET, API_URL + "/scan/configurations", callback);
    }

    public void scan(int configId, String barcode, ApiCallback<ScanResult> callback) {
        JSONObject o = new JSONObject();
        try {
            o.put("barcode", barcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendAuthenticatedRequest(ScanResult.class, Request.Method.POST, API_URL + "/scan/process/" + configId, o.toString(), callback);
    }

    public void getConfig(int eventId, int id, ApiCallback<PosConfigResponse> callback) {
        sendAuthenticatedRequest(PosConfigResponse.class, Request.Method.GET, API_URL + "/pos/configurations/" + eventId + "/" + id, callback);
    }

    public void placeOrder(Collection<CheckedOutItem> content, ApiCallback<PosOrderResponse> callback) {
        sendAuthenticatedRequest(PosOrderResponse.class, Request.Method.POST, API_URL + "/pos/checkout", "{\"items\": " + new Gson().toJson(content) + "}", callback);
    }

    public void sendPOSLog(int orderId, PaymentMethod method, boolean accepted, @Nullable String message, ApiCallback<ApiResult> callback) {
        this.sendPOSLog(orderId, method, accepted, message, null, null, null, callback);
    }

    public void sendPOSLog(int orderid, PaymentMethod method, boolean accepted, @Nullable String message, @Nullable String txCode, @Nullable String failureCause, @Nullable Boolean cardReceiptSend, ApiCallback<ApiResult> callback) {
        JSONObject o = new JSONObject();
        try {
            o.put("paymentMethod", method.name())
                    .put("accepted", accepted);

            if (message != null) o.put("cardTransactionMessage", message);
            if (txCode != null) o.put("cardTransactionCode", txCode);
            if (failureCause != null) o.put("cardTransactionFailureCause", failureCause);
            if (cardReceiptSend != null) o.put("cardReceiptSend", cardReceiptSend.booleanValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendAuthenticatedRequest(ApiResult.class, Request.Method.POST, API_URL + "/pos/paymentLog/" + orderid, o.toString(), callback);
    }

    private <T> void sendAuthenticatedRequest(Type clazz, int method, String url, String body, ApiCallback<T> listener) {
        sendAuthenticatedRequest(clazz, method, url, body, listener, true);
    }

    private <T> void sendAuthenticatedRequest(Type clazz, int method, String url, ApiCallback<T> listener) {
        sendAuthenticatedRequest(clazz, method, url, null, listener);
    }

    private <T> void sendAuthenticatedRequest(Type clazz, int method, String url, String body, ApiCallback<T> listener, boolean retry) {
        if (!storage.isLoggedIn()) {
            listener.onFailure(new LoginRequiredException());
            return;
        }

        JavaObjectRequest<T> request = new JavaObjectRequest<>(method, url, body, listener::onSuccess, listener::failure, clazz);
        request.setAuthToken(storage.getAccessToken());
        queue.add(request);
    }

    public interface ApiCallback<T> {
        default void failure(String error) {
            onFailure(new GenericNetworkException(error));
        }

        default void failure(VolleyError error) {
            if (error.networkResponse != null) {
                try {
                    if (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                        onFailure(new AuthorizationError(error));
                    } else onFailure(new ApiException(error));
                } catch (NullPointerException | UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                    onFailure(new VolleyException(error));
                }
            } else {
                onFailure(new VolleyException(error));
            }
        }

        void onSuccess(T data);

        void onFailure(NetworkException error);

    }
}
