package com.roomie.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {
    private String name,state,department,grade,time,distance,email,tel, imgUrl;
    private EditText et_name, et_state, et_department, et_grade, et_distance, et_time, et_tel;
    private ImageView img_profile;
    private Button btn_save;
    private Boolean isPfpChanged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setTitle(R.string.edit_profile);

        getExtras();
        matchComponents();
        fillComponents();

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