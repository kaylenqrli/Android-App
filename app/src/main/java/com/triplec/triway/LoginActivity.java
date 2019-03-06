package com.triplec.triway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText mail, password;
    private TextInputLayout mail_layout, password_layout;
    private Button loginInButton;
    private Button signUpButton;
    private CheckBox checkBox;
    private final int PASSWORD_LENGTH = 8;
    SharedPreferences autologinSp;
    SharedPreferences rememberSp;
    private final String validEmail = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +

            "\\@" +

            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +

            "(" +

            "\\." +

            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +

            ")+";

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginInButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupButton);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        // check if the user logged before, auto-login if true
        checkBox = findViewById(R.id.ch_rememberme);
        autologinSp = getSharedPreferences("login", MODE_PRIVATE);
        if(autologinSp.getBoolean("logged",false)){
            openHomeActivity();
        }

        //setContentView(R.layout.activity_login);
        mail_layout = findViewById(R.id.login_email_layout);
        password_layout = findViewById(R.id.login_password_layout);
        mail = findViewById(R.id.login_email);
        mail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        || (i == EditorInfo.IME_ACTION_NEXT)){
                    String email = mail.getText().toString();
                    Matcher matcher= Pattern.compile(validEmail).matcher(email);
                    if (matcher.matches()){
                        mail_layout.setError(null);
                    }
//                    else {
//                        mail_layout.setError("The Email you entered is not valid");
//                    }
                }
                return false;
            }
        });

        password = findViewById(R.id.login_password);

        // check if user has saved their email and password before
        rememberSp = getSharedPreferences("savedPref", MODE_PRIVATE);
        if(rememberSp.getBoolean("isRemembered", false)){
            // read user's data from sharedPreference
            Log.d(TAG, "saved before, should auto set email");
            mail.setText(rememberSp.getString("userEmail",""));
            checkBox.setChecked(true);
        }


        // make done the default button when user has input password
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                || (i == EditorInfo.IME_ACTION_DONE)){
                    // hide keyboard when click done
                    InputMethodManager imm = (InputMethodManager) textView.getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    login(null);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onResume(){
        super.onResume();
        loginInButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupButton);
        loginInButton.setEnabled(true);
        signUpButton.setEnabled(true);
    }
    /**
     * Login method, first check email&password are in valid form, if yes, attempt to login.
     * If fail, make a login failure toast.
     * @param v
     */
    public void login(View v) {
        // check if the email & password match the valid form
        loginInButton.setEnabled(false);
        String email = mail.getText().toString();
        String passW = password.getText().toString();
        if (validateForm(email, passW)) {

            mAuth.signInWithEmailAndPassword(email, passW)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if(mAuth.getCurrentUser().isEmailVerified()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    //user = mAuth.getCurrentUser();
                                    autologinSp.edit().putBoolean("logged", true).apply();

                                    // remember user's email address and password if ckeckbox is checked
                                    if(checkBox.isChecked()){
                                        rememberSp.edit().putString("userEmail", email).apply();
                                        rememberSp.edit().putBoolean("isRemembered", true).apply();
                                        Log.d(TAG, "user email saved");
                                    }else{          //if checkBox not checked, clear the sharedPreference
                                        rememberSp.edit().clear().commit();
                                    }
                                    openHomeActivity();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this, "Please " +
                                            "verify your email address",Toast.LENGTH_LONG).show();
                                    loginInButton.setEnabled(true);
                                    Log.d(TAG, "no verification");
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Email and Password doesn't match",
                                        Toast.LENGTH_LONG).show();
                                loginInButton.setEnabled(true);
                                Log.d(TAG, "open home activity failed");
                            }
                        }
                    });
        }else{
            loginInButton.setEnabled(true);
            Log.d(TAG, "invalid email or password");
        }
    }

    public void openSignUpPage(View v){
        signUpButton.setEnabled(false);
        Log.d(TAG, "open sign up page");
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void openHomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();               // terminate LoginActivity after login
    }

    // check if the password length is greater than or equal than 8
    public boolean validPassword(String password){
        return password.length() >= PASSWORD_LENGTH;
    }



    /**
     * validateForm, check if email matches the standard format and password length is greater than 8
     * @return
     */
    private boolean validateForm(String email, String password){
        boolean isValidPassword = validPassword(password);

        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            if(isValidPassword){
                return true;
            }
            else{
                Toast.makeText(getApplicationContext(), "Password length should at least be" +
                        "8", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"Enter Valid Email",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }


}
