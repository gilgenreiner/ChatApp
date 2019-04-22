package at.htl_villach.chatapplication;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import at.htl_villach.chatapplication.bll.User;

public class ChatActivity extends AppCompatActivity {

    private User selectedContact;
    private User currentUser;

    //toolbar
    Toolbar toolbar;
    TextView toolbarTextView;
    ImageView toolbarImageView;

    //normal controlls
    EditText messageToSend;
    ImageButton btnSend;
    RecyclerView recyclerViewMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        selectedContact = (User) intent.getParcelableExtra("selectedContact");

        toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        toolbarImageView = (ImageView) toolbar.findViewById(R.id.toolbarImageView);
        toolbarTextView = (TextView) toolbar.findViewById(R.id.toolbarTextView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_acion_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbarTextView.setText(selectedContact.getName());
        toolbarTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedContact);
                startActivity(intent);
            }
        });
        //ToDo image
        /*if(selectedContact.getProfilePicture() == 0) {
            toolbarImageView.setImageResource(R.drawable.standard_picture);
        } else {
            toolbarImageView.setImageResource(selectedContact.getProfilePicture());
        }*/

        EditText messageToSend = (EditText) findViewById(R.id.message_to_send);
        ImageButton btnSend = (ImageButton) findViewById(R.id.btn_send);
        RecyclerView recyclerViewMessages = (RecyclerView) findViewById(R.id.recycler_view_messages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuChatProfil:
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedContact);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
