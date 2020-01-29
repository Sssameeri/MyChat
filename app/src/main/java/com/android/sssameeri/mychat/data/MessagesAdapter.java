package com.android.sssameeri.mychat.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sssameeri.mychat.R;
import com.android.sssameeri.mychat.models.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private FirebaseAuth auth;

    @Override
    public long getItemId(int position) {
        return position;
    }

    public MessagesAdapter(Context context, List<Message> messageList, FirebaseAuth auth) {
        this.context = context;
        this.messageList = messageList;
        this.auth = auth;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case 0:
                return new MyMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.my_message_item, parent, false));
            case 1:
                return new YourMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.your_message_item, parent, false));
        default:
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                MyMessageViewHolder myMessageViewHolder = (MyMessageViewHolder) holder;
                Message myMessage = messageList.get(position);

                boolean isText = myMessage.getImageUrl() == null;

                if(isText) {
                    myMessageViewHolder.messageTxtView.setVisibility(View.VISIBLE);
                    myMessageViewHolder.photoImgView.setVisibility(View.GONE);
                    myMessageViewHolder.messageTxtView.setText(myMessage.getText());
                } else {
                    myMessageViewHolder.messageTxtView.setVisibility(View.GONE);
                    myMessageViewHolder.photoImgView.setVisibility(View.VISIBLE);
                    Glide.with(myMessageViewHolder.photoImgView.getContext()).load(myMessage.getImageUrl()).into(myMessageViewHolder.photoImgView);
                }

                myMessageViewHolder.nameTxtView.setText(myMessage.getName());
                break;
            case 1:
                YourMessageViewHolder yourMessageViewHolder = (YourMessageViewHolder) holder;
                Message yourMessage = messageList.get(position);

                boolean isYourText = yourMessage.getImageUrl() == null;

                if(isYourText) {
                    yourMessageViewHolder.messageTxtView.setVisibility(View.VISIBLE);
                    yourMessageViewHolder.photoImgView.setVisibility(View.GONE);
                    yourMessageViewHolder.messageTxtView.setText(yourMessage.getText());
                } else {
                    yourMessageViewHolder.messageTxtView.setVisibility(View.GONE);
                    yourMessageViewHolder.photoImgView.setVisibility(View.VISIBLE);
                    Glide.with(yourMessageViewHolder.photoImgView.getContext()).load(yourMessage.getImageUrl()).into(yourMessageViewHolder.photoImgView);
                }

                yourMessageViewHolder.nameTxtView.setText(yourMessage.getName());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        if(message.getSender().equals(auth.getCurrentUser().getUid())) {
            return 0;
        }
        return 1;
    }

    public void addItem(Message message, int index) {
        messageList.add(message);
        notifyItemInserted(index);
    }

    public void clearData() {
        messageList.clear();
        notifyDataSetChanged();
    }

    class MyMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTxtView, nameTxtView;
        private ImageView photoImgView;

        MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTxtView = itemView.findViewById(R.id.myTextTxtView);
            nameTxtView = itemView.findViewById(R.id.myNameTxtView);
            photoImgView = itemView.findViewById(R.id.userImageImgView);
        }
    }

    class YourMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTxtView, nameTxtView;
        private ImageView photoImgView;

         YourMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTxtView = itemView.findViewById(R.id.textTxtView);
            nameTxtView = itemView.findViewById(R.id.nameTxtView);
            photoImgView = itemView.findViewById(R.id.userImageImgView);
        }
    }
}
