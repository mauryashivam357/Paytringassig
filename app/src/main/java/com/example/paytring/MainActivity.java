package com.example.paytring;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText editTextUrl, editTextMethod, editTextBody;
    private TextView textViewResponse;
    private Button buttonCallApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        editTextUrl = findViewById(R.id.etUrl);
        editTextMethod = findViewById(R.id.etMethod);
        editTextBody = findViewById(R.id.etRequestBody);
        textViewResponse = findViewById(R.id.tvResponse);
        buttonCallApi = findViewById(R.id.btnSendRequest);

        buttonCallApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeApiCall();
            }
        });

        // Schedule Background Worker
        scheduleWorker();
    }
    private void makeApiCall() {
        String url = editTextUrl.getText().toString().trim();
        String method = editTextMethod.getText().toString().trim().toUpperCase();
        String requestBody = editTextBody.getText().toString().trim();

        if (url.isEmpty() || method.isEmpty()) {
            textViewResponse.setText("Please enter URL and HTTP Method.");
            return;
        }

        // Create a new Thread for network operations
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request;

                if ("GET".equals(method)) {
                    request = new Request.Builder().url(url).get().build();
                } else if ("POST".equals(method)) {
                    RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json; charset=utf-8"));
                    request = new Request.Builder().url(url).post(body).build();
                } else {
                    runOnUiThread(() -> textViewResponse.setText("Unsupported HTTP Method! Use GET or POST."));
                    return;
                }

                Response response = client.newCall(request).execute();
                final String responseData = response.body().string();

                runOnUiThread(() -> textViewResponse.setText(responseData));
            } catch (IOException e) {
                runOnUiThread(() -> textViewResponse.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void scheduleWorker() {
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).build();
        WorkRequest workRequest = new PeriodicWorkRequest.Builder(LogWorker.class, 5, TimeUnit.MINUTES).setConstraints(constraints).build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }
}
