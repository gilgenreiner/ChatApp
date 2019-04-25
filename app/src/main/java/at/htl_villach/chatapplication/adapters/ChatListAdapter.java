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
import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends BaseAdapter {
    ArrayList<Chat> contacts;
    LayoutInflater inflater;



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
        TextView item = view.findViewById(R.id.txtName);
        TextView subitem = view.findViewById(R.id.txtLastChat);


        CircleImageView image = (CircleImageView) view.findViewById(R.id.list_picture);

        item.setText(contacts.get(i).getId());

        //subitem.setText(contacts.get(i).getUsername());

        //if(contacts.get(i).getProfilePicture() == 0) {
        //    image.setImageResource(R.drawable.standard_picture);
        //}


        //image.setImageResource(flags[i]);
        return view;
    }


}
