package com.roomie.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_name, et_department, et_grade, et_state, et_distance, et_time, et_email, et_password;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle(R.string.register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.saving));

        mAuth = FirebaseAuth.getInstance();

        et_name = findViewById(R.id.et_name);
        et_department = findViewById(R.id.et_department);
        et_grade = findViewById(R.id.et_grade);
        et_state = findViewById(R.id.et_state);
        et_distance = findViewById(R.id.et_distance);
        et_time = findViewById(R.id.et_time);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);

        Button btn_register = findViewById(R.id.btn_register);

        et_grade.setOnClickListener(view ->
                showPopupMenu(et_grade, R.menu.grade_menu));
        et_state.setOnClickListener(view ->
                showPopupMenu(et_state, R.menu.state_menu));

        btn_register.setOnClickListener(view ->
                register());

    }

    private void register() {

        String name = et_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String grade = et_grade.getText().toString().trim();
        String state = et_state.getText().toString().trim();
        String distance = et_distance.getText().toString().trim();
        String time = et_time.getText().toString().trim();
        String password = et_password.getText().toString().trim();


        if(!email.isEmpty() && !password.isEmpty())
            registerUser(email,password);

    }

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "E-Posta Doğrulaması Gönderildi", Toast.LENGTH_SHORT).show();
                        uploadData();
                    }else{
                        Toast.makeText(RegisterActivity.this, R.string.register_unsuccess, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }else {
                Toast.makeText(RegisterActivity.this, R.string.register_unsuccess, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    private void uploadData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //create document
        if(currentUser!=null)
            documentReference = db.collection("Users").document(currentUser.getUid());

        Map<String, String> user = new HashMap<>();
        user.put("name", et_name.getText().toString().trim());
        user.put("department", et_department.getText().toString().trim());
        user.put("grade", et_grade.getText().toString().trim());
        user.put("state", et_state.getText().toString().trim());
        user.put("email", et_email.getText().toString().trim());
        user.put("distance", et_distance.getText().toString().trim());
        user.put("time", et_time.getText().toString().trim());
        user.put("tel", "");
        user.put("uid",currentUser.getUid());

        documentReference.set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RegisterActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    sendToMain();})
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, R.string.save_unsuccess, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();});
    }

    private void sendToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void showPopupMenu(EditText et, int menu) {
        PopupMenu popupMenu = new PopupMenu(this,et );
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                et.setText(menuItem.getTitle().toString());
                return true;
            }
        });
        popupMenu.show();
    }
}