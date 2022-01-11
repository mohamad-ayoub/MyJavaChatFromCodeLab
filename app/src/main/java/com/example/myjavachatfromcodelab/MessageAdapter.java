package com.example.myjavachatfromcodelab;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myjavachatfromcodelab.databinding.ImageMessageBinding;
import com.example.myjavachatfromcodelab.databinding.MessageBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MessageAdapter extends FirebaseRecyclerAdapter {
    private static final int VIEW_TYPE_IMAGE = 2;
    final String TAG = "MyChat:" + getClass().getSimpleName();
    private static final int VIEW_TYPE_TEXT = 1;
    final String ANONYMOUS = "anonymous";

    private FirebaseRecyclerOptions<Message> options;
    private String currentUserName;

    public MessageAdapter(FirebaseRecyclerOptions<Message> options, String currentUserName) {
        super(options);
        this.options = options;
        this.currentUserName = currentUserName;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Object model) {
        if (((Message) this.options.getSnapshots().get(position)).getText() != null) {
            ((MessageAdapter.MessageViewHolder) holder).bind((Message) model);
        } else {
            ((MessageAdapter.ImageMessageViewHolder) holder).bind((Message) model);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;

         if (viewType == VIEW_TYPE_TEXT) {
            View view = inflater.inflate(R.layout.message, parent, false);
            MessageBinding binding = MessageBinding.bind(view);
            viewHolder = new MessageAdapter.MessageViewHolder(binding);
         } else {
            View view = inflater.inflate(R.layout.image_message, parent, false);
            ImageMessageBinding binding = ImageMessageBinding.bind(view);
            viewHolder = new MessageAdapter.ImageMessageViewHolder(binding);
       }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return options.getSnapshots().get(position).getText() != null ? VIEW_TYPE_TEXT : VIEW_TYPE_IMAGE;
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        private MessageBinding binding;

        public MessageViewHolder(MessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message item) {
            binding.messageTextView.setText((CharSequence) item.getText());
            setTextColor(item.getName(), binding.messageTextView);
            binding.messengerTextView.setText(item.getName() == null ? (CharSequence) "anonymous" : (CharSequence) item.getName());
            if (item.getPhotoUrl() != null) {
                loadImageIntoView(binding.messengerImageView, item.getPhotoUrl());
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp);
            }

        }


        private void setTextColor(String userName, TextView textView) {
            if (userName != ANONYMOUS && currentUserName == userName && userName != null) {
                textView.setBackgroundResource(R.drawable.rounded_message_blue);
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray);
                textView.setTextColor(Color.BLACK);
            }
        }
    }

    private class ImageMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageMessageBinding binding;

        public ImageMessageViewHolder(ImageMessageBinding binding) {
            super((View) binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message item) {
            loadImageIntoView(binding.messageImageView, item.getImageUrl());
            binding.messengerTextView.setText(item.getName() == null ? ANONYMOUS : item.getName());
            if (item.getPhotoUrl() != null) {
                loadImageIntoView(binding.messengerImageView, item.getPhotoUrl());
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp);
            }
        }
    }

    private void loadImageIntoView(ImageView view, String url) {
        if (url.startsWith("gs://")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(@NonNull Uri uri) {
                    String downloadUrl = uri.toString();
                    Glide.with(view.getContext())
                            .load(downloadUrl)
                            .into(view);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Getting download url was not successful.", e);
                }
            });
        } else {
            Glide.with(view.getContext()).load(url).into(view);
        }
    }

}
