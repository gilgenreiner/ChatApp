package at.htl_villach.chatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.adapters.ChatListAdapter;
import at.htl_villach.chatapplication.adapters.MessageSeenByListAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;

public class SendToActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvMessageBody;
    private ListView lvUser;
    private TextView tvTime;
    private ChatListAdapter adapter;

    private DatabaseReference mRootRef;
    private FirebaseAuth firebaseAuth;

    private Message mSelectedMessage;
    private ArrayList<Chat> mChats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to);

        setContentView(R.layout.activity_message_info);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        mSelectedMessage = getIntent().getParcelableExtra("selectedMessage");

        getChatsFromDatabase();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_acion_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvMessageBody = findViewById(R.id.message_body);
        tvMessageBody.setText(mSelectedMessage.getMessage());

        tvTime = findViewById(R.id.message_time);
        tvTime.setText(mSelectedMessage.getTimeAsString());

        adapter = new ChatListAdapter(SendToActivity.this, mChats);

        lvUser = findViewById(R.id.list);
        lvUser.setAdapter(adapter);

        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                final Chat chat = (Chat) adapter.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(SendToActivity.this);

                builder.setPositiveButton(R.string.deletePopUpBtnYes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference sendMessagesRef = mRootRef.child("Messages").child(chat.getId());
                        String messageId = sendMessagesRef.push().getKey();

                        HashMap<String, Object> hashMapMessage = new HashMap<>();
                        hashMapMessage.put("id", messageId);
                        hashMapMessage.put("sender", firebaseAuth.getCurrentUser().getUid());
                        hashMapMessage.put("message", mSelectedMessage.getMessage());
                        hashMapMessage.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                        hashMapMessage.put("isseen", false);

                        sendMessagesRef.child(messageId).updateChildren(hashMapMessage);

                        HashMap<String, Object> hashMapMessageSeenBy = new HashMap<>();
                        for (Map.Entry<String, Boolean> entry : chat.getUsers().entrySet()) {
                            if (!entry.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                                hashMapMessageSeenBy.put(entry.getKey(), false);
                            }
                        }
                        mRootRef.child("MessagesSeenBy").child(chat.getId()).child(messageId).setValue(hashMapMessageSeenBy);

                        finish();
                    }
                });

                builder.setNegativeButton(R.string.deletePopUpBtnNo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder.setTitle(R.string.sendToPopUpTitle);
                builder.setMessage(SendToActivity.this.getResources().getString(R.string.sendToPopUpMessage, mSelectedMessage.getMessage()));

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    private void getChatsFromDatabase() {
        mRootRef.child("Chats").orderByChild("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Chat> tempChat = new ArrayList<>();
                        HashMap<String, Object> chats = (HashMap<String, Object>) dataSnapshot.getValue();
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if (chats != null) {
                            for (String key : chats.keySet()) {
                                HashMap<String, Object> curObj = (HashMap<String, Object>) chats.get(key);
                                HashMap<String, Boolean> userPair = (HashMap<String, Boolean>) curObj.get("users");
                                Boolean isGroupChat = (Boolean) curObj.get("isGroupChat");
                                if (userPair.containsKey(currentUser.getUid())) {
                                    tempChat.add(new Chat(key, userPair, isGroupChat));
                                }
                            }

                            if (!tempChat.isEmpty()) {
                                mChats.clear();
                                mChats.addAll(tempChat);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}
