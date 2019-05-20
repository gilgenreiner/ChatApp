package at.htl_villach.chatapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.bll.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class SelectListAdapter extends BaseAdapter {
    ArrayList<User> contacts;
    Boolean[] checked;
    LayoutInflater inflater;
    DatabaseReference database;
    FirebaseAuth firebaseAuth;


    public SelectListAdapter(Context applicationContext, ArrayList<User> contacts) {
        this.contacts = contacts;
        this.inflater = (LayoutInflater.from(applicationContext));
        checked = new Boolean[contacts.size()];
        for(int i=0; i<checked.length; i++) {
            checked[i] = false;
        }
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.select_list, null);
        TextView item = convertView.findViewById(R.id.txtName);
        TextView subitem = convertView.findViewById(R.id.txtUsername);
        CircleImageView image = (CircleImageView) convertView.findViewById(R.id.list_picture);
        final CheckBox cbSelectItem = convertView.findViewById(R.id.cbSelectItem);
        final int fposition = position;

        cbSelectItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked[fposition] = cbSelectItem.isChecked();
            }
        });

        item.setText(contacts.get(position).getFullname());

        subitem.setText(contacts.get(position).getUsername());

        /* TODO: Rewrite
        if(contacts.get(position).getProfilePicture() == 0) {
            image.setImageResource(R.drawable.standard_picture);
        }*/
        return convertView;
    }

    public void renewBooleans() {
        checked = new Boolean[contacts.size()];
        for(int i=0; i<checked.length; i++) {
            checked[i] = false;
        }
    }

    public Boolean[] getChecked() {
        return checked;
    }

}
