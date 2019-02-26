package com.triplec.triway;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private Button signUp;
    private TextView mail, password, secondPassword;
    private FirebaseAuth mAuth;
    private final int PASSWORD_LENGTH = 8;
    private final String validEmail = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +

            "\\@" +

            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +

            "(" +

            "\\." +

            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +

            ")+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionbar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mail = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        secondPassword = findViewById(R.id.signup_reenter);
        secondPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        || (i == EditorInfo.IME_ACTION_DONE)){
                    signUp.performClick();
                }
                return false;
            }
        });
    }

    public void Submit(View v){
        String email = mail.getText().toString();
        String passW = password.getText().toString();
        String check = secondPassword.getText().toString();
        boolean isValidPassword = validPassword(passW);

        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            if (!isValidPassword){
                Toast.makeText(getApplicationContext(), "Password length should be " +
                        "longer than 8", Toast.LENGTH_LONG).show();
            }
            else if(!passW.equals(check)){
                Toast.makeText(getApplicationContext(), "Password doesn't match" ,
                        Toast.LENGTH_LONG).show();
            }
            else{
                createAccount(email, passW);
                openLoginPage();
            }

        }
        else {
            Toast.makeText(getApplicationContext(),"Enter Valid Email",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void openLoginPage(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public boolean validPassword(String password){
        return password.length() >= PASSWORD_LENGTH;
    }


    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    private boolean validateForm(){
        mail = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        secondPassword = findViewById(R.id.signup_reenter);
        String email = mail.getText().toString();
        String passW = password.getText().toString();
        String check = secondPassword.getText().toString();
        boolean isValidPassword = validPassword(passW);

        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            if(isValidPassword && (passW.equals(check))){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

}

