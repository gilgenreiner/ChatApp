package at.htl_villach.chatapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import at.htl_villach.chatapplication.bll.User;

public class EditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private StorageReference storageReference;
    public static final int PICK_IMAGE = 1;
    public static final int TAKE_PICTURE = 2;
    CircularImageView profilePicture;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    TextInputEditText etxtUsername;
    TextInputEditText etxtFullname;
    private Uri imageUri;

    private final long MAX_DOWNLOAD_IMAGE = 1024 * 1024 * 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        profilePicture = (CircularImageView) findViewById(R.id.profilePicture);
        storageReference = FirebaseStorage.getInstance().getReference();


        profilePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final String[] items = {"Choose from Gallery", "Open Camera", "Delete Current Picture"};
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                checkPermissionAndPickPhotoIfGranted();

                                break;
                            case 1:
                                checkPermissionAndTakePhotoIfGranted();
                                break;
                            case 2:
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Something unexpected happened", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                builder.show();

            }
        });


        firebaseAuth = FirebaseAuth.getInstance().getInstance();
        user = firebaseAuth.getCurrentUser();
        fillTextfields(user.getUid());
        loadImage();

    }

    private void loadImage() {
        storageReference.child(user.getUid() + "/profilePicture.jpg").getBytes(MAX_DOWNLOAD_IMAGE)
        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, profilePicture.getWidth(),
                        profilePicture.getHeight(), false));
            }
        });
    }

    private void fillTextfields(String uid) {
        etxtUsername = findViewById(R.id.etxtUsername);
        etxtFullname = findViewById(R.id.etxtFullName);
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        etxtFullname.setText(user.getFullname());
                        etxtUsername.setText(user.getUsername());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }

                });
    }

    public void saveProfile(View view) {
        HashMap<String, Object> updateUser = new HashMap<>();
        updateUser.put("username", etxtUsername.getText().toString());
        updateUser.put("fullname", etxtFullname.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("Users").child( user.getUid()).updateChildren(updateUser);
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        intent.putExtra("allowBack", false);
        startActivity(intent);

    }

    public void onCancel(View view) {
        this.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            if(resultCode == Activity.RESULT_OK) {
                if (requestCode == PICK_IMAGE) {
                    imageUri = data.getData();
                    checkOrientation();
                    saveImageToDatabase();
                } else if (requestCode == TAKE_PICTURE) {
                    checkOrientation();
                    saveImageToDatabase();
                }
            }
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void checkOrientation() throws Exception {
        ExifInterface exif = new ExifInterface(imageUri.getPath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
        bitmap = createSquaredBitmap(bitmap);

        if(orientation == 6) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            FileOutputStream out = new FileOutputStream(imageUri.getPath());
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
        } else {
            FileOutputStream out = new FileOutputStream(imageUri.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
        }
    }

    private Bitmap createSquaredBitmap(Bitmap source) {
        int dim = Math.max(source.getWidth(), source.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(source, (dim - source.getWidth()) / 2, (dim - source.getHeight()) / 2, null);

        return dstBmp;
    }

    private void saveImageToDatabase() {
        storageReference.child(user.getUid() + "/profilePicture.jpg").putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkPermissionAndTakePhotoIfGranted() {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, PERMISSIONS_STORAGE, REQUEST_TAKE_PHOTO);
        } else {
            captureImage();
        }
    }

    private void checkPermissionAndPickPhotoIfGranted() {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, PERMISSIONS_STORAGE, REQUEST_PICK_PHOTO);
        } else {
            pickImage();
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "profilePicture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose a Picture"), PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_TAKE_PHOTO) {
            int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permission == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        } else if(requestCode == REQUEST_PICK_PHOTO) {
            int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permission == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        }
    }
}
