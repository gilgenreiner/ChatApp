package at.htl_villach.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
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

    private Chat currentChat;
    private User selectedUser;

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
    DatabaseReference referenceMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        currentChat = (Chat) intent.getParcelableExtra("selectedChat");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(currentChat.getReceiver(fuser.getUid()));
        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //get other User you are chatting with
                selectedUser = snapshot.getValue(User.class);

                //set Layouts with user data
                toolbarTitle.setText(selectedUser.getFullname());
                //toDo: Custom ProfilPicture
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

        toolbar.setOnClickListener(new View.OnClickListener() {
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

                if (!msg.trim().equals("")){
                    sendMessage(currentChat.getId(), fuser.getUid(), msg);
                } else {
                    Toast.makeText(ChatActivity.this, R.string.emptyMessage, Toast.LENGTH_SHORT).show();
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

    private void sendMessage(String chatId, String sender, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String messageId = reference.child("Messages").child(chatId).push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", messageId);
        hashMap.put("sender", sender);
        hashMap.put("message", message);
        hashMap.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

        reference.child("Messages").child(chatId).child(messageId).setValue(hashMap);
    }

    private void readMessages(final String chat_id){
        mMessages = new ArrayList<Message>();

        referenceMessages = FirebaseDatabase.getInstance().getReference("Messages").child(chat_id);
        referenceMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    mMessages.add(snapshot.getValue(Message.class));

                    chatAdapter = new ChatAdapter(ChatActivity.this, mMessages, selectedUser);
                    recyclerViewMessages.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
