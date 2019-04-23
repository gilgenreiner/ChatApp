package at.htl_villach.chatapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.bll.User;

public class ContactAdapter extends BaseAdapter {
    ArrayList<User> contacts;
    LayoutInflater inflater;



    public ContactAdapter(Context applicationContext, ArrayList<User> contacts) {
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
        view = inflater.inflate(R.layout.activity_list, null);
        TextView item = view.findViewById(R.id.txtName);
        TextView subitem = view.findViewById(R.id.txtLastChat);


        ImageView image = (ImageView) view.findViewById(R.id.profilePicture);
        ImageButton btnBeginChat = view.findViewById(R.id.btnBeginChat);

        item.setText(contacts.get(i).getFullname());

        subitem.setText(contacts.get(i).getUsername());

        if(contacts.get(i).getProfilePicture() == 0) {
            image.setImageResource(R.drawable.standard_picture);
        }

        btnBeginChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //image.setImageResource(flags[i]);
        return view;
    }


}
