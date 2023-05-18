package com.roomie.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tv_name, tv_state, tv_department, tv_grade, tv_time, tv_distance, tv_email, tv_tel, tv_telTitle;
    private String name,state,department,grade,time,distance,email,tel, imgUrl;
    private ImageView img_profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setTitle("Kullanıcı Profili");

        getExtras();
        matchComponents();
        fillComponents();

        tv_tel.setOnClickListener(view1 ->{
            String phone = tv_tel.getText().toString();
            String message = "Merhaba,";
            String url = "https://wa.me/" + phone + "?text=" + message;
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse(url));
            startActivity(whatsappIntent);
        });

        tv_email.setOnClickListener(view1 -> {
            String emailAddress = tv_email.getText().toString();
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", emailAddress, null));
            startActivity(Intent.createChooser(emailIntent, "Send email..."));

        });

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
        tv_name = findViewById(R.id.tv_username);
        tv_state = findViewById(R.id.tv_userstate);
        tv_department = findViewById(R.id.tv_userdepartment);
        tv_grade = findViewById(R.id.tv_usergrade);
        tv_distance = findViewById(R.id.tv_userdistance);
        tv_time = findViewById(R.id.tv_usertime);
        tv_tel = findViewById(R.id.tv_usertel);
        tv_email = findViewById(R.id.tv_useremail);
        tv_telTitle = findViewById(R.id.tv_usertelTitle);

        img_profile = findViewById(R.id.img_userProfile);
    }
    private void fillComponents() {
        tv_name.setText(name);
        tv_state.setText("Durum: "+state);
        tv_department.setText(department);
        tv_grade.setText(grade);
        tv_distance.setText(distance+" km");
        tv_time.setText(time+" Dönem");
        tv_tel.setText(tel);
        tv_email.setText(email);

        if(!tel.isEmpty()){
            tv_telTitle.setVisibility(View.VISIBLE);
            tv_tel.setVisibility(View.VISIBLE);
        }

        if(!imgUrl.isEmpty())
            Picasso.get().load(imgUrl).into(img_profile);
    }
}