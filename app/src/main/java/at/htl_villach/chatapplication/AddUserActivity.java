package at.htl_villach.chatapplication;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import at.htl_villach.chatapplication.bll.User;

public class AddUserActivity extends AppCompatActivity {
    DatabaseReference database;
    FirebaseAuth firebaseAuth;
    User userFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Toolbar toolbar = findViewById(R.id.toolbarAddUser);
        toolbar.setTitle("Add new User");
        toolbar.setNavigationIcon(R.drawable.ic_acion_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        final LinearLayout layout_user_found = findViewById(R.id.layout_user_found);
        final TextView txtFullName = findViewById(R.id.txtFullName);
        final TextView txtUsername = findViewById(R.id.txtUsername);
        final Button btnAddUser = findViewById(R.id.btnAddUser);
        final ImageView imgCheck = findViewById(R.id.imgCheck);

        layout_user_found.setVisibility(View.INVISIBLE);
        imgCheck.setVisibility(View.INVISIBLE);

        final SearchView searchUser = findViewById(R.id.searchUser);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchUser.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchUser.setIconifiedByDefault(false);

        searchUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String input = query;
                layout_user_found.setVisibility(View.INVISIBLE);
                btnAddUser.setVisibility(View.VISIBLE);
                imgCheck.setVisibility(View.INVISIBLE);
                if(!input.isEmpty()) {
                    database.orderByChild("username")
                                .equalTo(input)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        HashMap<String, HashMap<String,String>> user = (HashMap<String,HashMap<String,String>>) dataSnapshot.getValue();
                                        if(user != null) {
                                            final User userObject = new User();
                                            for(String key : user.keySet()) {
                                                userObject.setUsername(user.get(key).get("username"));
                                                userObject.setFullname(user.get(key).get("fullname"));
                                                userObject.setEmail(user.get(key).get("email"));
                                                userObject.setId(user.get(key).get("id"));
                                            }

                                            database.child(firebaseAuth.getCurrentUser().getUid()).child("friends")
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            HashMap<String, Boolean> friends = (HashMap<String, Boolean>) dataSnapshot.getValue();
                                                            boolean alreadyAdded = false;
                                                            if (friends != null) {
                                                                for (String key : friends.keySet()) {
                                                                    if (key.equals(userObject.getId())) {
                                                                        alreadyAdded = true;
                                                                    }
                                                                }
                                                            }

                                                            if (alreadyAdded) {
                                                                btnAddUser.setVisibility(View.INVISIBLE);
                                                                imgCheck.setVisibility(View.VISIBLE);
                                                            }
                                                            if(!firebaseAuth.getCurrentUser().getUid().equals(userObject.getId())) {
                                                                layout_user_found.setVisibility(View.VISIBLE);
                                                                txtFullName.setText(userObject.getFullname());
                                                                txtUsername.setText(userObject.getUsername());
                                                                userFound = userObject;
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "You cannot add yourself", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });


                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = database.child(firebaseAuth.getCurrentUser().getUid()).child("friends");
                HashMap<String, Object> newFriend = new HashMap<>();
                newFriend.put(userFound.getId(), true);
                ref.updateChildren(newFriend)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        btnAddUser.setVisibility(View.INVISIBLE);
                        imgCheck.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }
}
