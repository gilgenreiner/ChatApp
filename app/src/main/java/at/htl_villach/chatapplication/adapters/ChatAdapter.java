package at.htl_villach.chatapplication.adapters;

import android.content.Context;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;

/**
 * Created by pupil on 4/23/19.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    FirebaseUser fuser;

    private Context mContext;
    private List<Message> mMessages;
    private User sender;

    public ChatAdapter(Context mContext, List<Message> mMessages, User sender) {
        this.mMessages = mMessages;
        this.mContext = mContext;
        this.sender = sender;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.my_message, parent, false);
            return new ChatAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.their_message, parent, false);
            return new ChatAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Message message = mMessages.get(position);

        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        cal.setTimeInMillis(message.getTimestamp() * 1000L);
        String date = DateFormat.format("hh:mm", cal).toString();

        holder.messageBody.setText(message.getMessage());
        holder.timestamp.setText(date);
        holder.sendFrom.setText(sender.getFullname());

        if(holder.getItemViewType() == MSG_TYPE_RIGHT) {
            holder.sendFrom.setVisibility(View.GONE);
        } else if(holder.getItemViewType() == MSG_TYPE_LEFT){
            if((position - 1) < 0) {
                holder.sendFrom.setVisibility(View.VISIBLE);
            }
            else if(mMessages.get(position - 1).getSender().equals(sender.getId())) {
                holder.sendFrom.setVisibility(View.GONE);
            }else {
                holder.sendFrom.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView messageBody;
        public TextView sendFrom;
        public TextView timestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            messageBody = itemView.findViewById(R.id.message_body);
            sendFrom = itemView.findViewById(R.id.message_username);
            timestamp = itemView.findViewById(R.id.message_timestamp);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessages.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}