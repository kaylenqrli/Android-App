package com.triplec.triway;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
    private final int PASSWORD_LENGTH = 8;
    SharedPreferences sp;
    private final String validEmail = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +

            "\\@" +

            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +

            "(" +

            "\\." +

            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +

            ")+";

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("login", MODE_PRIVATE);
        if(sp.getBoolean("logged",true)){
            openHomeActivity();
        }
        setContentView(R.layout.activity_login);
        mail = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                || (i == EditorInfo.IME_ACTION_DONE)){
                    login(null);
                }
                return false;
            }
        });

    }

    public void login(View v) {
        String email = mail.getText().toString();
        String passW = password.getText().toString();
        boolean isValidPassword = validPassword(passW);

        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            if (!isValidPassword){
                Toast.makeText(getApplicationContext(), "Password length should at least be" +
                        "8", Toast.LENGTH_LONG).show();
            }
            else{
                signIn(email, passW);
            }

        }
        else {
            Toast.makeText(getApplicationContext(),"Enter Valid Email",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void openSignUpPage(View v){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void openHomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean validPassword(String password){
        return password.length() >= PASSWORD_LENGTH;
    }

    @Override
    public void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            sp.edit().putBoolean("logged", true).apply();
                            openHomeActivity();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }


                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Fail",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }

    /**
     * validateForm
     * @return
     */
    private boolean validateForm(){
        mail = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        String email = mail.getText().toString();
        String passW = password.getText().toString();
        boolean isValidPassword = validPassword(passW);

        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            if(isValidPassword){
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
