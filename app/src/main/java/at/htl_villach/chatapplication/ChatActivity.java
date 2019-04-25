package at.htl_villach.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.adapters.ChatAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private User selectedContact;       //change to currentChat
    private User currentUser;

    private List<Message> mMessages = new ArrayList<Message>();

    //toolbar
    Toolbar toolbar;
    TextView toolbarTitle;
    ImageView toolbarActionBack;
    CircleImageView toolbarPicture;

    //normal controlls
    EditText messageToSend;
    ImageButton btnSend;
    RecyclerView recyclerViewMessages;

    //Database
    FirebaseUser fuser;
    //DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        selectedContact = (User) intent.getParcelableExtra("selectedContact");

        toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        toolbarActionBack = (ImageView) findViewById(R.id.toolbar_back);
        toolbarPicture = (CircleImageView) findViewById(R.id.toolbar_profilpicture);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarActionBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(selectedContact.getProfilePicture() != 0) {
            toolbarPicture.setImageResource(selectedContact.getProfilePicture());
        }

        toolbarPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChatActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_profilpicture, null);

                ImageView image = (ImageView) mView.findViewById(R.id.dialog_profilePicture);
                TextView title = (TextView) mView.findViewById(R.id.dialog_title);

                if(selectedContact.getProfilePicture() != 0) {
                    image.setImageResource(selectedContact.getProfilePicture());
                }

                title.setText(selectedContact.getFullname());

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });

        toolbarTitle.setText(selectedContact.getFullname());
        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedContact);
                startActivity(intent);
            }
        });

        messageToSend = (EditText) findViewById(R.id.message_to_send);

        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendMessage("mkleinegger", "1", messageToSend.getText().toString());
            }
        });

        recyclerViewMessages = (RecyclerView) findViewById(R.id.recycler_view_messages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);
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

    private void sendMessage(String sender, String chat_id, String message){
        Message message1 = new Message(sender, chat_id, message, "12:20");
        mMessages.add(message1);

        ChatAdapter chatAdapter = new ChatAdapter(ChatActivity.this, mMessages);
        recyclerViewMessages.setAdapter(chatAdapter);
    }
}
