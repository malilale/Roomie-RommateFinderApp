package com.roomie.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private String name,state,department,grade,time,distance,email,tel, imgUrl;
    private String new_name,new_state,new_department,new_grade,new_time,new_distance,new_tel, new_imgUrl;
    private EditText et_name, et_state, et_department, et_grade, et_distance, et_time, et_tel;
    private ImageView img_profile;
    private Button btn_save;
    private ActivityResultLauncher<Intent> CamActivityResultLauncher,galleryActivityResultLauncher;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int GALLERY_PERM_CODE = 102;
    private Uri image_uri;
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

        setCameraIntent();
        setPickFromGalleryIntent();

        getExtras();
        matchComponents();
        fillComponents();

        et_grade.setOnClickListener(view ->
                showPopupMenu(et_grade, R.menu.grade_menu));
        et_state.setOnClickListener(view ->
                showPopupMenu(et_state, R.menu.state_menu));

        img_profile.setOnClickListener(view ->
                showImagePickDialog());

        btn_save.setOnClickListener(view -> {
            getNewdata();
            progressDialog.show();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            loadDatasToDb(currentUser);
        });

    }

    private void showPopupMenu(EditText et, int menu) {
        PopupMenu popupMenu = new PopupMenu(this,et );
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            et.setText(menuItem.getTitle().toString());
            return true;
        });
        popupMenu.show();
    }

    private void showImagePickDialog() {
        String options[] = {"Kamera","Galeri"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Profil Fotoğrafı");
        builder.setItems(options, (dialogInterface, i) -> {
            if(i == 0){
                //Camera
                askCameraPermissions();
            }else{
                //Gallery
                askGalleryPermissions();
            }
        });
        builder.create().show();
    }

    public Uri getImageUri(Bitmap inImage) throws IOException {
        File tempFile = new File(getCacheDir(), "temp.png");
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            inImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            // Handle error
        }
        return Uri.fromFile(tempFile);
    }

    private void setCameraIntent() {
        CamActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bundle bundle = result.getData().getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth(), bitmap.getHeight());
                try {
                    image_uri = getImageUri(thumb);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                img_profile.setImageURI(image_uri);
                isPfpChanged = true;
            }
        });

    }

    private void pickFromCamera() {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try{
            CamActivityResultLauncher.launch(camIntent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(EditProfileActivity.this,R.string.no_apps,Toast.LENGTH_SHORT).show();
        }
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else
            pickFromCamera();
    }

    private void setPickFromGalleryIntent(){
        galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth(), bitmap.getHeight());
                    try {
                        image_uri = getImageUri(thumb);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    img_profile.setImageURI(image_uri);
                    isPfpChanged = true;
                }
            });
    }

    private void pickFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try{
            galleryActivityResultLauncher.launch(pickIntent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(EditProfileActivity.this,R.string.no_apps,Toast.LENGTH_SHORT).show();
        }
    }

    private void askGalleryPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERM_CODE);
        }else
            pickFromGallery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickFromCamera();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == GALLERY_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadDatasToDb(FirebaseUser currentUser) {
        if(currentUser!=null)
            documentReference = db.collection("Users").document(currentUser.getUid());
        Double d = 0.0;

        StorageReference storageRef = null;

        Map<String, Object> user = new HashMap<>();
        user.put("name", new_name);
        user.put("department", new_department);
        user.put("grade", new_grade);
        user.put("state", new_state);
        user.put("distance", new_distance);
        user.put("time", new_time);
        user.put("tel", new_tel);
        user.put("imgUrl", imgUrl);
        user.put("email", email);
        user.put("latitude",181);
        user.put("longitude",181);
        user.put("userId",currentUser.getUid());

        if(isPfpChanged) {
            storageRef = FirebaseStorage.getInstance().getReference("Profile Images");
            StorageReference filePath = storageRef.child(System.currentTimeMillis() + ".jpg");
            filePath.putFile(image_uri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    filePath.getDownloadUrl().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            user.put("imgUrl", task1.getResult().toString());
                            documentReference.set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(EditProfileActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EditProfileActivity.this, R.string.save_unsuccess, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    });
                        } else {
                            user.put("imgUrl", imgUrl);
                            documentReference.set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(EditProfileActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EditProfileActivity.this, R.string.save_unsuccess, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    });
                        }
                    });
                }
            });
        }else {
            user.put("imgUrl", imgUrl);
            documentReference.set(user)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(EditProfileActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();})
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this, R.string.save_unsuccess, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();});
        }
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