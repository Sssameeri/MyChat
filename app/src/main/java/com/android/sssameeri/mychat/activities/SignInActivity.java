package com.android.sssameeri.mychat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.sssameeri.mychat.R;
import com.android.sssameeri.mychat.models.User;
import com.android.sssameeri.mychat.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private DatabaseReference userReference;
    private EditText emailEditTxt, passwordEditTxt, nameEditTxt, confirmPasswordEditTxt;
    private TextView toggleLoginTxtView;
    private Button createUserBtn;
    private ConstraintLayout layout;
    private String email, password, confirmPassword, name;
    private boolean isUserHaveAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
            updateUI();

        database = FirebaseDatabase.getInstance();
        userReference = database.getReference().child("users");

        layout = findViewById(R.id.layout);
        emailEditTxt = findViewById(R.id.emailEditTxt);
        passwordEditTxt = findViewById(R.id.passEditTxt);
        confirmPasswordEditTxt = findViewById(R.id.confirmPassEditTxt);
        nameEditTxt = findViewById(R.id.nameEditTxt);
        createUserBtn = findViewById(R.id.authBtn);
        toggleLoginTxtView = findViewById(R.id.toggleLoginTxtView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        createUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEditTxt.getText().toString().trim();
                password = passwordEditTxt.getText().toString().trim();
                confirmPassword = confirmPasswordEditTxt.getText().toString().trim();
                name = nameEditTxt.getText().toString().trim();
                /*
                * if userHaveAccount equals true then signIn with user data
                * else execute createUser method
                * */
                if(isUserHaveAccount) {
                    if (validateEmailAddress(email)) {
                        signInUser(email, password);
                    }
                } else {
                    if (!validateEmailAddress(email)) {
                        Snackbar.make(layout, R.string.invalid_email, Snackbar.LENGTH_SHORT).show();
                    } else if(!isPasswordEquals(password, confirmPassword)){
                        Snackbar.make(layout, R.string.passwords_equals, Snackbar.LENGTH_SHORT).show();
                    } else {
                        createUser(email, password);
                    }
                }
            }
        });

        toggleLoginTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUserHaveAccount) {
                    createUserBtn.setText(R.string.login);
                    toggleLoginTxtView.setText(R.string.tap_to_sign_up);
                    nameEditTxt.setVisibility(View.GONE);
                    confirmPasswordEditTxt.setVisibility(View.GONE);
                    isUserHaveAccount = true;
                } else {
                    createUserBtn.setText(R.string.sign_up);
                    toggleLoginTxtView.setText(R.string.tap_to_login);
                    confirmPasswordEditTxt.setVisibility(View.VISIBLE);
                    nameEditTxt.setVisibility(View.VISIBLE);
                    isUserHaveAccount = false;
                }
            }
        });
    }
    //create user with email and password
    private void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            createUser(user);
                            updateUI();
                        } else {
                            Snackbar.make(layout, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //login to chat with email and password
    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            Snackbar.make(layout, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //save user in database
    private void createUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setEmail(firebaseUser.getEmail());
        user.setId(firebaseUser.getUid());
        user.setName(name);
        userReference.push().setValue(user);
    }
    //return true if emailEditTxt match with email template
    private boolean validateEmailAddress(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    //start ChatActivity when login or sign up is successful
    private void updateUI() {
        Intent startMainActivity = new Intent(SignInActivity.this, UserListActivity.class);
        startActivity(startMainActivity);
        finish();
    }
    //return true if password equals to confirmPassword
    private boolean isPasswordEquals(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}
