package io.github.chronosx88.influence.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final static int RIGHT_ITEM = 0;
    private final static int LEFT_ITEM = 1;
    private final static int TECHNICAL_MESSAGE = 2; // TODO

    private final Context context = AppHelper.getContext();
    private ArrayList<MessageEntity> messages = new ArrayList<>();
    private static Comparator<MessageEntity> comparator = ((o1, o2) -> Long.compare(o1.timestamp, o2.timestamp));

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
            Collections.sort(messages, comparator);
        }
    }

    public void addMessages(List<MessageEntity> messages) {
        this.messages.addAll(messages);
        Collections.sort(messages, comparator);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        // Setting message text
        holder.messageText.setText(messages.get(position).text);

        // Setting message time (HOUR:MINUTE)
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        dateFormat.setTimeZone(TimeZone.getDefault());
        String time = dateFormat.format(new Date(messages.get(position).timestamp));
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
