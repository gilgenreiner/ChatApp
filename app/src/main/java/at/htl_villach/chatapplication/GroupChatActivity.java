package at.htl_villach.chatapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.htl_villach.chatapplication.adapters.ChatroomAdapter;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;
import at.htl_villach.chatapplication.fragments.ChatroomFragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    private final long MAX_DOWNLOAD_IMAGE = 1024 * 1024 * 5;

    //toolbar
    Toolbar toolbar;
    TextView toolbarTitle;
    CircleImageView toolbarPicture;

    //Fragment
    ChatroomFragment chatroom;

    //Database
    FirebaseUser fuser;
    DatabaseReference referenceUsers;
    DatabaseReference referenceGroupchat;
    StorageReference storageReference;

    //Data
    private Chat currentChat;
    private ArrayList<User> groupUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentChat = (Chat) getIntent().getParcelableExtra("selectedChat");

        setGroupchatUsers();

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        referenceGroupchat = FirebaseDatabase.getInstance().getReference("Groups").child(currentChat.getId());
        referenceGroupchat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getValue();
                if (data != null) {
                    toolbarTitle.setText(data.get("title"));
                    toolbarPicture.post(new Runnable() {
                        @Override
                        public void run() {
                            storageReference.child("groups/" + currentChat.getId() + "/profilePicture.jpg").getBytes(MAX_DOWNLOAD_IMAGE)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            //selectedUser.setProfilePictureResource(bytes);
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            toolbarPicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, toolbarPicture.getWidth(),
                                                    toolbarPicture.getHeight(), false));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            toolbarPicture.setImageResource(R.drawable.standard_picture);
                                        }
                                    });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        toolbarPicture = (CircleImageView) findViewById(R.id.toolbar_profilpicture);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_acion_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupChat", currentChat);
                startActivity(intent);
            }
        });

        chatroom = (ChatroomFragment) getSupportFragmentManager().findFragmentById(R.id.chatroom);
        chatroom = ChatroomFragment.newInstance(currentChat);
        getSupportFragmentManager().beginTransaction().add(R.id.chatroom, chatroom).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuChatProfil:
                Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intent.putExtra("test", groupUsers);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setGroupchatUsers() {
        for (String key : currentChat.getUsers().keySet()) {
            referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(key);
            referenceUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    groupUsers.add(snapshot.getValue(User.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
    }
}
