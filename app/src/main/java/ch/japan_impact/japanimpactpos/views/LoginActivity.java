package ch.japan_impact.japanimpactpos.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
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
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Inject
    BackendService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> attemptLogin());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
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
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);


        boolean cancel = checkFieldInvalid(mEmailView, this::isEmailValid) || checkFieldInvalid(mPasswordView, this::isPasswordValid);


        if (!cancel) {
            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            try {
                service.login(email, password, errors -> {
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
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
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

