package com.app.twishchat.adapater;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.twishchat.R;
import com.app.twishchat.model.ChatModel;
import com.app.twishchat.util.Util;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import me.himanshusoni.chatmessageview.ChatMessageView;

import static com.app.twishchat.util.Helper.converteTimestamp;

public class ChatAdapater extends RecyclerView.Adapter<ChatAdapater.MyViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;
    private static final int CENTER_MSG = 4;
    private static final int RIGHT_MSG_CONTACT = 5;
    private static final int LEFT_MSG_CONTACT = 6;
    private Boolean selected = false;
    private Context context;
    public ArrayList<ChatModel> mList;
    public ArrayList<ChatModel> mSelcetedList;
    private OnItemClickListener mListener;
    private String nameUser;

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        DatabaseReference Rootref;
        FirebaseAuth auth;
        String currentuserID;
        TextView tvTimestamp, tvLocation, tvContactName, tvContactNumber,tvLastSeen;
        EmojiconTextView txtMessage;
        CircleImageView ivUser;
        ImageView ivChatPhoto;
        ChatMessageView chatMessageView;
        RelativeLayout chatContainer;

        MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            Rootref = FirebaseDatabase.getInstance().getReference();
            auth = FirebaseAuth.getInstance();
            currentuserID = auth.getCurrentUser().getUid();

            tvTimestamp = itemView.findViewById(R.id.timestamp);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            tvLastSeen = itemView.findViewById(R.id.lastSeen);
            ivChatPhoto = itemView.findViewById(R.id.img_chat);
            ivUser = itemView.findViewById(R.id.ivUserChat);
            chatMessageView = itemView.findViewById(R.id.contentMessageChat);
            chatContainer = itemView.findViewById(R.id.container);


            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        public void setTxtMessage(String message) {
            if (txtMessage == null) return;
            txtMessage.setText(message);

        }

        public void setIvUser(String urlPhotoUser) {
            if (ivUser == null) return;
            Glide.with(ivUser.getContext()).load(urlPhotoUser).into(ivUser);
        }

        public void setTvTimestamp(String timestamp) {
            if (tvTimestamp == null) return;
            tvTimestamp.setText(converteTimestamp(timestamp, 2));
        }

        public void setIvChatPhoto(String url) {
            if (ivChatPhoto == null) return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .into(ivChatPhoto);
        }

        public void tvIsLocation(int visible) {
            if (tvLocation == null) return;
            tvLocation.setVisibility(visible);
        }
    }

    public ChatAdapater(Context context, ArrayList<ChatModel> mList, ArrayList<ChatModel> mSelcetedList, String nameUser) {
        this.context = context;
        this.mList = mList;
        this.nameUser = nameUser;
        this.mSelcetedList = mSelcetedList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new MyViewHolder(view, mListener);
        } else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MyViewHolder(view, mListener);
        } else if (viewType == RIGHT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img, parent, false);
            return new MyViewHolder(view, mListener);
        } else if (viewType == LEFT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img, parent, false);
            return new MyViewHolder(view, mListener);
        } else if (viewType == RIGHT_MSG_CONTACT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_contact, parent, false);
            return new MyViewHolder(view, mListener);
        } else if (viewType == LEFT_MSG_CONTACT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_contact, parent, false);
            return new MyViewHolder(view, mListener);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_center, parent, false);
            return new MyViewHolder(view, mListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = mList.get(position);
        if (model.getMapModel() != null) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getFile() != null) {
            if (model.getFile().getType().equals("img") && model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getContactsModel() != null) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_CONTACT;
            } else {
                return LEFT_MSG_CONTACT;
            }
        } else if (model.getMessage_type().equals("notification")) {
            return CENTER_MSG;
        } else if (model.getUserModel().getName().equals(nameUser)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {
        final ChatModel currentItem = mList.get(position);

        if (!TextUtils.isEmpty(currentItem.getUserModel().getPhoto_profile())) {

            viewHolder.setIvUser(currentItem.getUserModel().getPhoto_profile());

        }
        viewHolder.setTxtMessage(currentItem.getMessage());
        viewHolder.setTvTimestamp(currentItem.getTimeStamp());
        viewHolder.tvIsLocation(View.GONE);

        if (currentItem.getFile() != null) {

            viewHolder.tvIsLocation(View.GONE);
            viewHolder.setIvChatPhoto(currentItem.getFile().getUrl_file());

        } else if (currentItem.getMapModel() != null) {

            viewHolder.setIvChatPhoto(Util.local(currentItem.getMapModel().getLatitude(), currentItem.getMapModel().getLongitude()));
            viewHolder.tvIsLocation(View.VISIBLE);

        } else if (currentItem.getContactsModel() != null) {

            if (viewHolder.tvContactNumber == null) return;
            if (viewHolder.tvContactName == null) return;
            viewHolder.tvContactName.setText(currentItem.getContactsModel().getName());
            viewHolder.tvContactNumber.setText(currentItem.getContactsModel().getNumber());
            Linkify.addLinks(viewHolder.tvContactNumber, Linkify.PHONE_NUMBERS);
            viewHolder.tvContactNumber.setLinkTextColor(Color.BLACK);

        }if (mSelcetedList.contains(mList.get(position)))
            viewHolder.chatContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_selected_state));
        else
            viewHolder.chatContainer.setBackgroundColor(Color.TRANSPARENT);

        if (currentItem.getSeen().equals("true")){
            if (viewHolder.tvLastSeen == null) return;
            viewHolder.tvLastSeen.setVisibility(View.VISIBLE);
        }else {
            if (viewHolder.tvLastSeen == null) return;
            viewHolder.tvLastSeen.setVisibility(View.GONE);
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

}
