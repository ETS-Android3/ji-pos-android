package ch.japan_impact.japanimpactpos.network.auth;

import android.content.Context;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

/**
 * @author Louis Vialar
 */
@Singleton
public class AuthService {
    private static final String API_URL = "https://auth.japan-impact.ch/";
    private static final String CLIENT_ID = "8pdszcUWxht75pmttKnyAxxHzc9HarutYvScP6uHq5WsDXJqGfQcWeCRHGZgHwtGE9dQvKgu";
    private final RequestQueue queue;
    private final Context ctx;

    @Inject
    public AuthService(Context ctx) {
        this.ctx = ctx;
        this.queue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    public enum ErrorCodes {
        USER_OR_PASSWORD_INVALID(201, "Email ou mot de passe incorrect"),
        EMAIL_NOT_CONFIRMED(202, "Email non confirmé"),

        UNKNOWN_ERROR(100, "Une erreur inconnue s'est produite"),
        MISSING_DATA(101, "Erreur lors de l'envoi des données"),
        UNKNOWN_APP(102, "Application mal configurée (erreur 102)"),


        NETWORK_ERROR(-1, "Une erreur réseau s'est produite"),
        ;

        public final int code;
        public final String message;

        ErrorCodes(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public static ErrorCodes fromCode(int errorCode) {
            for (ErrorCodes e : ErrorCodes.values())
                if (e.code == errorCode)
                    return e;
            return UNKNOWN_ERROR;
        }
    }

    public void login(String email, String password, Consumer<String> tokenConsumer, Consumer<ErrorCodes> errorConsumer) throws JSONException {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                API_URL + "/hidden/login",
                new JSONObject()
                        .put("email", email)
                        .put("password", password)
                        .put("clientId", CLIENT_ID),
                response -> {
                    if (response.has("ticket")) {
                        try {
                            tokenConsumer.accept(response.getString("ticket"));
                        } catch (JSONException e) {
                            errorConsumer.accept(ErrorCodes.UNKNOWN_ERROR);
                        }
                    } else if (response.has("errorCode")) {
                        try {
                            errorConsumer.accept(ErrorCodes.fromCode(response.getInt("errorCode")));
                        } catch (JSONException e) {
                            errorConsumer.accept(ErrorCodes.UNKNOWN_ERROR);
                        }
                    } else {
                        errorConsumer.accept(ErrorCodes.UNKNOWN_ERROR);
                    }
                }, error -> {
            Log.e("AuthAPI", "VolleyError. " + error.toString() + " -- " + error.networkResponse.toString(), error.getCause());

            if (error instanceof NetworkError) {
                errorConsumer.accept(ErrorCodes.NETWORK_ERROR);
            } else if (error instanceof ServerError) {
                //Indicates that the server responded with a error response
                try {
                    String json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers, "utf-8"));
                    JSONObject response = new JSONObject(json);
                    errorConsumer.accept(ErrorCodes.fromCode(response.getInt("errorCode")));
                } catch (UnsupportedEncodingException | JSONException e) {
                    errorConsumer.accept(ErrorCodes.UNKNOWN_ERROR);
                }
            } else if (error instanceof ParseError) {
                // Indicates that the server response could not be parsed
                errorConsumer.accept(ErrorCodes.UNKNOWN_ERROR);
            }
        });

        queue.add(request);
    }
}
