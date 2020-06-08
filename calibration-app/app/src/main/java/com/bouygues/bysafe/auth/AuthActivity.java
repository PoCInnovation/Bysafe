package com.bouygues.bysafe.auth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bouygues.bysafe.MainActivity;
import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthPanel";
    private FirebaseAuth _auth;
    private boolean pressed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        _auth = FirebaseAuth.getInstance();

        final Button authButton = findViewById(R.id.auth_button);
        authButton.setOnClickListener(v -> {

            EditText textInput = findViewById(R.id.site_id);
            final String site_id = textInput.getText().toString();

            final String email = site_id + "@bysafe.app";
            final String password = "no-pass";

            Logger.d(TAG, "signin with id " + site_id);
            Logger.d(TAG, "signin with email " + email);
            Logger.d(TAG, "signin with password " + password);

            if (!pressed) {
                pressed = true;
                _auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                    closePanel();
                                else {
                                    Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    textInput.setBackgroundColor(Color.RED);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = _auth.getCurrentUser();

        if (currentUser != null && AppConfigManager.getInstance(MainApplication.getContext()).getIsLogged())
            closePanel();
    }

    private void closePanel() {
        // save that the client is logged
        AppConfigManager.getInstance(MainApplication.getContext()).setIsLogged(true);

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
