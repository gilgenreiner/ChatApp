package at.htl_villach.chatapplication.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

public class ChatroomFragment extends Fragment {

    public static int TOTAL_ITEMS_TO_LOAD = 10;

    //controlls
    private EditText messageToSend;
    private ImageButton btnSend;
    private RecyclerView recyclerViewMessages;
    private ChatroomAdapter chatroomAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    //Database
    private FirebaseUser fuser;
    private DatabaseReference mRootRef;
    private ValueEventListener valueEventListener;
    private HashMap<DatabaseReference, ValueEventListener> databaseListeners = new HashMap<>();

    //data
    private Chat mCurrentChat;
    private List<Message> mMessages = new ArrayList<>();
    private int mCurrentPage = 1;
    private int mItemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

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
        mCurrentChat = getArguments().getParcelable("selectedChat");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        messageToSend = (EditText) view.findViewById(R.id.message_to_send);

        btnSend = (ImageButton) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageToSend.getText().toString();

                if (!msg.trim().equals("")) {
                    sendMessage(msg.trim());
                } else {
                    Toast.makeText(getActivity(), R.string.emptyMessage, Toast.LENGTH_SHORT).show();
                }

                messageToSend.setText("");
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        chatroomAdapter = new ChatroomAdapter(getContext(), mMessages, mCurrentChat);

        recyclerViewMessages = (RecyclerView) view.findViewById(R.id.recycler_view_messages);
        recyclerViewMessages.setNestedScrollingEnabled(false);

        recyclerViewMessages.setLayoutManager(linearLayoutManager);
        recyclerViewMessages.setAdapter(chatroomAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                mItemPos = 0;

                readMoreMessages();
            }
        });

        readMessages();
        updateSeenMessage();

        return view;
    }

    private void readMoreMessages() {
        DatabaseReference messagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        Query messageQuery = messagesRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);

                if (!mPrevKey.equals(message.getId())) {
                    mMessages.add(mItemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                if (mItemPos == 1) {
                    mLastKey = message.getId();
                }

                if (!message.getSender().equals(fuser.getUid())) {
                    mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(message.getId()).child(fuser.getUid()).setValue(true);
                }

                chatroomAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                //todo: springen zur richtigen position
                //linearLayoutManager.scrollToPositionWithOffset(0, 10);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message m = dataSnapshot.getValue(Message.class);

                mMessages.remove(m);
                chatroomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        DatabaseReference messagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        Query messageQuery = messagesRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);

                    mItemPos++;

                    if (mItemPos == 1) {
                        mLastKey = message.getId();
                        mPrevKey = mLastKey;
                    }

                    if (!message.getSender().equals(fuser.getUid()) && !message.isIsseen()) {
                        mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(message.getId()).child(fuser.getUid()).setValue(true);
                    }

                    mMessages.add(message);
                }
                chatroomAdapter.notifyDataSetChanged();

                recyclerViewMessages.scrollToPosition(mMessages.size() - 1);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseListeners.put(messagesRef, valueEventListener);
    }

    private void updateSeenMessage() {
        DatabaseReference seenMessagesRef = FirebaseDatabase.getInstance().getReference("MessagesSeenBy").child(mCurrentChat.getId());
        seenMessagesRef.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, Object>> messageSeenBy = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();

                for (Message m : mMessages) {
                    if (!m.isIsseen()) {
                        if (!messageSeenBy.get(m.getId()).containsValue(false)) {
                            mRootRef.child("Messages").child(mCurrentChat.getId()).child(m.getId()).child("isseen").setValue(true);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseListeners.put(seenMessagesRef, valueEventListener);
    }

    private void sendMessage(String message) {
        DatabaseReference sendMessagesRef = mRootRef.child("Messages").child(mCurrentChat.getId());
        String messageId = sendMessagesRef.push().getKey();

        HashMap<String, Object> hashMapMessage = new HashMap<>();
        hashMapMessage.put("id", messageId);
        hashMapMessage.put("sender", fuser.getUid());
        hashMapMessage.put("message", message);
        hashMapMessage.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        hashMapMessage.put("isseen", false);

        sendMessagesRef.child(messageId).updateChildren(hashMapMessage);

        HashMap<String, Object> hashMapMessageSeenBy = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : mCurrentChat.getUsers().entrySet()) {
            if (!entry.getKey().equals(fuser.getUid())) {
                hashMapMessageSeenBy.put(entry.getKey(), false);
            }
        }
        mRootRef.child("MessagesSeenBy").child(mCurrentChat.getId()).child(messageId).setValue(hashMapMessageSeenBy);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : databaseListeners.entrySet()) {
            DatabaseReference databaseReference = entry.getKey();
            ValueEventListener value = entry.getValue();
            databaseReference.removeEventListener(value);
        }
    }
}
