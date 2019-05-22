package at.htl_villach.chatapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.bll.Message;
import at.htl_villach.chatapplication.bll.User;

/**
 * Created by pupil on 4/23/19.
 */

public class ChatroomAdapter extends RecyclerView.Adapter<ChatroomAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private FirebaseUser fuser;
    private DatabaseReference referenceUsers;

    private Activity mActivity;
    private Context mContext;
    private List<Message> mMessages;

    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public ChatroomAdapter(Context mContext, List<Message> mMessages, Activity activity) {
        this.mMessages = mMessages;
        this.mContext = mContext;
        this.mActivity = activity;

        viewBinderHelper.setOpenOnlyOne(true);
    }

    @NonNull
    @Override
    public ChatroomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.my_message, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.their_message, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final @NonNull ChatroomAdapter.ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        viewBinderHelper.bind(holder.swipeLayout, message.getId());

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

        holder.layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (Message m : mMessages) {
                    viewBinderHelper.closeLayout(m.getId());
                }
            }
        });

        if (message.getSender().equals(fuser.getUid())) {
            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    Toolbar t = mActivity.findViewById(R.id.toolbar);
                    // MenuItem m = mActivity.findViewById(R.id.menuDeleteMessage);
                    //m.setVisible(true);
                    //mActivity.findViewById(R.id.menuSendMessageTo).setVisibility(View.VISIBLE);
                    //mActivity.findViewById(R.id.menuMessageInfo).setVisibility(View.VISIBLE);
                    //mActivity.findViewById(R.id.menuChatProfil).setVisibility(View.GONE);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout layout;
        public SwipeRevealLayout swipeLayout;
        public TextView messageBody;
        public TextView sendFrom;
        public TextView datetime;
        public TextView isseen;

        public ViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.message_layout);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
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