package at.htl_villach.chatapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import at.htl_villach.chatapplication.ChatActivity;
import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.adapters.ChatListAdapter;
import at.htl_villach.chatapplication.adapters.ContactListAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.User;


public class chats extends Fragment {
    private ArrayList<Chat> arrChats;
    private ChatListAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        arrChats = new ArrayList<>();

        adapter = new ChatListAdapter(getContext(), arrChats);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Chats");

        final ListView lvChats = rootView.findViewById(R.id.lvChats);
        lvChats.setAdapter(adapter);

        database.orderByChild("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Chat> tempChat = new ArrayList<>();
                        HashMap<String, Object> chats = (HashMap<String,Object>) dataSnapshot.getValue();
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if(chats != null) {
                            for(String key : chats.keySet()) {
                                HashMap<String, Object> curObj = (HashMap<String, Object>) chats.get(key);
                                HashMap<String, String> userPair = (HashMap<String, String>) curObj.get("users");
                                if(userPair.containsKey(currentUser.getUid())) {
                                    tempChat.add(new Chat(key, userPair));
                                }
                            }

                            if(!tempChat.isEmpty()) {
                                arrChats.clear();
                                arrChats.addAll(tempChat);
                                adapter.notifyDataSetChanged();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //insertTestData();


        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                //User contact = (User)adapter.getItemAtPosition(position);
                HashMap<String, String> users = new HashMap<>();
                users.put("4Hsy0jFb2oPqrG8uAySRIALj7kW2", "testuser1");
                users.put("qdhpKvPejtSpMhOl1KXbj83AxtJ3", "testuser2");
                Chat chat = new Chat("1", users);
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                intent.putExtra("selectedChat", chat);

                startActivity(intent);

            }
        });

        return rootView;

    }

    private void insertTestData() {
        /*arrUsers.add(new User("Max Mustermann1", "mustermann@gmail.com","muster373"));
        arrUsers.add(new User("Max Mustermann2", "mustermann@gmail.com", "muster333"));
        arrUsers.add(new User("Max Mustermann3", "mustermann@gmail.com", "muster3563"));
        arrUsers.add(new User("Max Mustermann4", "mustermann@gmail.com", "muster3452"));
        arrUsers.add(new User("Max Mustermann5", "mustermann@gmail.com", "muster3766"));
        arrUsers.add(new User("Max Mustermann6", "mustermann@gmail.com", "muster3235463"));
        arrUsers.add(new User("Max Mustermann7", "mustermann@gmail.com", "muster37765"));

        arrLastMessage.add("Hallo1");
        arrLastMessage.add("Hallo2");
        arrLastMessage.add("Hallo3");
        arrLastMessage.add("Hallo4");
        arrLastMessage.add("Hallo5");
        arrLastMessage.add("Hallo6");
        arrLastMessage.add("Hallo7");
        adapter.notifyDataSetChanged();*/

    }

}
