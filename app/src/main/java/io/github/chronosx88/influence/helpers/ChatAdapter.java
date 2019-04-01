package io.github.chronosx88.influence.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final static int RIGHT_ITEM = 0;
    private final static int LEFT_ITEM = 1;
    private final static int TECHNICAL_MESSAGE = 2; // TODO

    private final static Context context = AppHelper.getContext();
    private ArrayList<MessageEntity> messages = new ArrayList<>();

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == RIGHT_ITEM) {
            return new ChatAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.message_right_item, parent, false));
        } else {
            return new ChatAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.message_left_item, parent, false));
        }
    }

    public void addMessage(MessageEntity message) {
        if(message != null) {
            for (MessageEntity messageEntity : messages) {
                if(messageEntity.messageID.equals(message.messageID)) {
                    return;
                }
            }
            messages.add(message);
        }
    }

    public void addMessages(List<MessageEntity> messages) {
        this.messages.addAll(messages);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        // Setting message text
        holder.messageText.setText(messages.get(position).text);

        // Setting message time (HOUR:MINUTE)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(messages.get(position).timestamp));
        String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE);
        holder.messageTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).senderID.equals(AppHelper.getPeerID())) {
            return RIGHT_ITEM;
        } else {
            return LEFT_ITEM;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        CircleImageView profileImage;
        TextView messageTime;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.profile_image);
            messageTime = itemView.findViewById(R.id.message_time);
        }
    }
}
