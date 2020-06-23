package com.bouygues.bysafe.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import retrofit2.http.HEAD;

import static com.bouygues.bysafe.MainApplication.getContext;

public class AuthActivity extends AppCompatActivity {


    private static final String TAG = "AuthPanel";
    private boolean pressed = false;
    private boolean anonyme = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);
        EditText textInput = findViewById(R.id.site_id);
        CircularProgressBar pb = findViewById(R.id.login_progress_bar);

        if (anonyme) {
            final TextView anonButton = findViewById(R.id.anon_button);
            anonButton.setVisibility(View.VISIBLE);
            anonButton.setClickable(true);
            anonButton.setOnClickListener(v -> {
                pb.setProgress(100);
                pb.setVisibility(View.VISIBLE);
                if (!pressed) {
                    Logger.d(TAG, "Launching app offline");
                    pressed = true;
                    AppConfigManager.getInstance(getContext()).setPrefOnline(false);
                    AppConfigManager.getInstance(getContext()).setPrefBadgeNumber("");
                    pb.setVisibility(View.GONE);
                    closePanel();
                    pressed = false;
                }
            });
        }

        textInput.setOnFocusChangeListener((a, b) -> textInput.setHint(""));

        final TextView authButton = findViewById(R.id.auth_button);
        authButton.setOnClickListener(v -> {
            pb.setProgress(100);
            pb.setVisibility(View.VISIBLE);

            final String site_id = textInput.getText().toString();

            Logger.d(TAG, "signin with id " + site_id);

            if (!pressed) {
                pressed = true;
                GradientDrawable bg = (GradientDrawable) textInput.getBackground();
                bg.setStroke(3, Color.WHITE);
                textInput.setTextColor(Color.WHITE);
                new ConnectUser().execute(site_id);
            }
        });
    }

    class ConnectUser extends AsyncTask<String, Void, Integer> {

        String site_id = "";

        @Override
        protected Integer doInBackground(String... strings) {
            site_id = strings[0];
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("https://us-central1-bysafe-4ee9a.cloudfunctions.net/IdExists/" + site_id);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                return connection.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return 503;
        }

        protected void onPostExecute(Integer responseCode) {
            CircularProgressBar pb = findViewById(R.id.login_progress_bar);
            EditText textInput = findViewById(R.id.site_id);
            GradientDrawable bg = (GradientDrawable) textInput.getBackground();
            if (responseCode == 200) {
                AppConfigManager.getInstance(getContext()).setPrefOnline(true);
                AppConfigManager.getInstance(getContext()).setPrefBadgeNumber(site_id);
                closePanel();
            } else if (responseCode == 404) {
                Toast.makeText(AuthActivity.this, "ID non reconnue", Toast.LENGTH_SHORT).show();
                bg.setStroke(3, ContextCompat.getColor(getBaseContext(), R.color.strong_red));
                textInput.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.strong_red));
            } else {
                Toast.makeText(AuthActivity.this, "Nous n'avons pas pu ouvrir la connection.\nRéessayez plus tard, ou passez en mode Hors Ligne", Toast.LENGTH_SHORT).show();
                final TextView anonButton = findViewById(R.id.anon_button);
                anonButton.setVisibility(View.VISIBLE);
                anonButton.setClickable(true);
                anonButton.setOnClickListener(v -> {
                    pb.setProgress(100);
                    pb.setVisibility(View.VISIBLE);
                    if (!pressed) {
                        Logger.d(TAG, "Launching app offline");
                        pressed = true;
                        AppConfigManager.getInstance(getContext()).setPrefOnline(false);
                        AppConfigManager.getInstance(getContext()).setPrefBadgeNumber("");
                        pb.setVisibility(View.GONE);
                        closePanel();
                        pressed = false;
                    }
                });
            }
            pb.setVisibility(View.GONE);
            pressed = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (AppConfigManager.getInstance(getContext()).getIsLogged())
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
