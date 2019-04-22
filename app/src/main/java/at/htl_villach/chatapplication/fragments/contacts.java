package at.htl_villach.chatapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

import at.htl_villach.chatapplication.ProfileActivity;
import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.adapters.ContactAdapter;
import at.htl_villach.chatapplication.bll.User;


public class contacts extends Fragment {
    private ArrayList<User> arrUsers;
    private ArrayList<String> arrFriends;
    private ContactAdapter adapter;
    private SwipeRefreshLayout srLayout;
    FirebaseFirestore database;
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        srLayout = rootView.findViewById(R.id.srLayout);

        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        arrUsers = new ArrayList<User>();
        arrFriends = new ArrayList<>();

        adapter = new ContactAdapter(getContext(), arrUsers);

        final ListView lvContacts = rootView.findViewById(R.id.lvContacts);
        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFriendsFromDatabase();
            }
        });


        lvContacts.setAdapter(adapter);

        getFriendsFromDatabase();

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

    public void RefreshList() {
        srLayout.setRefreshing(true);
        getFriendsFromDatabase();
    }

    private void getFriendsFromDatabase() {

        DocumentReference currUser = database.collection("users").document(firebaseAuth.getCurrentUser().getDisplayName());
        currUser.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                arrUsers.clear();
                                arrFriends = (ArrayList<String>) document.getData().get("friends");
                                if(arrFriends == null) {
                                    arrFriends = new ArrayList<>();
                                }
                                loadFriendDetails();
                            }  else {
                                Toast.makeText(getContext(), "Current User not found in Database.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Database error. Try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadFriendDetails() {
        int i = 1;
        for(String name : arrFriends) {
            srLayout.setRefreshing(true);
            DocumentReference userToAdd = database.collection("users").document(name);
            userToAdd.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            srLayout.setRefreshing(false);
                            if(task.isSuccessful()) {
                                DocumentSnapshot user = task.getResult();
                                if(user.exists()) {
                                    arrUsers.add(user.toObject(User.class));
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Toast.makeText(getContext(), "Database error. Try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
