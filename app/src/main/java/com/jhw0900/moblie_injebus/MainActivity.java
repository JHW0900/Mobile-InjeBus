package com.jhw0900.moblie_injebus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jhw0900.moblie_injebus.data.service.AuthenticationService;

public class MainActivity extends AppCompatActivity {
    AuthenticationService authService;

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

        authService = new AuthenticationService();
    }

    public void onClickLogin(View v){
        EditText eId = findViewById(R.id.id);
        EditText ePw = findViewById(R.id.password);

        String id = eId.getText().toString();
        String pw = ePw.getText().toString();

        authService.login(id, pw, new AuthenticationService.LoginCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Login Failed.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}