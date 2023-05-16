package com.roomie.app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText et_email, et_password;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle(R.string.login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loging));

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        Button btn_login = findViewById(R.id.btn_login);
        TextView tv_forgotPw = findViewById(R.id.tv_forgotpw);
        TextView tv_register = findViewById(R.id.tv_register);
        mAuth = FirebaseAuth.getInstance();

        btn_login.setOnClickListener(view -> {
            String email = et_email.getText().toString().trim();
            String password = et_password.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                //error message
                et_email.setError(getString(R.string.invaild_email));
                et_email.setFocusable(true);
            }else {
                login(email,password); //login user
            }

        });

        tv_register.setOnClickListener(view -> { //register user
            sendToRegisterPage();
        });

        tv_forgotPw.setOnClickListener(view -> //Recover password
                recoverPasswordDialog());
    }

    private void sendToRegisterPage() {
       // Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
       // startActivity(intent);
    }

    private void recoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        final EditText et_dialog_email = new EditText(this);
        et_dialog_email.setHint(R.string.email);
        et_dialog_email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        layout.addView(et_dialog_email);
        layout.setPadding(20,10,10,10);

        builder.setView(layout);

        builder.setPositiveButton(R.string.send, (dialogInterface, i) -> {
            String email_dialog = et_dialog_email.getText().toString().trim();
            recover(email_dialog);
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void recover(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, R.string.email_sent, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, R.string.email_cnt_sent, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                progressDialog.dismiss();
                sendToMain(); //Login successful
            }else {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, R.string.wrong_email_pw, Toast.LENGTH_SHORT).show();
            }
        });
        //Login User
    }

    private void sendToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}