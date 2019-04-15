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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddUserActivity extends AppCompatActivity {
    FirebaseFirestore database;

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
        database = FirebaseFirestore.getInstance();

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
                if(!input.isEmpty()) {
                    DocumentReference userRef = database.collection("users").document(input);
                    userRef.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        layout_user_found.setVisibility(View.VISIBLE);
                                        DocumentSnapshot user = task.getResult();
                                        if(user.exists()) {
                                            String fullname = user.getData().get("name").toString();
                                            txtFullName.setText(fullname);
                                            txtUsername.setText(input);

                                        }
                                    }
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
                btnAddUser.setVisibility(View.INVISIBLE);
                imgCheck.setVisibility(View.VISIBLE);
            }
        });

//        searchUser.setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String input = searchUser.getQuery().toString();
//                if(!input.isEmpty()) {
//                    DocumentReference userRef = database.collection("users").document(input);
//                    userRef.get()
//                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if(task.isSuccessful()) {
//                                        layout_user_found.setVisibility(View.VISIBLE);
//                                        DocumentSnapshot user = task.getResult();
//                                        if(user.exists()) {
//                                            String fullname = user.getData().get("firstname").toString() + user.getData().get("lastname").toString();
//                                            txtFullName.setText(fullname);
//                                            txtUsername.setText(input);
//
//                                        }
//                                    }
//                                }
//                            });
//
//                }
//            }
//        });
    }
}
