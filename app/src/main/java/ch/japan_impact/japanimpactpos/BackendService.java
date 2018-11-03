package ch.japan_impact.japanimpactpos;

import android.content.Context;
import android.support.annotation.Nullable;
import ch.japan_impact.japanimpactpos.data.PosConfiguration;
import ch.japan_impact.japanimpactpos.utils.Either;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Louis Vialar
 */
public class BackendService {
    private static final String API_URL = "https://shop.japan-impact.ch/api";
    private static BackendService instance;
    private final RequestQueue queue;
    private final Context ctx;
    private final Moshi moshi = new Moshi.Builder().build();

    public BackendService(RequestQueue queue, Context ctx) {
        this.queue = queue;
        this.ctx = ctx;
    }

    public static BackendService getInstance(Context context) {
        if (instance == null) {
            instance = new BackendService(Volley.newRequestQueue(context.getApplicationContext()), context.getApplicationContext());
        }

        return instance;
    }

    public JsonObjectRequest login(String email, String password, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) throws JSONException {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_URL + "/users/login", new JSONObject().put("email", email).put("password", password), listener, errorListener);
        queue.add(request);
        return request;
    }

    public JsonArrayRequest getConfigs(Consumer<Either<List<PosConfiguration>, String>> listOrError) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL + "/pos/configurations", null, resp -> {
            Type type = Types.newParameterizedType(List.class, PosConfiguration.class);
            JsonAdapter<List<PosConfiguration>> adapter = moshi.adapter(type);
            try {
                List<PosConfiguration> cards = adapter.fromJson(resp.toString());
                listOrError.accept(Either.ofFirst(cards));
            } catch (IOException e) {
                e.printStackTrace();
                listOrError.accept(Either.ofSecond(e.getMessage()));
            }
        }, error -> listOrError.accept(Either.ofSecond(error.getMessage()))) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>(super.getHeaders());
                if (!PosSession.getInstance(ctx).isLoggedIn()) {
                    throw new AuthFailureError("Vous devez être connecté pour faire cela.");
                }
                headers.put("authorization", "Bearer " + PosSession.getInstance(ctx).getToken());
                return headers;
            }
        };
        queue.add(request);
        return request;
    }
}
