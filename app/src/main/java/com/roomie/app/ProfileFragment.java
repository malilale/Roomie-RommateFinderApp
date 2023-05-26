package com.roomie.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


public class ProfileFragment extends Fragment {
    private TextView tv_name, tv_state, tv_department, tv_grade, tv_time, tv_distance, tv_email, tv_tel, tv_telTitle;
    private String name,state,department,grade,time,distance,email,tel, imgUrl;
    private ImageView img_profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        tv_name = view.findViewById(R.id.tv_name);
        tv_state = view.findViewById(R.id.tv_state);
        tv_department = view.findViewById(R.id.tv_department);
        tv_grade = view.findViewById(R.id.tv_grade);
        tv_time = view.findViewById(R.id.tv_time);
        tv_distance = view.findViewById(R.id.tv_distance);
        tv_email = view.findViewById(R.id.tv_email);
        tv_tel = view.findViewById(R.id.tv_tel);
        tv_telTitle = view.findViewById(R.id.tv_telTitle);

        img_profile = view.findViewById(R.id.img_profile);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection("Users").document(currentUserId);

        documentReference.get().addOnCompleteListener(task -> {
            if(task.getResult().exists()){
                getData(task);
            }else{
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.act_logout:
                FirebaseAuth.getInstance().signOut();
                sendToLoginPage();
                break;
            case R.id.act_edit:
                sendToEditProfilePage();
                break;
            case R.id.act_updatepassword:
                updatePasswordDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendToEditProfilePage() {
        Intent intent = new Intent(getActivity(),EditProfileActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("state",state);
        intent.putExtra("department",department);
        intent.putExtra("grade",grade);
        intent.putExtra("time",time);
        intent.putExtra("distance",distance);
        intent.putExtra("email",email);
        intent.putExtra("tel",tel);
        intent.putExtra("imgUrl",imgUrl);
        startActivity(intent);
    }

    private void sendToLoginPage() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void updatePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LinearLayout layout = new LinearLayout(getActivity());
        final EditText et_password = new EditText(getActivity());
        et_password.setHint(R.string.new_password);
        et_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        layout.addView(et_password);
        layout.setPadding(30,30,10,10);

        builder.setView(layout);

        builder.setPositiveButton(R.string.Change, (dialogInterface, i) -> {
            String password = et_password.getText().toString().trim();
            updatePassword(password);
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void updatePassword(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updatePassword(password).addOnCompleteListener(task -> {
            if(task.isSuccessful())
                Toast.makeText(getActivity(), R.string.recover_pw_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
        });
    }


    private void getData(Task<DocumentSnapshot> task) {
        name = task.getResult().getString("name");
        state = task.getResult().getString("state");
        department = task.getResult().getString("department");
        grade = task.getResult().getString("grade");
        time = task.getResult().getString("time");
        distance = task.getResult().getString("distance");
        email = task.getResult().getString("email");
        tel = task.getResult().getString("tel");
        imgUrl = task.getResult().getString("imgUrl");

        tv_name.setText(name);
        tv_state.setText("Durum: "+state);
        tv_department.setText(department);
        tv_grade.setText(grade);
        tv_time.setText(time+" Dönem");
        tv_distance.setText(distance+" km");
        tv_email.setText(email);

        if(!imgUrl.isEmpty()){
            Picasso.get().load(imgUrl).into(img_profile);
        }

        if(tel.isEmpty()) {
            tv_telTitle.setVisibility(View.GONE);
            tv_tel.setVisibility(View.GONE);
        }else {
            tv_tel.setText(tel);
            tv_telTitle.setVisibility(View.VISIBLE);
            tv_tel.setVisibility(View.VISIBLE);
        }
    }
}