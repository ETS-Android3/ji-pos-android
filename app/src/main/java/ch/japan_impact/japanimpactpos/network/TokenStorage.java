package ch.japan_impact.japanimpactpos.network;

import android.content.SharedPreferences;
import android.util.Log;
import com.auth0.android.jwt.DecodeException;
import com.auth0.android.jwt.JWT;

import java.util.Date;

/**
 * A class that represents a way to store authentication tokens for an event's ticketing platform
 *
 * @author Louis Vialar
 */
public class TokenStorage {
    private final SharedPreferences preferences;
    private String refreshToken;
    private String idToken;

    TokenStorage(SharedPreferences preferences) {
        this.preferences = preferences;

        readTokenFromPreferences();
    }

    protected void readTokenFromPreferences() {
        // Retrieve token if it exists
        this.refreshToken = preferences.getString("refreshToken", null);
        this.idToken = preferences.getString("idToken", null);
    }

    protected void writeTokenToPreferences() {
        preferences.edit()
                .putString("idToken", idToken)
                .putString("refreshToken", refreshToken)
                .apply();
    }

    /**
     * Check if this token storage holds a valid token
     * @return true if this storage holds a valid JWT token or a non-JWT token
     */
    public boolean isLoggedIn() {
        if (refreshToken == null) {
            return false; // No token == not logged in
        }

        try {
            JWT jwt = new JWT(refreshToken);
            Date exp = jwt.getExpiresAt();

            Log.i("TicketingService", "Logged in with token expiring at " + exp + " / " + jwt.toString());

            return exp == null || exp.after(new Date()); // token is not expired yet
        } catch (DecodeException exception) {
            // Not a JWT token
            // We consider the token as valid for ever
            return true;
        }
    }

    public void logout() {
        this.refreshToken = null;
        this.idToken = null;

        writeTokenToPreferences();
    }

    void setTokens(String refresh, String id) {
        this.refreshToken = refresh;
        this.idToken = id;

        Log.i("TokenStorage", "Storing tokens " +refresh + " / " + id);

        writeTokenToPreferences();
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }
}
