package com.app.twishchat.adapater;

import android.content.Context;
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
import com.app.twishchat.model.FindFriendModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsAdapater extends RecyclerView.Adapter<FindFriendsAdapater.MyViewHolder> implements Filterable {
    private Context context;
    private ArrayList<FindFriendModel> mList;
    private ArrayList<FindFriendModel> msearchList;
    private OnItemClickListener mListener;
    private String currentuserID;


    public interface OnItemClickListener {
        void onAddClick(int position);

        void onCancelClick(int position);
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
        public ImageButton addBtn;
        public Button cancelBtn;

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
            cancelBtn = itemView.findViewById(R.id.cancelBtn);
            displayMessage.setVisibility(View.VISIBLE);

            addBtn.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAddClick(position);
                    }
                }
            });

            cancelBtn.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCancelClick(position);
                    }
                }
            });
        }
    }

    public FindFriendsAdapater(Context context, ArrayList<FindFriendModel> mList, String currentuserID) {
        this.context = context;
        this.mList = mList;
        this.currentuserID = currentuserID;
        msearchList = new ArrayList<>(mList);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_find_friends_adapater, parent, false);
        MyViewHolder evh = new MyViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final FindFriendModel currentItem = mList.get(position);

        holder.username.setText(currentItem.getMainModel().getName());
        holder.displayMessage.setText(currentItem.getMainModel().getAbout());

        if (!TextUtils.isEmpty(currentItem.getMainModel().getProfile_pic())) {
            Glide.with(context)
                    .load(currentItem.getMainModel().getProfile_pic())
                    .into(holder.profileImage);
        }else {
            holder.profileImage.setImageResource(R.drawable.person);
        }

        if (currentItem.getRequestSent()){
            holder.cancelBtn.setVisibility(View.VISIBLE);
            holder.addBtn.setVisibility(View.GONE);
        }else {
            holder.addBtn.setVisibility(View.VISIBLE);
            holder.cancelBtn.setVisibility(View.GONE);
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
            List<FindFriendModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(msearchList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (FindFriendModel item : msearchList) {
                    if (item.getMainModel().getName().toLowerCase().contains(filterPattern)) {
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
