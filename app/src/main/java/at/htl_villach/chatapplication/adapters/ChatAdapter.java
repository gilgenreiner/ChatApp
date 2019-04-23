package at.htl_villach.chatapplication.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import at.htl_villach.chatapplication.R;
import at.htl_villach.chatapplication.bll.Message;

/**
 * Created by pupil on 4/23/19.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mContext;
    private List<Message> mMessages;

    public ChatAdapter(Context mContext, List<Message> mMessages) {
        this.mMessages = mMessages;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_message, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Message message = mMessages.get(position);

        holder.messageBody.setText(message.getMessage());
        holder.sendFrom.setText(message.getSender());
        holder.timestamp.setText(message.getTimestamp());

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
}