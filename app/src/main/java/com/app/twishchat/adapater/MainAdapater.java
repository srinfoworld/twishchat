package com.app.twishchat.adapater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.twishchat.R;
import com.app.twishchat.model.MainModel;
import com.app.twishchat.util.Helper;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.twishchat.util.Helper.converteTimestamp;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getMessageSeen;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;


public class MainAdapater extends RecyclerView.Adapter<MainAdapater.MyViewHolder> {

    private Context context;
    public ArrayList<MainModel> mList;
    public ArrayList<MainModel> mSelcetedList;
    String currentuserName;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        public FirebaseAuth auth;
        public TextView username, displayMessage, time, messageCount;
        public CircleImageView profileImage;
        public RelativeLayout container, message_container;
        ImageView msgSeen;

        MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            auth = FirebaseAuth.getInstance();
            currentuserID = auth.getCurrentUser().getUid();

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            container = itemView.findViewById(R.id.container);
            message_container = itemView.findViewById(R.id.message_container);
            messageCount = itemView.findViewById(R.id.message_count);
            time = itemView.findViewById(R.id.time);
            msgSeen = itemView.findViewById(R.id.msgSeen);
            displayMessage = itemView.findViewById(R.id.displayMessage);

            displayMessage.setVisibility(View.VISIBLE);
            time.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }

    public MainAdapater(Context context, ArrayList<MainModel> mList, ArrayList<MainModel> mSelcetedList, String currentuserName) {
        this.context = context;
        this.mList = mList;
        this.mSelcetedList = mSelcetedList;
        this.currentuserName = currentuserName;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_adapater, parent, false);
        MyViewHolder evh = new MyViewHolder(v, mListener);
        return evh;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final MainModel currentItem = mList.get(position);

        if (currentItem.getType().equals("group")) {

            if (currentItem.getSenderName().equals(currentuserName)) {
                switch (currentItem.getAttachment()) {
                    case "Img":
                        holder.displayMessage.setText("  You");
                        holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_image_24, 0, 0, 0);
                        break;
                    case "Map":
                        holder.displayMessage.setText("  You");
                        holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
                        break;
                    case "Contact":
                        holder.displayMessage.setText("  You");
                        holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_call_24, 0, 0, 0);
                        break;
                    default:
                        holder.displayMessage.setText("You: " + currentItem.getDisplayMessage());
                        break;
                }
            } else if (currentItem.getSenderName().equals("")) {
                holder.displayMessage.setText(currentItem.getDisplayMessage());
            } else {
                switch (currentItem.getAttachment()) {
                    case "Img":
                        holder.displayMessage.setText("  " + currentItem.getSenderName());
                        holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_image_24, 0, 0, 0);
                        break;
                    case "Map":
                        holder.displayMessage.setText("  " + currentItem.getSenderName());
                        holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
                        break;
                    case "Contact":
                        holder.displayMessage.setText("  " + currentItem.getSenderName());
                        holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_call_24, 0, 0, 0);
                        break;
                    default:
                        holder.displayMessage.setText(currentItem.getSenderName() + ": " + currentItem.getDisplayMessage());
                        break;
                }
            }
        } else {
            switch (currentItem.getAttachment()) {
                case "Img":
                    holder.displayMessage.setText("  Image");
                    holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_image_24, 0, 0, 0);
                    break;
                case "Map":
                    holder.displayMessage.setText("  Location");
                    holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
                    break;
                case "Contact":
                    holder.displayMessage.setText("  Contact");
                    holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_call_24, 0, 0, 0);
                    break;
                default:
                    holder.displayMessage.setText(currentItem.getDisplayMessage());
                    break;
            }

            holder.msgSeen.setVisibility(View.VISIBLE);

            if (getMessageSeen(currentuserID, currentItem.getId())) {
                holder.msgSeen.setBackgroundResource(R.drawable.ic_baseline_done_all_color_primary_24);
            } else {
                if (currentItem.getSenderName().equals(getUsersName(currentItem.getId()))) {
                    holder.msgSeen.setVisibility(View.GONE);
                } else {
                    holder.msgSeen.setBackgroundResource(R.drawable.ic_baseline_done_24);
                }
            }
        }


        if (!currentItem.getMessageCount().equals("0") && !TextUtils.isEmpty(currentItem.getMessageCount())) {
            holder.messageCount.setVisibility(View.VISIBLE);
            holder.message_container.setVisibility(View.VISIBLE);
            holder.messageCount.setText(currentItem.getMessageCount());
        } else {
            holder.messageCount.setVisibility(View.GONE);
            holder.message_container.setVisibility(View.GONE);
        }


        if (currentItem.getType().equals("1to1")) {
            holder.username.setText(getUsersName(currentItem.getId()));
        } else {
            holder.username.setText(currentItem.getName());
        }

        holder.time.setText(converteTimestamp(currentItem.getTimeStamp(), 1));

        if (!currentItem.getBlocked().equals("true")) {
            if (!TextUtils.isEmpty(currentItem.getProfile_pic())) {
                Glide.with(context)
                        .load(currentItem.getProfile_pic())
                        .into(holder.profileImage);
            } else {
                if (currentItem.getType().equals("group")) {
                    holder.profileImage.setImageResource(R.drawable.group);
                }
            }

            if (currentItem.getType().equals("1to1")) {
                if (!TextUtils.isEmpty(getUsersProfilePic(currentItem.getId()))) {
                    Glide.with(context)
                            .load(getUsersProfilePic(currentItem.getId()))
                            .into(holder.profileImage);
                }
            }
        }

        if (mSelcetedList.contains(mList.get(position)))
            holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.selection));
        else
            holder.container.setBackgroundColor(Color.TRANSPARENT);
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
