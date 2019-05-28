package at.htl_villach.chatapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import at.htl_villach.chatapplication.adapters.MessageSeenByListAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;

public class MessageInfoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvMessageBody;
    private ListView lvUser;
    private TextView tvTime;
    private MessageSeenByListAdapter adapter;

    private DatabaseReference mRootRef;

    private Message mSelectedMessage;
    private Chat mSelectedChat;
    private ArrayList<User> mUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_info);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mSelectedMessage = getIntent().getParcelableExtra("selectedMessage");
        mSelectedChat = getIntent().getParcelableExtra("selectedChat");

        //todo probieren mit chat.users zu arbeiten!
        DatabaseReference messagesSeenByRef = mRootRef.child("MessagesSeenBy").child(mSelectedChat.getId()).child(mSelectedMessage.getId());
        messagesSeenByRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DatabaseReference usersRef = mRootRef.child("Users").child(ds.getKey());
                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            mUsers.add(snapshot.getValue(User.class));
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

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

        adapter = new MessageSeenByListAdapter(MessageInfoActivity.this, mUsers, mSelectedChat, mSelectedMessage);

        lvUser = findViewById(R.id.list);
        lvUser.setAdapter(adapter);
    }
}
