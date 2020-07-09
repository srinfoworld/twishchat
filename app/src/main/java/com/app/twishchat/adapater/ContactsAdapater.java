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
import com.app.twishchat.model.ContactsModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapater extends RecyclerView.Adapter<ContactsAdapater.MyViewHolder> {
    private Context context;
    private ArrayList<ContactsModel> mList;
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
        TextView displayMessage,PhoneOrFriend;

        MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            Rootref = FirebaseDatabase.getInstance().getReference();
            auth = FirebaseAuth.getInstance();
            currentuserID = auth.getCurrentUser().getUid();

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            container = itemView.findViewById(R.id.container);
            displayMessage = itemView.findViewById(R.id.displayMessage);
            PhoneOrFriend = itemView.findViewById(R.id.time);
            displayMessage.setVisibility(View.VISIBLE);

            PhoneOrFriend.setPadding(10,2,10,2);
            PhoneOrFriend.setTextSize(10);
            PhoneOrFriend.setTextColor(itemView.getResources().getColor(R.color.colorPrimary));
        }
    }

    public ContactsAdapater(Context context, ArrayList<ContactsModel> mList) {
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
        final ContactsModel currentItem = mList.get(position);
        holder.username.setText(currentItem.getName());
        holder.displayMessage.setText(currentItem.getAbout());

        if (currentItem.isInPhoneList()){
            holder.PhoneOrFriend.setVisibility(View.VISIBLE);
            holder.PhoneOrFriend.setText("PHONE");
        }

        if (!TextUtils.isEmpty(currentItem.getProfile_pic())){
            Glide.with(context)
                    .load(currentItem.getProfile_pic())
                    .into(holder.profileImage);
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
