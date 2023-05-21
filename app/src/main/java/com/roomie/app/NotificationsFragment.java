package com.roomie.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class NotificationsFragment extends Fragment implements SelectListener{
    RecyclerView recyclerView;
    NotificationAdapter notificationAdapter;
    ArrayList<Notification> list;
    CollectionReference reference;
    private String currentUserId;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();

        recyclerView = view.findViewById(R.id.notifications_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        reference = FirebaseFirestore.getInstance().collection("Contacts").document(currentUserId).collection("Notifications");

        list = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getActivity(),list,this);
        recyclerView.setAdapter(notificationAdapter);

        getData();


        return view;
    }

    private void getData() {
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    list.clear();
                    for(DocumentSnapshot d : value.getDocuments()){
                        list.add(0,d.toObject(Notification.class));
                    }
                    notificationAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        Notification selectedNotification = list.get(position);
        getUserInfo(selectedNotification.getUserId());
    }

    private void getUserInfo(String userId){
        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(userId);

        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                sendToUserProfile(user);
            }
        });
    }

    private void sendToUserProfile(User user) {
        Intent intent = new Intent(getActivity(),UserProfileActivity.class);
        intent.putExtra("name",user.getName());
        intent.putExtra("state",user.getState());
        intent.putExtra("department",user.getDepartment());
        intent.putExtra("grade",user.getGrade());
        intent.putExtra("time",user.getTime());
        intent.putExtra("distance",user.getDistance());
        intent.putExtra("email",user.getEmail());
        intent.putExtra("tel",user.getTel());
        intent.putExtra("imgUrl",user.getImgUrl());
        intent.putExtra("userId",user.getUserId());
        startActivity(intent);
    }
}