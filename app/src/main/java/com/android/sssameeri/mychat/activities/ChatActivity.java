package com.android.sssameeri.mychat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.sssameeri.mychat.R;
import com.android.sssameeri.mychat.data.MessagesAdapter;
import com.android.sssameeri.mychat.models.Message;
import com.android.sssameeri.mychat.models.User;
import com.android.sssameeri.mychat.utils.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity{

    private ProgressBar progressBar;
    private RelativeLayout layout;
    private RecyclerView messageRecyclerView;
    private MessagesAdapter adapter;
    private ImageButton sendImgBtn;
    private Button sendMessageBtn;
    private EditText getMessageEditTxt;
    private String userName;
    private DatabaseReference messageReference; //messages child tree in database
    private ChildEventListener messagesChildEventListener;
    private DatabaseReference userReference;
    private ChildEventListener usersChildEventListener;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String recipientUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        mAuth = FirebaseAuth.getInstance();
        Intent getUserIdIntent = getIntent();
        if(getUserIdIntent != null) {
            recipientUserId = getUserIdIntent.getStringExtra("recipient_user_id");
            userName = getUserIdIntent.getStringExtra("user_name");
        }
        //binding components
        userName = "default_username";
        progressBar = findViewById(R.id.progressBar);
        sendImgBtn = findViewById(R.id.sendImgBtn);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        getMessageEditTxt = findViewById(R.id.messageEditTxt);
        layout = findViewById(R.id.constraintLayout);
        //binding listView
        List<Message> messagesList = new ArrayList<>();
        messageRecyclerView = findViewById(R.id.messageListView);
        adapter = new MessagesAdapter(this, messagesList, mAuth);
        messageRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.smoothScrollToPosition(adapter.getItemCount());
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        //database instance and references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messageReference = database.getReference().child("messages");
        userReference = database.getReference().child("users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("chat_img");
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMessageEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //edit text is empty then disable send button
                if(s.toString().trim().length() > 0)
                    sendMessageBtn.setEnabled(true);
                else
                    sendMessageBtn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        //set max text capacity to 500 symbols
        getMessageEditTxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        //send message button
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create Message object and set text and user information
                Message currentMessage = new Message();
                currentMessage.setText(getMessageEditTxt.getText().toString());
                currentMessage.setName(userName);
                currentMessage.setImageUrl(null);
                currentMessage.setSender(mAuth.getCurrentUser().getUid());
                currentMessage.setRecipient(recipientUserId);
                //send message into database
                messageReference.push().setValue(currentMessage);
                getMessageEditTxt.setText("");
                messageRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });
        //send image button
        sendImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoIntent.setType("image/*");
                photoIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(photoIntent, "Choose an image"),
                        Utils.RESULT_CODE_FOR_INTENT);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messageRecyclerView.smoothScrollToPosition(adapter.getItemCount());
                    }
                }, 3000);
            }
        });

        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message snapshotMessage =
                        dataSnapshot.getValue(Message.class);
                //messageAdapter.add(snapshotMessage);

                if(snapshotMessage.getSender().equals(mAuth.getCurrentUser().getUid())
                && snapshotMessage.getRecipient().equals(recipientUserId) || snapshotMessage.getRecipient().equals(mAuth.getCurrentUser().getUid())
                        && snapshotMessage.getSender().equals(recipientUserId)) {
                    adapter.addItem(snapshotMessage, adapter.getItemCount());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user =
                        dataSnapshot.getValue(User.class);
                if(user.getId().equals(mAuth.getCurrentUser().getUid())) {
                    userName = user.getName();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        messageReference.addChildEventListener(messagesChildEventListener);
        userReference.addChildEventListener(usersChildEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageReference.removeEventListener(messagesChildEventListener);
        userReference.removeEventListener(usersChildEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.clearData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOutItem) {
            FirebaseAuth.getInstance().signOut();
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void signOut() {
        Intent startSignInActivity = new Intent(ChatActivity.this, SignInActivity.class);
        startActivity(startSignInActivity);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Utils.RESULT_CODE_FOR_INTENT && resultCode == RESULT_OK && data != null) {
            Uri selectedImgUri = data.getData();
            final StorageReference imgReference = storageReference.child(selectedImgUri.getLastPathSegment());
            UploadTask uploadTask = imgReference.putFile(selectedImgUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imgReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Message message = new Message();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(mAuth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        messageReference.push().setValue(message);
                    } else {
                        Snackbar.make(layout, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
