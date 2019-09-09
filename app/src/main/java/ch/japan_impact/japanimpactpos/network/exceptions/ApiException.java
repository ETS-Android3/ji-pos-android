package ch.japan_impact.japanimpactpos.network.exceptions;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class ApiException extends VolleyException {
    public final String key;
    public final List<String> messages = new ArrayList<>();
    public final List<JSONObject> args = new ArrayList<>();

    public ApiException(VolleyError error) throws UnsupportedEncodingException, JSONException {
        super(error);

        Log.i("ApiException", "Build exception from " + error.networkResponse + " (len " + (error.networkResponse == null ? 0 : error.networkResponse.data.length));

        // Fill the data:
        if (error.networkResponse != null && error.networkResponse.data.length > 0) {
            NetworkResponse r = error.networkResponse;
            String jsonString = new String(r.data, HttpHeaderParser.parseCharset(r.headers, "utf-8"));

            Log.i("ApiException", "Json " + jsonString);

            JSONObject object = new JSONObject(jsonString);

            if (object.has("success") && object.has("errors") && object.getJSONArray("errors").length() > 0) {
                object = object.getJSONArray("errors").getJSONObject(0);
            }

            this.key = object.optString("key", null);

            if (object.has("messages")) {
                JSONArray arr = object.getJSONArray("messages");
                for (int i = 0; i < arr.length(); ++i)
                    messages.add(arr.getString(i));
            }

            if (object.has("args")) {
                JSONArray arr = object.getJSONArray("args");
                for (int i = 0; i < arr.length(); ++i)
                    args.add(arr.getJSONObject(i));
            }

        } else {
            throw new NullPointerException();
        }
    }


    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (key != null) sb.append(key).append(" - ");

        for (String m : messages) {
            sb.append(m).append(" ");
        }

        return sb.toString();
    }
}
