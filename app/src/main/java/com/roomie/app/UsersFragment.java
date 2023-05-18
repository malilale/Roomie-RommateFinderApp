package com.roomie.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class UsersFragment extends Fragment implements SelectListener{
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    ArrayList<User> list;
    CollectionReference reference;
    private Button btn_menu,btn_filter;
    private EditText et_filter;
    private String option, filter;
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

        et_filter = view.findViewById(R.id.et_filter);
        btn_menu = view.findViewById(R.id.btn_menu_filter);
        btn_filter = view.findViewById(R.id.btn_filter);

        layout = view.findViewById(R.id.filter_layout);

        et_filter.setText("");
        option = "";

        btn_menu.setOnClickListener(view1 ->
               // showPopupMenu());
                deneme());

        btn_filter.setOnClickListener(view1 ->
                setFilters());


        getData();

        return view;
    }

    private void deneme() {
        if(layout.getVisibility()==View.GONE)
            layout.setVisibility(View.VISIBLE);
        else if(layout.getVisibility()==View.VISIBLE)
            layout.setVisibility(View.GONE);
    }

    private void setFilters() {
        filter = et_filter.getText().toString().trim();
        if(filter.isEmpty() || option.isEmpty()){
            Toast.makeText(getActivity(), R.string.pickfilter, Toast.LENGTH_SHORT).show();
            return;
        }
        list.clear();
        Query filterQuery = reference.whereEqualTo(option,filter);

        filterQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                list.add(d.toObject(User.class));
            }
            userAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getActivity(), btn_menu);
        popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               // option = getOption(menuItem.getTitle().toString());
                et_filter.setText("");
                btn_menu.setText(menuItem.getTitle().toString());
                return true;
            }
        });
        popupMenu.show();
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
        User currentUser = list.get(position);
        //sendToUserProfile(currentUser);
    }




}