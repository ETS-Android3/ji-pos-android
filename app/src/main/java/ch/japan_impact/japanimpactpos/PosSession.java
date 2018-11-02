package ch.japan_impact.japanimpactpos;

import android.content.Context;
import android.util.Pair;
import org.json.JSONException;

import java.util.function.Consumer;

/**
 * @author Louis Vialar
 */
public class PosSession {
    private static PosSession instance;
    private final Context ctx;
    private String token;

    public PosSession(Context ctx) {
        this.ctx = ctx;
    }

    public static PosSession getInstance(Context ctx) {
        if (instance == null) {
            instance = new PosSession(ctx.getApplicationContext());
        }
        return instance;
    }

    /**
     * Try to login against the backend
     * @param email the email of the client
     * @param password the password of the client
     * @param callback a callback taking (success, errorMessage (optional))
     */
    public void login(String email, String password, Consumer<Pair<Boolean, String>> callback) throws JSONException {
        BackendService.getInstance(ctx).login(email, password, success -> {
            try {
                this.token = success.getString("token");
                callback.accept(new Pair<>(true, null));
            } catch (JSONException e) {
                e.printStackTrace();
                callback.accept(new Pair<>(false, e.getMessage()));
            }
        }, error -> callback.accept(new Pair<>(false, error.getMessage())));
    }

    public void logout() {
        this.token = null;
    }

    public String getToken() {
        return this.token;
    }

    public boolean isLoggedIn() {
        return this.token != null;
    }
}
