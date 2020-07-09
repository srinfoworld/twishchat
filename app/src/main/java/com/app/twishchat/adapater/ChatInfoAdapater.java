package com.app.twishchat.adapater;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.twishchat.R;
import com.app.twishchat.model.ChatInfoModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatInfoAdapater extends RecyclerView.Adapter<ChatInfoAdapater.MyViewHolder> {
    private Context context;
    public ArrayList<ChatInfoModel> mList;
    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public DatabaseReference Rootref;
        public FirebaseAuth auth;
        public String currentuserID;
        public TextView username;
        public CircleImageView profileImage;
        public RelativeLayout container;
        TextView displayMessage,admin;

        MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            Rootref = FirebaseDatabase.getInstance().getReference();
            auth = FirebaseAuth.getInstance();
            currentuserID = auth.getCurrentUser().getUid();

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            container = itemView.findViewById(R.id.container);
            displayMessage = itemView.findViewById(R.id.displayMessage);
            admin = itemView.findViewById(R.id.time);
            displayMessage.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public ChatInfoAdapater(Context context, ArrayList<ChatInfoModel> mList) {
        this.context = context;
        this.mList = mList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_adapater, parent, false);
        MyViewHolder evh = new MyViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ChatInfoModel currentItem = mList.get(position);
        holder.displayMessage.setText(currentItem.getAbout());
        if (currentItem.isAdmin()){
            holder.admin.setVisibility(View.VISIBLE);
            holder.admin.setText("ADMIN");
            holder.admin.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }

        if (currentItem.getUid().equals(holder.currentuserID)){
            holder.username.setText("You");
        }else{
            holder.username.setText(currentItem.getName());
        }
        if (!currentItem.isBlocked()){
            if (!TextUtils.isEmpty(currentItem.getProfile_pic())) {
                Glide.with(context)
                        .load(currentItem.getProfile_pic())
                        .into(holder.profileImage);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
