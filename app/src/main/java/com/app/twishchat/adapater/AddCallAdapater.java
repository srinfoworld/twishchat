package com.app.twishchat.adapater;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.twishchat.R;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.model.FindFriendModel;
import com.app.twishchat.model.MainModel;
import com.app.twishchat.util.Helper;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddCallAdapater extends RecyclerView.Adapter<AddCallAdapater.MyViewHolder> implements Filterable {
    private Context context;
    private ArrayList<ContactsModel> mList;
    private ArrayList<ContactsModel> msearchList;
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
        public Button addBtn;

        MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            Rootref = FirebaseDatabase.getInstance().getReference();
            auth = FirebaseAuth.getInstance();
            currentuserID = auth.getCurrentUser().getUid();

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            container = itemView.findViewById(R.id.container);
            displayMessage = itemView.findViewById(R.id.displayMessage);
            addBtn = itemView.findViewById(R.id.addBtn);
            displayMessage.setVisibility(View.VISIBLE);

            addBtn.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAddClick(position);
                    }
                }
            });
        }
    }

    public AddCallAdapater(Context context, ArrayList<ContactsModel> mList) {
        this.context = context;
        this.mList = mList;
        msearchList = new ArrayList<>(mList);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_add_call_adapter, parent, false);
        MyViewHolder evh = new MyViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ContactsModel currentItem = mList.get(position);

        holder.username.setText(currentItem.getName());
        if (currentItem.getOnline().equals("true")){
            holder.displayMessage.setText("Online");
            holder.displayMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.online,0,0,0);
            holder.displayMessage.setCompoundDrawablePadding(10);
        }else if (currentItem.getOnline().equals("false")){
            holder.displayMessage.setText(Helper.converteTimestamp(currentItem.getTimeStamp(),3));
        }

        if (!TextUtils.isEmpty(currentItem.getProfile_pic())) {
            Glide.with(context)
                    .load(currentItem.getProfile_pic())
                    .into(holder.profileImage);
        }else {
            holder.profileImage.setImageResource(R.drawable.person);
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

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ContactsModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(msearchList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ContactsModel item : msearchList) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
