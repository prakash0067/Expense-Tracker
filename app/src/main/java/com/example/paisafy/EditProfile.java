package com.example.paisafy;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.paisafy.Model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileOutputStream;

public class EditProfile extends AppCompatActivity {

    private ImageView profileImage;
    private TextInputEditText nameEdit, emailEdit, passwordEdit;
    private TextView changePicBtn;
    private MaterialButton saveBtn;
    private Uri imageUri;
    private String currentPhotoPath;

    private DbHelper dbHelper;
    private int userId;

    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_GALLERY = 102;
    private static final int PERMISSION_REQUEST = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        profileImage = findViewById(R.id.editProfileImage);
        nameEdit = findViewById(R.id.editFullName);
        emailEdit = findViewById(R.id.editEmail);
        passwordEdit = findViewById(R.id.editPassword);
        changePicBtn = findViewById(R.id.changePicButton);
        saveBtn = findViewById(R.id.saveProfileBtn);

        dbHelper = new DbHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);

        // Load user data
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            nameEdit.setText(user.getName());
            emailEdit.setText(user.getEmail());
            passwordEdit.setText("");

            if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                File imgFile = new File(user.getProfilePic());
                if (imgFile.exists()) {
                    profileImage.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                }
            }
        }

        // Change profile pic
        changePicBtn.setOnClickListener(v -> showImagePickerBottomSheet());

        // Save button with validation
        saveBtn.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            if (name.isEmpty()) {
                nameEdit.setError("Name cannot be empty");
                nameEdit.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                emailEdit.setError("Email cannot be empty");
                emailEdit.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEdit.setError("Enter a valid email address");
                emailEdit.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordEdit.setError("Password cannot be empty");
                passwordEdit.requestFocus();
                return;
            }

            if (password.length() < 6) {
                passwordEdit.setError("Password must be at least 6 characters");
                passwordEdit.requestFocus();
                return;
            }

            // If all validations pass
            dbHelper.updateUserInfo(userId, name, email, password);
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish(); // go back to profile screen
        });


        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST);
        }
    }

    private void showImagePickerBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_profile_pic, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        LinearLayout takePhoto = view.findViewById(R.id.takePhoto);
        LinearLayout choosePhoto = view.findViewById(R.id.choosePhoto);
        LinearLayout cancelBtn = view.findViewById(R.id.cancelBtn);

        takePhoto.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        choosePhoto.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "user_" + userId + "_" + timeStamp + ".jpg";

        File storageDir = new File(getFilesDir(), "profile_pics");
        if (!storageDir.exists()) storageDir.mkdirs();

        File imageFile = new File(storageDir, imageFileName);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;

            try {
                if (requestCode == REQUEST_CAMERA && imageUri != null) {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } else if (requestCode == REQUEST_GALLERY && data != null) {
                    imageUri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    // Save the image to internal folder and get path
                    File imageFile = createImageFile();
                    FileOutputStream out = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    out.flush();
                    out.close();

                    currentPhotoPath = imageFile.getAbsolutePath(); // Update with new path

                }

                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                    // Save file name or full path to DB instead of Base64
                    String imagePath = currentPhotoPath;
                    dbHelper.updateUserProfilePicPath(userId, imagePath);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
