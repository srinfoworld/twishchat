package com.app.twishchat.adapater;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.twishchat.R;
import com.app.twishchat.model.CallModel;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.util.Helper;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallAdapater extends RecyclerView.Adapter<CallAdapater.MyViewHolder> {
    private Context context;
    private ArrayList<CallModel> mList;
    private OnItemClickListener mListener;
    private String currentuserID;


    public interface OnItemClickListener {
        void onAddClick(int position);
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
        TextView displayMessage;
        public Button endBtn;

        MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            Rootref = FirebaseDatabase.getInstance().getReference();
            auth = FirebaseAuth.getInstance();
            currentuserID = auth.getCurrentUser().getUid();

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            container = itemView.findViewById(R.id.container);
            displayMessage = itemView.findViewById(R.id.displayMessage);
            endBtn = itemView.findViewById(R.id.addBtn);
            displayMessage.setVisibility(View.VISIBLE);

            endBtn.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAddClick(position);
                    }
                }
            });
        }
    }

    public CallAdapater(Context context, ArrayList<CallModel> mList) {
        this.context = context;
        this.mList = mList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_add_call_adapter, parent, false);
        MyViewHolder evh = new MyViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final CallModel currentItem = mList.get(position);

        if (currentItem.getCallModel().getPick().equals("true")){
            holder.endBtn.setVisibility(View.GONE);
        }else {
            holder.endBtn.setVisibility(View.VISIBLE);
        }

        holder.username.setText(currentItem.getName());
        holder.endBtn.setText("End");
        holder.endBtn.setBackground(context.getResources().getDrawable(R.drawable.red_rounded_bg_for_btn));
        holder.displayMessage.setText(currentItem.getCallModel().getStatus());

        if (!TextUtils.isEmpty(currentItem.getProfile_pic())) {
            Glide.with(context)
                    .load(currentItem.getProfile_pic())
                    .into(holder.profileImage);
        }else {
            holder.profileImage.setImageResource(R.drawable.person);
        }

        if (currentItem.getCallModel().getStatus().equals("Call Denied")){
            Helper.endVideoCall(currentuserID, currentItem.getUid(),false);
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
