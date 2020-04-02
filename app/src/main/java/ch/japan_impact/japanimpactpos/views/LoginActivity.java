package ch.japan_impact.japanimpactpos.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.network.BackendService;
import dagger.android.AndroidInjection;
import org.json.JSONException;

import javax.inject.Inject;
import java.util.function.Function;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    @Inject
    BackendService service;
    // UI references.
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        String CLIENT_ID = getResources().getString(R.string.auth_client_id);
        String API_URL = getResources().getString(R.string.auth_api_url);


        Button mEmailSignInButton = findViewById(R.id.login_button);
        mEmailSignInButton.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(API_URL + "/login?app=" + CLIENT_ID + "&tokenType=token"));
            startActivity(i);
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Check if the login data is here
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();

            if (data != null && data.getPath() != null && data.getPath().equals("/login")) {
                String accessToken = data.getQueryParameter("accessToken");

                this.attemptLogin(accessToken);
            } else {
                Toast.makeText(this, "Invalid callback, please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service.getStorage().isLoggedIn()) {
            startActivity(new Intent(this, ConfigurationPickerActivity.class));
            finish();
        }
    }

    private boolean checkFieldInvalid(EditText textView, Function<String, Boolean> isValid) {
        String val = textView.getText().toString();

        if (TextUtils.isEmpty(val)) {
            textView.setError(getString(R.string.error_field_required));
            textView.requestFocus();
            return true;
        } else if (!isValid.apply(val)) {
            textView.setError(getString(R.string.error_invalid_password));
            textView.requestFocus();
            return true;
        }

        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(String accessToken) {
        showProgress(true);

        try {
            service.login(accessToken, errors -> {
                if (errors.isPresent()) {
                    Toast.makeText(this, "Erreur de connexion : " + errors.get(), Toast.LENGTH_LONG).show();
                    showProgress(false);
                } else {
                    Toast.makeText(this, "Login success!", Toast.LENGTH_LONG).show();

                    Intent openIntent = new Intent(this, ConfigurationPickerActivity.class);
                    startActivity(openIntent);

                    finish();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Erreur de connexion : " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}

