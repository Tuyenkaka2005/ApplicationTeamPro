package com.budgetwise.ad;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private UserDAO userDAO;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userDAO = new UserDAO(this);
        initViews();
        if (getIntent().getBooleanExtra("just_signed_up", false)) {
            String email = getIntent().getStringExtra("signup_email");
            if (email != null) {
                etEmail.setText(email);
                etPassword.requestFocus(); // Chuyển con trỏ sang mật khẩu
                Toast.makeText(this, "Registration successful! Please log in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            btnSignUp.setEnabled(false);
            btnSignUp.setText("Creating account...");
            signUp();
        });
        tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void signUp() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        // Validate
        if (TextUtils.isEmpty(name)) {
            showError(etName, "Please enter your full name");
            resetButton();
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, "Invalid email");
            resetButton();
            return;
        }
        if (password.length() < 6) {
            showError(etPassword, "Password must be 6 characters or more");
            resetButton();
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, "Passwords do not match");
            resetButton();
            return;
        }
        if (userDAO.getUserByEmail(email) != null) {
            showError(etEmail, "This email is already in use");
            resetButton();
            return;
        }

        // Tạo user
        String userId = "user_" + System.currentTimeMillis();
        User user = new User(userId, name, email, password);

        long result = userDAO.createUser(user);
        if (result > 0) {
            Toast.makeText(this, "Registration successful! Please login", Toast.LENGTH_LONG).show();

            // CHỈ TRẢ VỀ LOGIN – KHÔNG TỰ ĐỘNG ĐĂNG NHẬP
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("just_signed_up", true);        // Gợi ý: có thể tự fill email
            intent.putExtra("signup_email", email);         // Tự động điền email vào ô đăng nhập
            startActivity(intent);
            finish(); // Đóng SignUpActivity
        } else {
            Toast.makeText(this, "System error, please try again", Toast.LENGTH_SHORT).show();
            resetButton();
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void showError(TextInputEditText field, String error) {
        field.setError(error);
        field.requestFocus();
    }

    private void resetButton() {
        btnSignUp.setEnabled(true);
        btnSignUp.setText("Register");
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}