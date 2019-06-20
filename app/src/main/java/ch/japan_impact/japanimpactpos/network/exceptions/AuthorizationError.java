package ch.japan_impact.japanimpactpos.network.exceptions;

import com.android.volley.VolleyError;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * @author Louis Vialar
 */
public class AuthorizationError extends ApiException {
    public AuthorizationError(VolleyError error) throws UnsupportedEncodingException, JSONException {
        super(error);
    }

    @Override
    public String getDescription() {
        return "Permission manquante! " + error.networkResponse.statusCode;
    }
}
