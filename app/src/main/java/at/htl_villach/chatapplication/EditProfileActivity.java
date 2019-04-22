package at.htl_villach.chatapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

public class EditProfileActivity extends AppCompatActivity {
    CircularImageView profilePicture;
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

    }
}
