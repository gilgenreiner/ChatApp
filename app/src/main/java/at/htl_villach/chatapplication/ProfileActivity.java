package at.htl_villach.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import at.htl_villach.chatapplication.bll.User;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final ImageView profilePicture = findViewById(R.id.profilePicture);
        final TextView txtName = findViewById(R.id.txtName);
        final TextView txtUsername = findViewById(R.id.txtFullName);
        final Toolbar toolProfile = findViewById(R.id.toolProfile);
        Intent intent = getIntent();

        User selectedContact = (User) intent.getParcelableExtra("selectedContact");
        if(selectedContact.getProfilePicture() == 0) {
            profilePicture.setImageResource(R.drawable.standard_picture);
        } else {
            profilePicture.setImageResource(selectedContact.getProfilePicture());
        }
        txtName.setText(selectedContact.getFullname());
        txtUsername.setText(selectedContact.getUsername());
        toolProfile.setTitle("Contact Details");
    }
}
