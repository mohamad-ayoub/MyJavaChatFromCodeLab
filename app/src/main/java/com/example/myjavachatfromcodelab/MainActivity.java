package com.example.myjavachatfromcodelab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myjavachatfromcodelab.databinding.ActivityMainBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MyChat:" + getClass().getSimpleName();
    final String MESSAGES_CHILD = "messages";
    final String ANONYMOUS = "anonymous";
    final int REQUEST_IMAGE = 2;
    final int REQUEST_IMAGE_FOR_PROFILE = 3;
    final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    DatabaseReference messagesRef;
    private MessageAdapter adapter;
    private LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate and load layout from binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        messagesRef = db.getReference().child(MESSAGES_CHILD);

        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(messagesRef, Message.class).build();
        adapter = new MessageAdapter(options, getUserName());
        binding.progressBar.setVisibility(ProgressBar.INVISIBLE);
        manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        binding.messageRecyclerView.setLayoutManager(manager);
        binding.messageRecyclerView.setAdapter(adapter);

        binding.messageEditText.addTextChangedListener(new SendButtonObserver(binding.sendButton));

        adapter.registerAdapterDataObserver(new ScrollToButtomObserver(binding.messageRecyclerView, adapter, manager));

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = binding.messageEditText.getText().toString();
                Message newMessage = new Message(text, getUserName(), getPhotoUrl(), null);

                messagesRef.push().setValue(newMessage);
                binding.messageEditText.setText("");
            }
        });

        binding.addMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "onCreate: inValid user");
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "onCreate: valid user");

        Toast.makeText(MainActivity.this, "started", Toast.LENGTH_SHORT).show();
        adapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
        //adapter.stopListening();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(MainActivity.this, "resumed", Toast.LENGTH_SHORT).show();
        //adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(MainActivity.this, "Stoped", Toast.LENGTH_SHORT).show();
        //adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                signout();
                return true;
            case R.id.change_profile_image_menu:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_FOR_PROFILE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());
                    FirebaseUser user = auth.getCurrentUser();
                    Message tempMessage = new Message(null, getUserName(), getPhotoUrl(), LOADING_IMAGE_URL);
                    messagesRef.push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.d(TAG, "Unable to write message to database.", (Throwable) databaseError.toException());
                                    } else {
                                        String key = databaseReference.getKey();
                                        FirebaseStorage storageReference = FirebaseStorage.getInstance();
                                        StorageReference userStorageRef = storageReference.getReference(user.getUid()).child(key).child(uri.getLastPathSegment());
                                        putImageInStorage(userStorageRef, uri, key);
                                    }
                                }
                            });
                }
                break;
            case REQUEST_IMAGE_FOR_PROFILE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    FirebaseUser user = auth.getCurrentUser();
                    FirebaseStorage storageReference = FirebaseStorage.getInstance();
                    StorageReference userStorageRef = storageReference.getReference(user.getUid());
                    userStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(@NonNull Uri uri) {
                                    Toast.makeText(MainActivity.this,"uploaded",Toast.LENGTH_SHORT).show();
                                    updateUserProfilePicture(uri);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,"error upload",Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                } else {
                }
                break;
        }
    }

    private void updateUserProfilePicture(final Uri uri) {
        Log.d(TAG, "updateUserProfilePicture: uri=" + uri.toString());
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        auth.getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: Profile image has changed successfully");
                    }
                });
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, String key) {
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(@NonNull Uri uri) {
                        Message message = new Message(null, getUserName(), getPhotoUrl(), uri.toString());
                        messagesRef.child(key).setValue(message);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Image upload task was unsuccessful." + e.getMessage());
                // TODO - Remove the message from realtime database, by key
            }
        });

    }

    private void signout() {
        auth.signOut();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private String getPhotoUrl() {
        Uri url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        return url == null ? null : url.toString();
    }

    private String getUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? ANONYMOUS : user.getDisplayName();
    }

}