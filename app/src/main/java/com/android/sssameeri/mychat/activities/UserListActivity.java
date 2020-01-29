package com.android.sssameeri.mychat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.sssameeri.mychat.R;
import com.android.sssameeri.mychat.data.UsersAdapter;
import com.android.sssameeri.mychat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private DatabaseReference usersReference;
    private ChildEventListener usersChildEventListener;
    private UsersAdapter adapter;
    private FirebaseAuth mAuth;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mAuth = FirebaseAuth.getInstance();

        userList = new ArrayList<>();
        RecyclerView userRecyclerView = findViewById(R.id.usersRecyclerView);
        adapter = new UsersAdapter(this, userList);

        userRecyclerView.setAdapter(adapter);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnUserClickListener(new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChar(position);
            }
        });
    }

    private void goToChar(int position) {
        Intent startMainActivity = new Intent(UserListActivity.this, ChatActivity.class);
        startMainActivity.putExtra("recipient_user_id", userList.get(position).getId());
        startMainActivity.putExtra("user_name", userList.get(position).getName());
        startActivity(startMainActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();

        usersReference = FirebaseDatabase.getInstance().getReference().child("users");
        if(usersChildEventListener == null) {
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    User user =
                            dataSnapshot.getValue(User.class);
                    if(!user.getId().equals(mAuth.getCurrentUser().getUid())) {
                        user.setPhotoMockUp(R.drawable.photo);
                        adapter.addItem(user, adapter.getItemCount());
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
        }

        usersReference.addChildEventListener(usersChildEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.clearData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        usersReference.removeEventListener(usersChildEventListener);
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
        Intent startSignInActivity = new Intent(UserListActivity.this, SignInActivity.class);
        startActivity(startSignInActivity);
        finish();
    }
}
