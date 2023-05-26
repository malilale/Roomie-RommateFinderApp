package com.roomie.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class MapsFragment extends Fragment {
    Context mContext;
    ArrayList<User> list = new ArrayList<>();
    private Button btn_menu,btn_filter;
    private EditText et_distance, et_time, et_state;
    private LinearLayout layout;
    CollectionReference reference;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
            GoogleMap googleMap;

        @Override
        public void onMapReady(GoogleMap googleMap) {
            this.googleMap = googleMap;

            getUserInfo(googleMap);
            btn_filter.setOnClickListener(view1 ->
                    setFilters(googleMap));


        }
    };

    private void getUserLocations(GoogleMap googleMap) {
        googleMap.clear();
        LatLng ytu = new LatLng(41.025766, 28.8898359);
        googleMap.addMarker(new MarkerOptions().position(ytu).title("YILDIZ TEKNİK")
                .snippet("Davutpaşa Kampüsü")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        BitmapDescriptor bitmapDescriptor;
        for(User u:list) {
            if(u.getLatitude()>180 || u.getLongitude() >180)
                continue;
            if(uid.matches(u.getUserId()))
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
            else
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            LatLng position = new LatLng(u.getLatitude(), u.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(position).title(u.getName())
                    .snippet(u.getState()+"\n"
                            +"Süre: "+u.getTime()+"\n"
                            +"Uzaklık: "+u.getDistance())
                    .icon(bitmapDescriptor));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ytu,14));
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Nullable
                @Override
                public View getInfoContents(@NonNull Marker marker) {
                    return null;
                }

                @Nullable
                @Override
                public View getInfoWindow(@NonNull Marker marker) {
                    LinearLayout info = new LinearLayout(mContext);
                    info.setOrientation(LinearLayout.VERTICAL);
                    info.setBackgroundColor(Color.WHITE);
                    info.setPadding(10,10,10,10);

                    TextView title = new TextView(getActivity());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(mContext);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }

    }

    private void getUserInfo(GoogleMap googleMap) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        reference = db.collection("Users");
        reference.get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               for(DocumentSnapshot d : task.getResult().getDocuments()){
                   list.add(d.toObject(User.class));
               }
               getUserLocations(googleMap);
           }else{
               Log.i("User Info","Failed");
           }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
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



        return view;
    }

    private void setFilters(GoogleMap googleMap) {
        String distance = et_distance.getText().toString().trim();
        String time = et_time.getText().toString().trim();
        String state = et_state.getText().toString().trim();
        boolean getTime=false;

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
        boolean finalGetTime = getTime;
        filterQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                list.add(d.toObject(User.class));
                getUserLocations(googleMap);
            }
            if(finalGetTime){
                float t = Float.parseFloat(time);
                for(User u:list){
                    if(Float.parseFloat(u.getTime())<t)
                        list.remove(u);
                }
                getUserLocations(googleMap);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            getUserLocations(googleMap);
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
        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
        } else if (layout.getVisibility() == View.VISIBLE) {
            layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }


}