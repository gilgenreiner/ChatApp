package at.htl_villach.chatapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import at.htl_villach.chatapplication.MessageInfoActivity;
import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.SendToActivity;
import at.htl_villach.chatapplication.bll.Chat;
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

    private ViewBinderHelper mViewBinderHelper = new ViewBinderHelper();
    private Context mContext;
    private ActionMode mActionMode;
    private Boolean mActionModeOn = false;
    private Chat mCurrentChat;
    private Message mSelectedMessage;
    private List<Message> mMessages;

    public ChatroomAdapter(Context mContext, List<Message> mMessages, Chat chat) {
        this.mMessages = mMessages;
        this.mContext = mContext;
        this.mCurrentChat = chat;

        mViewBinderHelper.setOpenOnlyOne(true);
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
    public void onBindViewHolder(final @NonNull ChatroomAdapter.ViewHolder holder, final int position) {

        final Message m = mMessages.get(position);

        mViewBinderHelper.bind(holder.swipeLayout, m.getId());

        holder.messageBody.setText(m.getMessage());

        referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(m.getSender());
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
            } else if (mMessages.get(position - 1).getSender().equals(m.getSender())) {
                holder.sendFrom.setVisibility(View.GONE);
            } else {
                holder.sendFrom.setVisibility(View.VISIBLE);
            }
        }

        holder.datetime.setText(m.getTimeAsDate());
        if ((position - 1) < 0) {
            holder.datetime.setVisibility(View.VISIBLE);
        } else if (mMessages.get(position - 1).getTimeAsDate().equals(m.getTimeAsDate())) {
            holder.datetime.setVisibility(View.GONE);
        } else {
            holder.datetime.setVisibility(View.VISIBLE);
        }

        if (position == mMessages.size() - 1 && m.getSender().equals(fuser.getUid())) {
            holder.isseen.setVisibility(View.VISIBLE);
            if (m.isIsseen()) {
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
                for (Message message : mMessages) {
                    mViewBinderHelper.closeLayout(message.getId());
                }
            }
        });

            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (mActionMode == null && !mActionModeOn) {
                        mSelectedMessage = m;
                        mActionModeOn = true;
                        mActionMode = ((Activity) mContext).startActionMode(new ActionBarCallback());
                    } else {
                        finishActionMode();
                    }

                    return true;
                }
            });
    }

    private void finishActionMode() {
        mActionMode.finish();
        mActionMode = null;
        mActionModeOn = false;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
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

    private class ActionBarCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_chat, menu);

            if (!mCurrentChat.getGroupChat()) {
                menu.findItem(R.id.menuMessageInfo).setVisible(false);
            } else {
                menu.findItem(R.id.menuMessageInfo).setVisible(true);
            }

            if (!mSelectedMessage.getSender().equals(fuser.getUid())) {
                menu.findItem(R.id.menuDeleteMessage).setVisible(false);
                menu.findItem(R.id.menuMessageInfo).setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuMessageInfo:
                    Intent intent = new Intent(mContext, MessageInfoActivity.class);
                    intent.putExtra("selectedMessage", mSelectedMessage);
                    intent.putExtra("selectedChat", mCurrentChat);
                    mContext.startActivity(intent);
                    break;
                case R.id.menuDeleteMessage:

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setPositiveButton(R.string.deletePopUpBtnYes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseDatabase.getInstance().getReference().child("MessagesSeenBy").child(mSelectedMessage.getId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrentChat.getId()).child(mSelectedMessage.getId()).removeValue();
                        }
                    });

                    builder.setNegativeButton(R.string.deletePopUpBtnNo, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    builder.setTitle(R.string.deletePopUpTitle);
                    builder.setMessage(mContext.getResources().getString(R.string.deletePopUpMessage, mSelectedMessage.getMessage()));

                    AlertDialog dialog = builder.create();

                    dialog.show();
                    break;
                case R.id.menuSendMessageTo:
                    Intent intent2 = new Intent(mContext, SendToActivity.class);
                    intent2.putExtra("selectedMessage", mSelectedMessage);
                    mContext.startActivity(intent2);
                    break;
            }
            finishActionMode();

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

}