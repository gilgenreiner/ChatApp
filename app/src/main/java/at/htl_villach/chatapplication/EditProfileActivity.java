package at.htl_villach.chatapplication;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;

import at.htl_villach.chatapplication.bll.User;

public class EditProfileActivity extends AppCompatActivity {
    CircularImageView profilePicture;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    TextInputEditText etxtUsername;
    TextInputEditText etxtFullname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        profilePicture = (CircularImageView) findViewById(R.id.profilePicture);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(EditProfileActivity.this, "Onclick on Profilepicture",
                        Toast.LENGTH_SHORT).show();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance().getInstance();
        user = firebaseAuth.getCurrentUser();
        fillTextfields(user.getUid());

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
}
