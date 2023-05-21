package com.roomie.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class UsersFragment extends Fragment implements SelectListener{
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    ArrayList<User> list;
    CollectionReference reference;
    private Button btn_menu,btn_filter;
    private EditText et_distance, et_time, et_state;
    private LinearLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.user_view);
        reference = FirebaseFirestore.getInstance().collection("Users");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list = new ArrayList<>();
        userAdapter = new UserAdapter(getActivity(),list,this);
        recyclerView.setAdapter(userAdapter);

        btn_menu = view.findViewById(R.id.btn_menu_filter);
        btn_filter = view.findViewById(R.id.btn_filter);

        et_distance = view.findViewById(R.id.et_filterdistance);
        et_time = view.findViewById(R.id.et_filtertime);
        et_state = view.findViewById(R.id.et_filterstate);

        layout = view.findViewById(R.id.filter_layout);


        et_state.setOnClickListener(view1 ->
                showPopupStateMenu());

        btn_menu.setOnClickListener(view1 ->
                openFilterMenu());

        btn_filter.setOnClickListener(view1 ->
                setFilters());


        getData();

        return view;
    }

    private void setFilters() {
        String distance = et_distance.getText().toString().trim();
        String time = et_time.getText().toString().trim();
        String state = et_state.getText().toString().trim();
        Boolean getTime=false;

        Query filterQuery=null;

        if(state.isEmpty() && distance.isEmpty() && time.isEmpty()){
            Toast.makeText(getActivity(), R.string.pickfilter, Toast.LENGTH_SHORT).show();
            return;
        }else if(time.isEmpty() && distance.isEmpty()){
            //only state
            filterQuery = reference.whereEqualTo("state",state);
        }else if(time.isEmpty() && state.isEmpty()){
            //only distance
            filterQuery = reference.whereLessThanOrEqualTo("distance",distance);
        }else if(distance.isEmpty() && state.isEmpty()){
            //only time
            filterQuery = reference.whereGreaterThanOrEqualTo("time",time);
        }else if(time.isEmpty()){
            //distance and state
            filterQuery = reference
                    .whereEqualTo("state",state)
                    .whereGreaterThanOrEqualTo("time",time);
        }else if(distance.isEmpty()){
            //time and state
            filterQuery = reference
                    .whereEqualTo("state",state)
                    .whereGreaterThanOrEqualTo("time",time);
        }else if(state.isEmpty()){
            //distance and time
            filterQuery = reference.whereLessThanOrEqualTo("distance",distance);
            getTime =true;
        }else{
            //all
            filterQuery = reference
                    .whereEqualTo("state",state)
                    .whereLessThanOrEqualTo("distance",distance);
            getTime = true;
        }

        list.clear();
        Boolean finalGetTime = getTime;
        filterQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                list.add(d.toObject(User.class));
            }
            if(finalGetTime){
                float t = Float.parseFloat(time);
                for(User u:list){
                    if(Float.parseFloat(u.getTime())<t)
                        list.remove(u);
                }
            }
            userAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void showPopupStateMenu() {
        PopupMenu popupMenu = new PopupMenu(getActivity(),et_state );
        popupMenu.getMenuInflater().inflate(R.menu.state_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            et_state.setText(menuItem.getTitle().toString());
            return true;
        });
        popupMenu.show();
    }

    private void openFilterMenu() {
        et_distance.setText("");
        et_time.setText("");
        et_state.setText("");
        if(layout.getVisibility()==View.GONE) {
            layout.setVisibility(View.VISIBLE);
        }else if(layout.getVisibility()==View.VISIBLE) {
            layout.setVisibility(View.GONE);
        }
    }


    private void getData(){
        reference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                list.add(d.toObject(User.class));
            }
            userAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onItemClicked(int position) {
        User selectedUser = list.get(position);
        sendToUserProfile(selectedUser);
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