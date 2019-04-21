package com.triplec.triway;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity implements SessionTimeoutListener{

    private static final String TAG = "EmailPassword";
    private TextInputEditText first_name, last_name;
    private Button signUp;
    private TextInputEditText mail, password, secondPassword;
    private TextInputLayout mail_layout, password_layout, secondPassword_layout;
    private FirebaseAuth mAuth;
    private Timer timer;
    private SessionTimeoutListener timeoutListener;
    private boolean signUpClicked;
    SharedPreferences sp;
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
        Toolbar toolbar = findViewById(R.id.toolbar_sign_up);
        sp = getSharedPreferences("time_out", MODE_PRIVATE);

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sp = getSharedPreferences("time_out", MODE_PRIVATE);
        // print a timeout message if last time activity got expired
        if(sp.getBoolean("isTimeout", false)){
            Log.d(TAG, "timeout");
            Toast.makeText(this,"Signup session timeout!", Toast.LENGTH_LONG).show();
            sp.edit().putBoolean("isTimeout", false).apply();
        }

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionbar.setDisplayHomeAsUpEnabled(true);
        // find each view on this activity
        signUp = findViewById(R.id.signUpButton);
        mAuth = FirebaseAuth.getInstance();
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        mail = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        secondPassword = findViewById(R.id.signup_reenter);
        // make done the default button when user has input secondPassword, also hide the soft keyboard
        secondPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        || (i == EditorInfo.IME_ACTION_DONE)){
                    InputMethodManager imm = (InputMethodManager) textView.getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    submit(null);
                    return true;
                }
                return false;
            }
        });
        mail_layout = findViewById(R.id.signup_email_layout);
        password_layout = findViewById(R.id.signup_password_layout);
        secondPassword_layout = findViewById(R.id.signup_reenter_layout);

    }

    public void submit(View v){
        // lock the submit function to avoid calling it multiple times at a time
        signUp.setEnabled(false);
        if (signUpClicked ){
            return;
        }
        else{
            signUpClicked = true;
        }
        String name = last_name.getText().toString() + " " + first_name.getText().toString();
        String email = mail.getText().toString();
        String passW = password.getText().toString();
        String check = secondPassword.getText().toString();
        boolean isValidPassword = validPassword(passW);

        // check if the user input email is valid
        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            clearError(mail_layout);
            // check if the password is valid
            if (!isValidPassword){
                signUpClicked = false;
                signUp.setEnabled(true);
//                Toast.makeText(getApplicationContext(), "Password length should be " +
//                        "longer than 8", Toast.LENGTH_LONG).show();
                displayError(password_layout, password, "Password length should be at least 8");
            }
            else {
                // check if the two password inputs are the same
                clearError(password_layout);
                if(!passW.equals(check)){
//                Toast.makeText(getApplicationContext(), "Password doesn't match" ,
//                        Toast.LENGTH_LONG).show();
                    displayError(secondPassword_layout, secondPassword, "The two passwords don't match");
                    signUpClicked = false;
                    signUp.setEnabled(true);
                }
                else{
                    // all inputs are valid, try to create user account
                    clearError(secondPassword_layout);
                    createAccount(email, passW, name);
                }
            }
        }
        else {
            signUpClicked = false;
            signUp.setEnabled(true);
//            Toast.makeText(getApplicationContext(),"Enter Valid Email",
//                    Toast.LENGTH_LONG).show();
            displayError(mail_layout, mail, "Not a valid Email");
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
    @Override
    public void onResume(){
        super.onResume();
        signUp = findViewById(R.id.signUpButton);
        signUp.setEnabled(true);
    }
    public void openLoginPage(){
        signUp.setEnabled(false);
        Log.d(TAG, "open Login Page");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public boolean validPassword(String password){
        return password.length() >= PASSWORD_LENGTH;
    }

    private void createAccount(String email, String password, String name) {
//        Log.d(TAG, "createAccount:" + email);
//        if (!validateForm()) {
//            return;
//        }
        // start the timer and listener to check is the session has timeout
        registerSessionListner(this);
        startSignupSession();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:success");
                        if (task.isSuccessful()) {
                            timer.cancel();         // cancel the timer since succeed
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                            }
                                        }
                                    });
                            sendEmailVerification();
                        } else {
                            timer.cancel();     // cancel the timer since failed, enable user to login again
                            signUpClicked = false;
                            signUp.setEnabled(true);
                            try
                            {
                                throw task.getException();
                            }
                            // check if email already registered by user
                            catch (FirebaseAuthUserCollisionException existEmail)
                            {
                                Log.d(TAG, "onComplete: exist_email");
//                                Toast.makeText(SignUpActivity.this, "Email already used"
//                                        , Toast.LENGTH_SHORT).show();
                                displayError(mail_layout, mail, "This Email is already in use");

                            }
                            // catch all other exceptions
                            catch (Exception e)
                            {
                                Log.d(TAG, "onComplete: " + e.getMessage());
                                Toast.makeText(SignUpActivity.this, "Authentication" +
                                                " failed.", Toast.LENGTH_SHORT).show();
                            }
                            // If sign in fails, display a message to the user.

                        }
                    }
                });
    }

    /**
     * Send email verification after user has created account
     */
    public void sendEmailVerification() {
        // [START send_email_verification]
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(SignUpActivity.this, "Verification" +
                                    "Email Sent", Toast.LENGTH_LONG).show();
                            openLoginPage();
                        }else{
                            Log.d(TAG,"Sent Email Failed");
                            Toast.makeText(SignUpActivity.this, "Fail to send" +
                                    "verification email", Toast.LENGTH_LONG).show();
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

    private void clearError(@NonNull TextInputLayout layout) {
        layout.setError(null);
    }

    private void displayError(@NonNull TextInputLayout layout, @NonNull TextInputEditText editText, String error) {
        layout.setError(error);
        editText.setSelection(editText.getText().length());
        editText.requestFocus();
    }

    /**
     * start a new SignUpActivity, finish the old one
     */
    @Override
    public void onSessionTimeout(){
        Log.d(TAG, "signup session timeout");
        startActivity(new Intent(this, SignUpActivity.class));
        sp.edit().putBoolean("isTimeout", true).apply();
        finish();
    }

    /**
     * create a new timer, after 10s, make the signup session timeout
     */
    public void startSignupSession(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeoutListener.onSessionTimeout();
            }
        }, 10000);
    }

    /**
     * create a timeout listener
     * @param listener check if the timer is completed
     */
    public void registerSessionListner(SessionTimeoutListener listener){
        this.timeoutListener = listener;
    }
}
