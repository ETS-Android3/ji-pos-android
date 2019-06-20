package ch.japan_impact.japanimpactpos.network;

import androidx.annotation.Nullable;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class JavaObjectRequest<RepType> extends JsonRequest<RepType> {
    private static final Gson gson = new Gson();
    private String token;
    private Type repType;

    JavaObjectRequest(int method, String url, String body, Response.Listener<RepType> listener, @Nullable Response.ErrorListener errorListener, Type type) {
        super(method, url, body, listener, errorListener);

        this.repType = type;
    }

    JavaObjectRequest(int method, String url, Response.Listener<RepType> listener, @Nullable Response.ErrorListener errorListener, Type repType) {
        this(method, url, null, listener, errorListener, repType);
    }

    void setAuthToken(String token) {
        this.token = token;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        if (token != null)
            headers.put("authorization", "Bearer " + token);
        return headers;
    }

    @Override
    protected Response<RepType> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

            return Response.success(gson.fromJson(jsonString, repType), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
