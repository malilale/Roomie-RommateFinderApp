package com.roomie.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tv_name, tv_state, tv_department, tv_grade, tv_time, tv_distance, tv_email, tv_tel, tv_telTitle;
    private String name,state,department,grade,time,distance,email,tel, imgUrl, userId, CURRENT_STATE, currentUserId;
    private ImageView img_profile;
    private Button btn_request;

    CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setTitle("Kullanıcı Profili");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentUserId = currentUser.getUid();

        getExtras();
        matchComponents();
        fillComponents();

        Log.d("rec1",userId);
        Log.d("send1",currentUserId);

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

        btn_request.setOnClickListener(view ->
                setRequest());

    }

    private void setRequest() {
        if(CURRENT_STATE.matches("no_state")){
            sendRequest();
        }else if(CURRENT_STATE.matches("request_sent")){
            showCancelRequestDialog();
        }else if(CURRENT_STATE.matches("request_received")){
            showAcceptRequestDialog();
        }else if(CURRENT_STATE.matches("accepted")){
            showRemoveConnectionDialog();
        }
    }

    private void acceptRequest() {
        String senderId = currentUserId;
        String receiverId = userId;

        HashMap<String,String> req1 = new HashMap<>();
        HashMap<String,String> req2 = new HashMap<>();
        req1.put("request_type","accepted");
        req2.put("request_type","accepted");

        DocumentReference senderReference = collectionReference.document(senderId).collection("Requests").document(receiverId);
        DocumentReference receiverReference = collectionReference.document(receiverId).collection("Requests").document(senderId);

        senderReference.set(req1).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                receiverReference.set(req2).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Toast.makeText(this, "Eşleşme Kabul Edildi", Toast.LENGTH_SHORT).show();
                        CURRENT_STATE = "accepted";
                        btn_request.setBackgroundColor(0xFF4CAF50);
                        btn_request.setText("Eşleştirildi");
                    }else{
                        Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRequest(String msg) {
        String senderId = userId;
        String receiverId = currentUserId;

        DocumentReference senderReference = collectionReference.document(senderId).collection("Requests").document(receiverId);
        DocumentReference receiverReference = collectionReference.document(receiverId).collection("Requests").document(senderId);

        senderReference.delete().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                receiverReference.delete().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        CURRENT_STATE = "no_state";
                        btn_request.setBackgroundColor(Color.LTGRAY);
                        btn_request.setText("Eşleşme Talebi Gönder");
                    }else{
                        Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this, "İşlem Başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendRequest() {
        String senderId = currentUserId;
        String receiverId = userId;

        HashMap<String,String> req1 = new HashMap<>();
        HashMap<String,String> req2 = new HashMap<>();
        req1.put("request_type","sent");
        req2.put("request_type","received");

        DocumentReference senderReference = collectionReference.document(senderId).collection("Requests").document(receiverId);
        DocumentReference receiverReference = collectionReference.document(receiverId).collection("Requests").document(senderId);

        senderReference.set(req1).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                receiverReference.set(req2).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Toast.makeText(this, "Eşleşme Talebi Gönderildi", Toast.LENGTH_SHORT).show();
                        CURRENT_STATE = "request_sent";
                        btn_request.setBackgroundColor(Color.DKGRAY);
                        btn_request.setText("Talep Gönderildi");
                    }else{
                        Toast.makeText(this, "Talep Gönderme İşlemi Başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this, "Talep Gönderme İşlemi Başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAcceptRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);

        layout.setPadding(30,30,10,0);

        builder.setMessage("İsteği Kabul Et?");
        builder.setView(layout);

        builder.setPositiveButton("Kabul Et", (dialogInterface, i) -> {
            //Kabul Et
            acceptRequest();
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("Reddet", (dialogInterface, i) ->{
            cancelRequest("Talep Reddedildi");
            dialogInterface.dismiss();
    });
        builder.create().show();
    }


    private void showRemoveConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);

        layout.setPadding(30,30,10,0);

        builder.setMessage("Eşleşmeyi sonlandırmak istediğinize emin misiniz?");
        builder.setView(layout);

        builder.setPositiveButton("Sonlandır", (dialogInterface, i) -> {
            //Kabul Et
            cancelRequest("Eşleşme Sonlandırıldı");
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("İptal", (dialogInterface, i) ->{

            dialogInterface.dismiss();
        });
        builder.create().show();
    }

    private void showCancelRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);

        layout.setPadding(30,30,10,0);

        builder.setMessage("Eşleşme talebini geri almak istediğinize emin misiniz?");
        builder.setView(layout);

        builder.setPositiveButton("Geri al", (dialogInterface, i) -> {
            //Kabul Et
            cancelRequest("Eşleşme Talebi İptal Edildi");
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("İptal", (dialogInterface, i) ->{

            dialogInterface.dismiss();
        });
        builder.create().show();
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
        userId = bundle.getString("userId","").trim();
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

        btn_request = findViewById(R.id.btn_request);

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
        
        getCurrentState();
    }

    private void getCurrentState() {
        if(currentUserId.matches(userId))
            return;
        collectionReference = FirebaseFirestore.getInstance().collection("Contacts");

        DocumentReference documentReference = collectionReference.document(currentUserId).collection("Requests").document(userId);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.d("TAG", "get failed with ", error);
                    return;
                }
                btn_request.setVisibility(View.VISIBLE);
                if (document.exists()) {

                    String state = document.getString("request_type");
                    if(state.matches("sent")){
                        CURRENT_STATE = "request_sent";
                        btn_request.setBackgroundColor(Color.DKGRAY);
                        btn_request.setText("Talep Gönderildi");
                    }else if(state.matches("received")){
                        CURRENT_STATE = "request_received";
                        btn_request.setBackgroundColor(Color.DKGRAY);
                        btn_request.setText("İsteği Kabul Et");
                    }else if(state.matches("accepted")){
                        CURRENT_STATE = "accepted";
                        btn_request.setBackgroundColor(0xFF4CAF50);
                        btn_request.setText("Eşleştirildi");
                    }
                } else {
                    CURRENT_STATE = "no_state";
                    btn_request.setText("Eşleşme Talebi Gönder");
                    btn_request.setBackgroundColor(Color.LTGRAY);
                }
            }
        });
          documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
              @Override
              public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                  btn_request.setVisibility(View.VISIBLE);
                  if (task.isSuccessful()) {
                      DocumentSnapshot document = task.getResult();
                      if (document.exists()) {

                          String state = task.getResult().getString("request_type");
                          if(state.matches("sent")){
                              CURRENT_STATE = "request_sent";
                              btn_request.setBackgroundColor(Color.DKGRAY);
                              btn_request.setText("Talep Gönderildi");
                          }else if(state.matches("received")){
                              CURRENT_STATE = "request_received";
                              btn_request.setBackgroundColor(Color.DKGRAY);
                              btn_request.setText("İsteği Kabul Et");
                          }else if(state.matches("accepted")){
                              CURRENT_STATE = "accepted";
                              btn_request.setBackgroundColor(0xFF4CAF50);
                              btn_request.setText("Eşleşme");
                          }
                      } else {
                          CURRENT_STATE = "no_state";
                          btn_request.setBackgroundColor(Color.LTGRAY);
                      }
                  } else {
                      Log.d("TAG", "get failed with ", task.getException());
                  }
              }
          });
    }
}