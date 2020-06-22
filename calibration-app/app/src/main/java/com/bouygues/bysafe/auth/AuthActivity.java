package com.bouygues.bysafe.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bouygues.bysafe.MainActivity;
import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

import retrofit2.http.HEAD;

import static com.bouygues.bysafe.MainApplication.getContext;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthPanel";
    private FirebaseAuth _auth;
    private boolean pressed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        _auth = FirebaseAuth.getInstance();
        EditText textInput = findViewById(R.id.site_id);
        CircularProgressBar pb = findViewById(R.id.login_progress_bar);

        textInput.setOnFocusChangeListener((a, b) -> {
            textInput.setHint("");
        });

        final TextView authButton = findViewById(R.id.auth_button);
        authButton.setOnClickListener(v -> {
            pb.setProgress(100);
            pb.setVisibility(View.VISIBLE);

            final String site_id = textInput.getText().toString();

            final String email = site_id + "@bysafe.app";
            final String password = "no-pass";

            Logger.d(TAG, "signin with id " + site_id);
            Logger.d(TAG, "signin with email " + email);
            Logger.d(TAG, "signin with password " + password);

            if (!pressed) {
                pressed = true;
                AppConfigManager.getInstance(getContext()).setPrefOnline(true);
                _auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                GradientDrawable bg = (GradientDrawable) textInput.getBackground();
                                bg.setStroke(3, Color.WHITE);
                                textInput.setTextColor(Color.WHITE);
                                if (task.isSuccessful())
                                    closePanel();
                                else {
                                    Toast.makeText(AuthActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    bg.setStroke(3, ContextCompat.getColor(getBaseContext(), R.color.strong_red));
                                    textInput.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.strong_red));
                                }
                                pb.setVisibility(View.GONE);
                            }
                        });
                pressed = false;
            }
        });

        // TODO OFFLINE BUTTON -> on click listener
//        if (!pressed) {
//            Logger.d(TAG, "Launching app offline");
//            pressed = true;
//            AppConfigManager.getInstance(getContext()).setPrefOnline(false);
//            closePanel();
//        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = _auth.getCurrentUser();

        if (currentUser != null && AppConfigManager.getInstance(getContext()).getIsLogged())
            closePanel();
    }

    private void closePanel() {
        // save that the client is logged
        AppConfigManager.getInstance(getContext()).setIsLogged(true);

        // prepare main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


        MainActivity activity = (MainActivity) MainActivity.getContext();
        if (activity != null) {
            AppConfigManager.getInstance(MainApplication.getContext()).setIsThread(true);
            activity.threadContact();
            DP3T.start(MainActivity.getContext());
        }

        // close auth panel
        finish();
    }
}
