package at.htl_villach.chatapplication.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.Fragment;

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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.adapters.ChatroomAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatroomFragment extends Fragment {

    private Chat currentChat;

    private List<Message> mMessages;

    //normal controlls
    EditText messageToSend;
    ImageButton btnSend;
    RecyclerView recyclerViewMessages;
    ChatroomAdapter chatroomAdapter;
    LinearLayoutManager linearLayoutManager;

    //Database
    FirebaseUser fuser;
    DatabaseReference referenceMessages;

    ValueEventListener seenMessageListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
            for (final DataSnapshot snapshotMessage : dataSnapshot.getChildren()) {
                final Message m = snapshotMessage.getValue(Message.class);

                if (!m.isIsseen()) {
                    DatabaseReference referenceMessageSeen = FirebaseDatabase.getInstance().getReference("MessagesSeenBy").child(m.getId());
                    referenceMessageSeen.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshotMessageSeenBy) {
                            HashMap<String, Object> MessageSeenby = new HashMap<>();
                            for (DataSnapshot ds : dataSnapshotMessageSeenBy.getChildren()) {
                                MessageSeenby.put(ds.getKey(), ds.getValue());
                            }

                            if (MessageSeenby.containsKey(fuser.getUid()) && !m.getSender().equals(fuser.getUid())) {
                                HashMap<String, Object> hashMap2 = new HashMap<>();
                                hashMap2.put(fuser.getUid(), true);
                                dataSnapshotMessageSeenBy.getRef().updateChildren(hashMap2);
                            }

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", (!MessageSeenby.containsValue(false)));
                            snapshotMessage.getRef().updateChildren(hashMap);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public ChatroomFragment() {
        // Required empty public constructor
    }

    public static ChatroomFragment newInstance(Chat selectedChat) {
        ChatroomFragment toDoListFragment = new ChatroomFragment();
        Bundle args = new Bundle();
        args.putParcelable("selectedChat", selectedChat);
        toDoListFragment.setArguments(args);
        return toDoListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentChat = getArguments().getParcelable("selectedChat");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        messageToSend = (EditText) view.findViewById(R.id.message_to_send);
        btnSend = (ImageButton) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageToSend.getText().toString();

                if (!msg.trim().equals("")) {
                    sendMessage(currentChat.getId(), fuser.getUid(), msg.trim());
                } else {
                    Toast.makeText(getActivity(), R.string.emptyMessage, Toast.LENGTH_SHORT).show();
                }

                messageToSend.setText("");
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerViewMessages = (RecyclerView) view.findViewById(R.id.recycler_view_messages);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);

        readMessages(currentChat.getId());
        seenMessage(currentChat.getId());

        return view;
    }

    public void seenMessage(String chat_id) {
        referenceMessages = FirebaseDatabase.getInstance().getReference("Messages").child(chat_id);
        referenceMessages.addValueEventListener(seenMessageListener);
    }

    private void sendMessage(String chatId, String sender, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String messageId = reference.child("Messages").child(chatId).push().getKey();

        HashMap<String, Object> hashMapMessage = new HashMap<>();
        hashMapMessage.put("id", messageId);
        hashMapMessage.put("sender", sender);
        hashMapMessage.put("message", message);
        hashMapMessage.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        hashMapMessage.put("isseen", false);

        reference.child("Messages").child(chatId).child(messageId).setValue(hashMapMessage);

        HashMap<String, Object> hashMapMessageSeenBy = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : currentChat.getUsers().entrySet()) {
            if (!entry.getKey().equals(fuser.getUid())) {
                hashMapMessageSeenBy.put(entry.getKey(), false);
            }
        }

        reference.child("MessagesSeen").child(messageId).setValue(hashMapMessageSeenBy);
    }

    private void readMessages(final String chat_id) {
        mMessages = new ArrayList<Message>();

        referenceMessages = FirebaseDatabase.getInstance().getReference("Messages").child(chat_id);
        referenceMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mMessages.add(snapshot.getValue(Message.class));

                    chatroomAdapter = new ChatroomAdapter(getContext(), mMessages, getActivity());
                    recyclerViewMessages.setAdapter(chatroomAdapter);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        referenceMessages.removeEventListener(seenMessageListener);
    }
}
