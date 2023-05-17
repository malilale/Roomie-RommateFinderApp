package com.roomie.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        //set Fragments by bottom navigation bar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected=null;
            switch (item.getItemId()){
                case R.id.users_menu:
                    //selected = usersFragment;
                    getSupportActionBar().setTitle(R.string.users);
                    break;
                case R.id.map_menu:
                    //selected = announcementsFragment;
                    getSupportActionBar().setTitle(R.string.find_on_map);
                    break;
                case R.id.profile_menu:
                    selected = profileFragment;
                    getSupportActionBar().setTitle(R.string.profile);
                    break;
            }
            if(selected!=null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selected).commit();
                return true;
            }else
                return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            sendToLoginPage();
    }

    private void sendToLoginPage() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}