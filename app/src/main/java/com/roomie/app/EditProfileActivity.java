package com.roomie.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private String name,state,department,grade,time,distance,email,tel, imgUrl;
    private String new_name,new_state,new_department,new_grade,new_time,new_distance,new_tel, new_imgUrl;
    private EditText et_name, et_state, et_department, et_grade, et_distance, et_time, et_tel;
    private ImageView img_profile;
    private Button btn_save;
    private Boolean isPfpChanged;
    private ProgressDialog progressDialog;
    private DocumentReference documentReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setTitle(R.string.edit_profile);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.saving));

        getExtras();
        matchComponents();
        fillComponents();

        btn_save.setOnClickListener(view -> {
            getNewdata();
            progressDialog.show();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            loadDatasToDb(currentUser);
        });

    }

    private void loadDatasToDb(FirebaseUser currentUser) {
        if(currentUser!=null)
            documentReference = db.collection("Users").document(currentUser.getUid());

        Map<String, String> user = new HashMap<>();
        user.put("name", new_name);
        user.put("department", new_department);
        user.put("grade", new_grade);
        user.put("state", new_state);
        user.put("distance", new_distance);
        user.put("time", new_time);
        user.put("tel", new_tel);
        user.put("imgUrl", imgUrl);
        user.put("email", email);


        documentReference.set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditProfileActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();})
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, R.string.save_unsuccess, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();});
    }

    private void getNewdata() {
        new_name = et_name.getText().toString().trim();
        new_state = et_state.getText().toString().trim();
        new_department = et_department.getText().toString().trim();
        new_grade = et_grade.getText().toString().trim();
        new_time = et_time.getText().toString().trim();
        new_distance = et_distance.getText().toString().trim();
        new_tel = et_tel.getText().toString().trim();
    }

    private void getExtras() {
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("name","");
        state = bundle.getString("state","");
        department = bundle.getString("department","");
        grade = bundle.getString("grade","");
        email = bundle.getString("email","");
        time = bundle.getString("time","");
        distance = bundle.getString("distance","");
        tel = bundle.getString("tel","");
        imgUrl = bundle.getString("imgUrl","");
    }
    private void matchComponents() {
        et_name = findViewById(R.id.et_editname);
        et_state = findViewById(R.id.et_editstate);
        et_department = findViewById(R.id.et_editdepartment);
        et_grade = findViewById(R.id.et_editgrade);
        et_distance = findViewById(R.id.et_editdistance);
        et_time = findViewById(R.id.et_edittime);
        et_tel = findViewById(R.id.et_edittel);
        img_profile = findViewById(R.id.img_editProfile);

        btn_save = findViewById(R.id.btn_save);
    }
    private void fillComponents() {
        isPfpChanged=false;

        et_name.setText(name);
        et_state.setText(state);
        et_department.setText(department);
        et_grade.setText(grade);
        et_distance.setText(distance);
        et_time.setText(time);
        et_tel.setText(tel);

        if(!imgUrl.isEmpty())
            Picasso.get().load(imgUrl).into(img_profile);
    }
}