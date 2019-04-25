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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.adapters.ChatAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Chat currentChat ;
    private User selectedUser;

    private List<Message> mMessages;

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
    DatabaseReference reference;
    DatabaseReference referenceChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        currentChat = (Chat) intent.getParcelableExtra("selectedChat");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentChat.getReceiver(fuser.getUid()));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //get other User you are chatting with
                selectedUser = snapshot.getValue(User.class);

                //set Layouts with user data
                toolbarTitle.setText(selectedUser.getFullname());
                //toDo: Custom ProfilPicture
                //if(selectedUser.getProfilePicture() != 0) {
                //    toolbarPicture.setImageResource(selectedUser.getProfilePicture());
                //}

                readMesagges(currentChat.getId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        toolbarPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChatActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_profilpicture, null);

                ImageView image = (ImageView) mView.findViewById(R.id.dialog_profilePicture);
                TextView title = (TextView) mView.findViewById(R.id.dialog_title);

                //toDo: Custom ProfilPicture
                //if(selectedUser.getProfilePicture() != 0) {
                //    image.setImageResource(selectedUser.getProfilePicture());
                //}

                title.setText(selectedUser.getUsername());

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });


        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedUser);
                startActivity(intent);
            }
        });

        messageToSend = (EditText) findViewById(R.id.message_to_send);

        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageToSend.getText().toString();

                if (!msg.equals("")){
                    sendMessage(fuser.getUid(), currentChat.getId(), msg);
                } else {
                    Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                messageToSend.setText("");
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
                intent.putExtra("selectedContact", selectedUser);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String sender, String chat_id, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("chatid", chat_id);
        hashMap.put("message", message);
        hashMap.put("timestamp", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));

        reference.child("Messages").push().setValue(hashMap);
    }

    private void readMesagges(final String chat_id){
        mMessages = new ArrayList<Message>();

        referenceChat = FirebaseDatabase.getInstance().getReference("Messages");
        referenceChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    if (message.getChatid().equals(chat_id)) {
                        mMessages.add(message);
                    }

                    ChatAdapter chatAdapter = new ChatAdapter(ChatActivity.this, mMessages, selectedUser);
                    recyclerViewMessages.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
