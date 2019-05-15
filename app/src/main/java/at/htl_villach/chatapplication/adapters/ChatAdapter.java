package at.htl_villach.chatapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
    DatabaseReference referenceUsers;

    private Context mContext;
    private List<Message> mMessages;
    private User sender;

    public ChatAdapter(Context mContext, List<Message> mMessages) {
        this.mMessages = mMessages;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.my_message, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.their_message, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final @NonNull ChatAdapter.ViewHolder holder, int position) {
        Message message = mMessages.get(position);

        holder.messageBody.setText(message.getMessage());

        referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(message.getSender());
        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                holder.sendFrom.setText(snapshot.getValue(User.class).getFullname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        if (holder.getItemViewType() == MSG_TYPE_LEFT) {
            if ((position - 1) < 0) {
                holder.sendFrom.setVisibility(View.VISIBLE);
            } else if (mMessages.get(position - 1).getSender().equals(message.getSender())) {
                holder.sendFrom.setVisibility(View.GONE);
            } else {
                holder.sendFrom.setVisibility(View.VISIBLE);
            }
        }

        holder.datetime.setText(message.getTimeAsDate());
        if ((position - 1) < 0) {
            holder.datetime.setVisibility(View.VISIBLE);
        } else if (mMessages.get(position - 1).getTimeAsDate().equals(message.getTimeAsDate())) {
            holder.datetime.setVisibility(View.GONE);
        } else {
            holder.datetime.setVisibility(View.VISIBLE);
        }

        if (position == mMessages.size() - 1 && message.getSender().equals(fuser.getUid())) {
            holder.isseen.setVisibility(View.VISIBLE);
            if (message.isIsseen()) {
                holder.isseen.setText("Seen");
            } else {
                holder.isseen.setText("Delivered");
            }
        } else {
            holder.isseen.setVisibility(View.GONE);
        }
    }

    public Message getMessage(int position) {
        return mMessages.get(position);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout layout;
        public TextView messageBody;
        public TextView sendFrom;
        public TextView datetime;
        public TextView isseen;

        public ViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.message_layout);
            datetime = itemView.findViewById(R.id.message_datetime);
            messageBody = itemView.findViewById(R.id.message_body);
            sendFrom = itemView.findViewById(R.id.message_username);
            isseen = itemView.findViewById(R.id.message_seen);
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