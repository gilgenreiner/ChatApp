package at.htl_villach.chatapplication;

import android.content.Intent;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.file.attribute.GroupPrincipal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.adapters.ChatAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    private Chat currentChat;
    private HashMap<String, User> groupUsers;

    private List<Message> mMessages;

    //toolbar
    Toolbar toolbar;
    TextView toolbarTitle;
    CircleImageView toolbarPicture;

    //normal controlls
    EditText messageToSend;
    ImageButton btnSend;
    RecyclerView recyclerViewMessages;
    ChatAdapter chatAdapter;
    SwipeController swipeController;
    LinearLayoutManager linearLayoutManager;

    //Database
    FirebaseUser fuser;
    DatabaseReference referenceUsers;
    DatabaseReference referenceGroupchat;
    DatabaseReference referenceMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Intent intent = getIntent();
        currentChat = (Chat) intent.getParcelableExtra("selectedChat");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        groupUsers = getGroupchatUsers();

        referenceGroupchat = FirebaseDatabase.getInstance().getReference("Groups").child(currentChat.getId());
        referenceGroupchat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               HashMap<String,String> data = (HashMap<String,String>) dataSnapshot.getValue();
                if(data != null) {
                   toolbarTitle.setText(data.get("title"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        toolbarPicture = (CircleImageView) findViewById(R.id.toolbar_profilpicture);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_acion_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //toDo: loadProfilPicture
        //toolbarPicture.setImageResource();

        /*toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedUser);
                startActivity(intent);
            }
        });*/

        messageToSend = (EditText) findViewById(R.id.message_to_send);

        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageToSend.getText().toString();

                if (!msg.trim().equals("")) {
                    sendMessage(currentChat.getId(), fuser.getUid(), msg);
                } else {
                    Toast.makeText(GroupChatActivity.this, R.string.emptyMessage, Toast.LENGTH_SHORT).show();
                }

                messageToSend.setText("");
            }
        });

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerViewMessages = (RecyclerView) findViewById(R.id.recycler_view_messages);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);

        readMessages(currentChat.getId());

        swipeController = new SwipeController();
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerViewMessages);
        recyclerViewMessages.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c, chatAdapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuChatProfil:
                Intent intent = new Intent(GroupChatActivity.this, ProfileActivity.class);
                //intent.putExtra("selectedContact", selectedUser);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String chatId, String sender, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String messageId = reference.child("Messages").child(chatId).push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", messageId);
        hashMap.put("sender", sender);
        hashMap.put("message", message);
        hashMap.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

        reference.child("Messages").child(chatId).child(messageId).setValue(hashMap);
    }

    private void readMessages(final String chat_id) {
        mMessages = new ArrayList<Message>();

        referenceMessages = FirebaseDatabase.getInstance().getReference("Messages").child(chat_id);
        referenceMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message m = snapshot.getValue(Message.class);
                    mMessages.add(m);

                    chatAdapter = new ChatAdapter(GroupChatActivity.this, mMessages, groupUsers.get(m.getSender()));
                    recyclerViewMessages.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private HashMap<String, User> getGroupchatUsers() {
        final HashMap<String, User> result = new HashMap<String, User>();

        for (final String key : currentChat.getUsers().keySet()) {
            referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(key);
            referenceUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    result.put(key, snapshot.getValue(User.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

        return result;
    }
}
