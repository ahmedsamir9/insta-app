package com.example.instaapp.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instaapp.R;
import com.example.instaapp.genericFirebase;
import com.example.instaapp.models.Profile;
import com.example.instaapp.models.User;
import com.example.instaapp.ui.activity.UserProfileActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackandphantom.circularimageview.CircleImage;

import java.util.List;

import static com.example.instaapp.Utils.USERS;

public class following_adapter extends RecyclerView.Adapter<following_adapter.view_holder> {
    Context context;
    ProgressDialog dialog;

    public following_adapter(Context context, List<String> users) {
        this.context = context;
        this.users = users;
    }

    List<String> users;
    String myid;

    public following_adapter(Context context, List<String> users, String myid) {
        this.context = context;
        this.users = users;
        this.myid = myid;
    }

    @NonNull
    @Override
    public view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item, parent, false);
        return new view_holder(view);
    }

    boolean checkFollowing(User following, User follower, final Context context) {
        for (String name : follower.getFollowing())
            if (name.equals(following.getId()))
                return true;
        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull final view_holder holder, final int position) {
        final String userid = users.get(position);
        if (userid != null) {
            System.out.println(userid);
            dialog = new ProgressDialog(context);
            dialog.show();
            dialog.setContentView(R.layout.item_photo_filter);
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            if (!userid.equals(Profile.user.getId()))
                holder.status.setVisibility(View.VISIBLE);
            else holder.status.setVisibility(View.GONE);
            final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(USERS).document(userid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                            final User user = documentSnapshot.toObject(User.class);
                            Glide.with(context).load(user.getImage()).apply(RequestOptions.skipMemoryCacheOf(true)).into(holder.image);
                            holder.name.setText(user.getName());
                            if (checkFollowing(user, Profile.user, context)) {
                                holder.status.setText("following");
                            } else
                                holder.status.setText("follow");
                            holder.tvuserabout.setText(user.getAbout());
                            dialog.dismiss();
                            if (UserProfileActivity.check == false) {

                                holder.status.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (holder.status.getText().equals("following")) {
                                            genericFirebase.unfollowing_relation(user, Profile.user, context);
                                            holder.status.setText("follow");
                                            notifyDataSetChanged();
                                        } else {
                                            genericFirebase.following_relation(user, Profile.user, context);
                                            holder.status.setText("following");
                                            notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                            holder.image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    UserProfileActivity.user=user;

                                    onclickitem.onclick();
                                }
                            });

                        }


                    });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    onclick onclickitem;

    public void setOnclickitem(onclick onclickitem) {
        this.onclickitem = onclickitem;
    }

    public interface onclick {
        public void onclick();
    }

    class view_holder extends RecyclerView.ViewHolder {
        CircleImage image;
        TextView name, tvuserabout;
        Button status;

        public view_holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivUserAvatar2);
            name = itemView.findViewById(R.id.tvuserName);
            tvuserabout = itemView.findViewById(R.id.tvuserabout);
            status = itemView.findViewById(R.id.follow_status);

        }
    }
}
