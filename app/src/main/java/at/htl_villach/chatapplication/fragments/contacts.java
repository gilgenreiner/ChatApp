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

import at.htl_villach.chatapplication.ProfileActivity;
import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.adapters.ContactAdapter;
import at.htl_villach.chatapplication.bll.User;


public class contacts extends Fragment {
    private ArrayList<User> arrUsers;
    private ContactAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        arrUsers = new ArrayList<User>();

        adapter = new ContactAdapter(getContext(), arrUsers);

        final ListView lvContacts = rootView.findViewById(R.id.lvContacts);
        lvContacts.setAdapter(adapter);
        insertTestData();

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                User contact = (User)adapter.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ProfileActivity.class);

                intent.putExtra("selectedContact", contact);

                startActivity(intent);

            }
        });

        return rootView;

    }

    private void insertTestData() {
        arrUsers.add(new User("Max Mustermann1", "mustermann@gmail.com", 0, "muster373", "Status1", "passwort123"));
        arrUsers.add(new User("Max Mustermann2", "mustermann@gmail.com", 0, "muster373", "Status2", "passwort123"));
        arrUsers.add(new User("Max Mustermann3", "mustermann@gmail.com", 0, "muster373", "Status3", "passwort123"));
        arrUsers.add(new User("Max Mustermann4", "mustermann@gmail.com", 0, "muster373", "Status4", "passwort123"));
        arrUsers.add(new User("Max Mustermann5", "mustermann@gmail.com", 0, "muster373", "Status5", "passwort123"));
        arrUsers.add(new User("Max Mustermann6", "mustermann@gmail.com", 0, "muster373", "Status6", "passwort123"));
        arrUsers.add(new User("Max Mustermann7", "mustermann@gmail.com", 0, "muster373", "Status7", "passwort123"));


        adapter.notifyDataSetChanged();

    }

}
