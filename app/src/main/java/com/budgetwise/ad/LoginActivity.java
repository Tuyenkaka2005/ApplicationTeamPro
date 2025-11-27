// LoginActivity.java - Activity for user login
package com.budgetwise.ad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_login);

        initViews();
        // TỰ ĐỘNG ĐIỀN EMAIL SAU KHI ĐĂNG KÝ THÀNH CÔNG
        if (getIntent().hasExtra("signup_email")) {
            String email = getIntent().getStringExtra("signup_email");
            etEmail.setText(email);
            etEmail.setSelection(email.length());  // bôi đen để dễ sửa nếu cần
            etPassword.requestFocus();             // con trỏ nhảy sẵn vào ô mật khẩu
        }
        userDAO = new UserDAO(this);
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> login());
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        User user = userDAO.authenticate(email, password);
        if (user != null) {
            UserSession.setCurrentUser(this, user);
            startActivity(new Intent(this, MainActivity.class));
            SharedPreferences prefs = getSharedPreferences(UserSession.PREF_NAME, MODE_PRIVATE);
            prefs.edit().putString(UserSession.KEY_NAME, user.getName()).apply();
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}