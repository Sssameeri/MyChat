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
import com.android.sssameeri.mychat.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserClickListener listener;

    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public interface OnUserClickListener {
        void onUserClick(int position);
    }

    public void addItem(User user, int index) {
        userList.add(user);
        notifyItemInserted(index);
    }

    public void clearData() {
        userList.clear();
        notifyDataSetChanged();
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        UserViewHolder viewHolder = new UserViewHolder(itemView, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = userList.get(position);
        holder.photoImageView.setImageResource(currentUser.getPhotoMockUp());
        holder.userNameTxtView.setText(currentUser.getName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ImageView photoImageView;
        TextView userNameTxtView;

        UserViewHolder(View itemView, final OnUserClickListener listener) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.userPhotoImgView);
            userNameTxtView = itemView.findViewById(R.id.userNameTxtView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onUserClick(position);
                        }
                    }
                }
            });
        }
    }
}
