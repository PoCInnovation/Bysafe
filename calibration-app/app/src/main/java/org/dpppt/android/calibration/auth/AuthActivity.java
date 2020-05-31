package org.dpppt.android.calibration.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.dpppt.android.calibration.MainActivity;
import org.dpppt.android.calibration.MainApplication;
import org.dpppt.android.calibration.R;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        final Button authButton = findViewById(R.id.auth_button);
        Logger.d("button", "text value is : " + authButton.getText());
        authButton.setOnClickListener(v -> {
            AppConfigManager cfgManager = AppConfigManager.getInstance(MainApplication.getContext());
            cfgManager.setIsLogged(true);
            Logger.d("auth", "is logged is now at : " + String.valueOf(cfgManager.getIsLogged()));
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
