package com.example.instaapp.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.instaapp.R;
import com.example.instaapp.models.Post;
import com.example.instaapp.models.Profile;
import com.example.instaapp.models.User;
import com.example.instaapp.ui.activity.CommentsActivity;
import com.example.instaapp.ui.activity.EditBostActivity;
import com.example.instaapp.ui.activity.MainActivity;
import com.example.instaapp.ui.activity.UserProfileActivity;
import com.example.instaapp.ui.view.FeedContextMenu;
import com.example.instaapp.ui.view.FeedContextMenuManager;
import com.example.instaapp.ui.view.SquaredFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackandphantom.circularimageview.CircleImage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.instaapp.Utils.POSTS;
import static com.example.instaapp.Utils.USERS;

/**
 * Created by froger_mcs on 05.11.14.
 */
public class MyFeedAdapter extends RecyclerView.Adapter<MyFeedAdapter.CellFeedViewHolder> {
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    private List<Post> posts;
    int pos;
    private Context context;
     FirebaseFirestore db;;
     Post apost ;
    public MyFeedAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }


    public void deleteItem() {
        notifyItemRemoved(pos);
    }
    @NonNull
    @Override
    public CellFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_loader, parent, false);
        return new CellFeedViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final CellFeedViewHolder holder, final int position) {
         apost = posts.get(position);
        pos=position;
        holder.Post_Likes.setText(String.valueOf(posts.get(position).getLikes().getCounter()));
        holder.Post_description.setText(posts.get(position).getDisc());
        Glide.with(context)
                .load(posts.get(position).getImage())
                .into(holder.Post_image);
        db.collection("Insta Users").document(posts.get(position).getUserId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                holder.User_Name.setText(documentSnapshot.toObject(User.class).getName());
                Glide.with(context).load(documentSnapshot.toObject(User.class).getImage()).into(holder.User_Image);
            }
        });

        for (String id : posts.get(position).getLikes().getLikers()) {
            if (id.equals(Profile.user.getId())) {
                holder.Btn_Like.setTag(R.drawable.ic_heart_red);
                holder.Btn_Like.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_red));
                break;
            }


        }
        holder.tsLikesCounter.setCurrentText(holder.vImageRoot.getResources().getQuantityString(
                R.plurals.likes_count, posts.get(position).getLikes().getCounter(), posts.get(position).getLikes().getCounter()
        ));
        holder.tsCommentsCounter.setCurrentText(holder.vImageRoot.getResources().getQuantityString(
                R.plurals.comments_count, posts.get(position).getComments().size(), posts.get(position).getComments().size()
        ));

        holder.Btn_Comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsActivity.post = posts.get(position);
                CommentsActivity.position=position;
                final Intent intent = new Intent(context, CommentsActivity.class);
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                Activity activity = (Activity) context;
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });
        holder.Btn_Like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer integer = (Integer) holder.Btn_Like.getTag();
                integer = integer == null ? 0 : integer;
                if (R.drawable.ic_heart_red != integer) {
                    holder.Btn_Like.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_red));
                    posts.get(position).getLikes().setLike(Profile.user.getId());
                    DocumentReference noteRef = db.document(POSTS+"/" + posts.get(position).getPostId());
                    noteRef.update("likes", posts.get(position).getLikes());
                    notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showLikedSnackbar();
                    }
                } else {
                    holder.Btn_Like.setTag(R.drawable.ic_heart_outline_grey);
                    holder.Btn_Like.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_outline_grey));
                    posts.get(position).getLikes().deleteLike(Profile.user.getId());
                    DocumentReference noteRef = db.document(POSTS+"/" + posts.get(position).getPostId());
                    noteRef.update("likes", posts.get(position).getLikes());
                    notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showLikedSnackbar();
                    }
                }

            }
        });
        holder.Btn_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Post post = new Post(posts.get(position));
                post.setUserId(Profile.user.getId());
                db.collection(POSTS).add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        post.setPostId(documentReference.getId());
                        db.collection(POSTS).document(documentReference.getId()).set(post);
                        Profile.user.getPosts().add(post.getPostId());
                        db.collection(USERS).document(Profile.user.getId()).set(Profile.user);
                        Toast.makeText(context, "Done share", Toast.LENGTH_LONG).show();
                    }
                });
                Toast.makeText(context, "Done ", Toast.LENGTH_LONG).show();

            }
        });
        holder.User_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Profile.user.getId().equals(posts.get(position).getUserId()))
                    UserProfileActivity.check = true;
                else UserProfileActivity.check = false;
                db.collection(USERS).document(posts.get(position).getUserId())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserProfileActivity.user =new User(documentSnapshot.toObject(User.class));
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        Activity activity = (Activity) context;
                        activity.startActivity(intent);
                    }
                });
            }
        });

        holder.Btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditBostActivity.position=position;
                EditBostActivity.postitem=posts.get(position);
                context.startActivity(new Intent(context,EditBostActivity.class));
            }
        });
        holder.Btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection(POSTS).document( posts.get(position).getPostId()).delete();
                Profile.user.getPosts().remove(posts.get(position).getPostId());
                db.collection(USERS).document(Profile.user.getId()).set(Profile.user);
                UserProfileActivity.posts.setText(String.valueOf(Integer.parseInt(UserProfileActivity.posts.getText().toString())-1));
                deleteItem();
                posts.remove(position);
                Toast.makeText(context, "Post deleted", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class CellFeedViewHolder extends RecyclerView.ViewHolder {
        CircleImage User_Image;
        TextSwitcher tsLikesCounter,tsCommentsCounter;
        ImageButton Btn_More, Btn_Comment, Btn_delete, Btn_edit;
        ImageView Btn_Like;
        ImageView Post_image;
        TextView User_Name, Post_Likes, Post_description,item_post_comments;
        SquaredFrameLayout vImageRoot;

        public CellFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            User_Image = itemView.findViewById(R.id.item_post_user_img);
            User_Name = itemView.findViewById(R.id.item_post_user_name);
            Btn_delete = itemView.findViewById(R.id.item_post_btn_delete);
            Btn_Like = itemView.findViewById(R.id.item_post_btn_like);
            Btn_Comment = itemView.findViewById(R.id.item_post_btn_comment);
            Btn_edit = itemView.findViewById(R.id.item_post_btn_edit);

            tsCommentsCounter = itemView.findViewById(R.id.tsCommentsCounter);
            item_post_comments = itemView.findViewById(R.id.item_post_comments);
            Btn_More = itemView.findViewById(R.id.item_post_btn_more);
            Post_image = itemView.findViewById(R.id.item_post_img);
            Post_description = itemView.findViewById(R.id.item_post_disc);
            Post_Likes = itemView.findViewById(R.id.item_post_likes);
            tsLikesCounter = itemView.findViewById(R.id.tsLikesCounter);
            vImageRoot = itemView.findViewById(R.id.vImageRoot);
        }
    }
}
