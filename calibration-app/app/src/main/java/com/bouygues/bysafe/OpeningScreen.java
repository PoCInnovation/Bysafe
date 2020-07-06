package com.bouygues.bysafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bouygues.bysafe.auth.AuthActivity;

import org.dpppt.android.sdk.internal.AppConfigManager;

import static com.bouygues.bysafe.MainApplication.getContext;

public class OpeningScreen extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfigManager appConfigManager = AppConfigManager.getInstance(getContext());
        setContentView(R.layout.first_opening_screen);
        final RelativeLayout l1 = findViewById(R.id.button_opening_barrier_gestures);
        final RelativeLayout l2 = findViewById(R.id.button_opening_protection);
        final RelativeLayout l3 = findViewById(R.id.button_opening_activities);
        final ImageButton logoutButton = findViewById(R.id.logout_button);

        logoutButton.setVisibility(View.GONE);

        l1.setOnClickListener(v -> {
            appConfigManager.setPrefChosenOpeningMenu("barrier_gestures");
            appConfigManager.setPrefIsFirstOpening(false);
            closePanel();
        });
        l2.setOnClickListener(v -> {
            appConfigManager.setPrefChosenOpeningMenu("protection");
            appConfigManager.setPrefIsFirstOpening(false);
            closePanel();
        });
        l3.setOnClickListener(v -> {
            appConfigManager.setPrefChosenOpeningMenu("activities");
            appConfigManager.setPrefIsFirstOpening(false);
            closePanel();
        });
    }

    private void closePanel() {
        final ImageButton logoutButton = findViewById(R.id.logout_button);

        logoutButton.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}
