package ch.japan_impact.japanimpactpos.network.exceptions;

import com.android.volley.VolleyError;

/**
 * @author Louis Vialar
 */
public class VolleyException extends NetworkException {
    public final VolleyError error;

    public VolleyException(VolleyError error) {
        this.error = error;
    }

    @Override
    public String getDescription() {
        return error.getMessage();
    }
}
