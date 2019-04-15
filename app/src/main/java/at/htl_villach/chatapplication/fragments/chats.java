package at.htl_villach.chatapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import at.htl_villach.chatapplication.ChatActivity;
import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.adapters.ContactAdapter;
import at.htl_villach.chatapplication.bll.User;


public class chats extends Fragment {
    private ArrayList<User> arrUsers;
    private ArrayList<String> arrLastMessage;
    private ContactAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        arrUsers = new ArrayList<User>();
        arrLastMessage = new ArrayList<String>();

        adapter = new ContactAdapter(getContext(), arrUsers, arrLastMessage);

        final ListView lvChats = rootView.findViewById(R.id.lvChats);
        lvChats.setAdapter(adapter);
        insertTestData();


        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                User contact = (User)adapter.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                intent.putExtra("selectedContact", contact);

                startActivity(intent);

            }
        });

        return rootView;

    }

    private void insertTestData() {
        arrUsers.add(new User("Max Mustermann1", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann2", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann3", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann4", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann5", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann6", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann7", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));

        arrLastMessage.add("Hallo1");
        arrLastMessage.add("Hallo2");
        arrLastMessage.add("Hallo3");
        arrLastMessage.add("Hallo4");
        arrLastMessage.add("Hallo5");
        arrLastMessage.add("Hallo6");
        arrLastMessage.add("Hallo7");
        arrLastMessage.add("Hallo8");
        arrLastMessage.add("Hallo9");
        adapter.notifyDataSetChanged();

    }

}
