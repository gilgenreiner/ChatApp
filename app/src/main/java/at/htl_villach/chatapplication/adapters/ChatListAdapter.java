package at.htl_villach.chatapplication.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends BaseAdapter {
    ArrayList<Chat> contacts;
    LayoutInflater inflater;
    DatabaseReference database;
    FirebaseAuth firebaseAuth;



    public ChatListAdapter(Context applicationContext, ArrayList<Chat> contacts) {
        this.contacts = contacts;
        this.inflater = (LayoutInflater.from(applicationContext));
    }


    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int i) {
        return contacts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.activity_list_chats, null);
        final TextView item = view.findViewById(R.id.txtName);
        TextView subitem = view.findViewById(R.id.txtLastChat);


        CircleImageView image = (CircleImageView) view.findViewById(R.id.list_picture);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        HashMap<String, String> users = contacts.get(i).getUsers();
        String userToLookup = "";
        for(String key : users.keySet()) {
            if(!firebaseAuth.getCurrentUser().getUid().equals(key)) {
                userToLookup = key;
            }
        }
        if(!TextUtils.isEmpty(userToLookup)) {
            database.orderByChild("id")
                    .equalTo(userToLookup)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, HashMap<String,String>> user = (HashMap<String,HashMap<String,String>>) dataSnapshot.getValue();
                            if(user != null) {
                                final User userObject = new User();
                                for (String key : user.keySet()) {
                                    userObject.setUsername(user.get(key).get("username"));
                                    userObject.setFullname(user.get(key).get("fullname"));
                                    userObject.setEmail(user.get(key).get("email"));
                                    userObject.setId(user.get(key).get("id"));
                                }
                                item.setText(userObject.getFullname());
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }


        //subitem.setText(contacts.get(i).getUsername());

        //if(contacts.get(i).getProfilePicture() == 0) {
        //    image.setImageResource(R.drawable.standard_picture);
        //}


        //image.setImageResource(flags[i]);
        return view;
    }


}
