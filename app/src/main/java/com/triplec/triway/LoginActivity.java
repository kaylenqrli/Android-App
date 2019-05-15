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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
/*
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
*/
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements SessionTimeoutListener{
    private static final int RC_SIGN_IN = 9001;
    private TextInputEditText mail_et, password_et;
    private TextInputLayout mail_layout, password_layout;
    private Button loginInButton;
    private Button signUpButton;
    private ImageButton googleSigninButton;
    private LoginButton facebookImageButton;
    private ImageButton TwitterSigninButton;
    private CheckBox checkBox;
    private final int PASSWORD_LENGTH = 8;
    private boolean loginClicked;
    private Timer timer;
    private boolean forgetClicked;
    private TextView forgetPassword;
    private SessionTimeoutListener timeoutListener;
    private CallbackManager mCallbackManager;   // facebook
    GoogleSignInClient mGoogleSignInClient;
    SharedPreferences sp;
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
        Intent intent = getIntent();
        if (intent.getBooleanExtra("Splash", false)) {
            overridePendingTransition(R.transition.fade_in,R.transition.fade_out);
        }
        setContentView(R.layout.activity_login);

        loginInButton = findViewById(R.id.loginButton);
        loginInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        });
        forgetPassword = findViewById(R.id.forget_password_view);

        signUpButton = findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpPage(v);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        // check if the user logged before, auto-login if true
        checkBox = findViewById(R.id.ch_rememberme);
        sp = getSharedPreferences("login", MODE_PRIVATE);
        loginClicked = false;
        if(sp.getBoolean("isTimeout", false)){
            Log.d(TAG, "timeout");
            Toast.makeText(this,"Login session timeout!", Toast.LENGTH_LONG).show();
            sp.edit().putBoolean("isTimeout", false).apply();
        }
        if(sp.getBoolean("logged",false)){
            openHomeActivity();
        }
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openForgetActivity();
            }
        });
        // Google third party Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("619574719784-v5pepuomg9gr6mqjkd0grh7dsear8eg0.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSigninButton = findViewById(R.id.login_google);
        googleSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        // Facebook third party login
        initializeFacebook();

        // Twitter third party login
        // initializeTwitter();
        mail_layout = findViewById(R.id.login_email_layout);
        password_layout = findViewById(R.id.login_password_layout);
        mail_et = findViewById(R.id.login_email);
        password_et = findViewById(R.id.login_password);

        // check if user has saved their email and password_et before
        rememberSp = getSharedPreferences("savedPref", MODE_PRIVATE);
        if(rememberSp.getBoolean("isRemembered", false)){
            // read user's data from sharedPreference

            mail_et.setText(rememberSp.getString("userEmail",""));
            password_et.setText(rememberSp.getString("userPassword",""));
            checkBox.setChecked(true);
        }
        // make done the default button when user has input password_et
        password_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        // reset all the login & signup flag, allow user to login & sign up again
        loginInButton.setEnabled(true);
        signUpButton.setEnabled(true);
        loginClicked = false;
        forgetClicked = false;
    }
    /**
     * Login method, first check email&password_et are in valid form, if yes, attempt to login.
     * If fail, make a login failure toast.
     * @param v
     */
    public void login(View v) {
        // lock the method to avoid login twice at the same time
        if( loginClicked ){
            return;
        }
        else{
            loginClicked = true;
        }
        loginInButton.setEnabled(false);
        String email = mail_et.getText().toString();
        String passW = password_et.getText().toString();
        // check if email & password in valid form
        if (validateForm(email, passW)) {
            // start the timer and listener to check is the session has timeout
            registerSessionListner(this);
            startLoginSession();
            mAuth.signInWithEmailAndPassword(email, passW)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // cancel the timer since login successful
                                timer.cancel();
                                if(mAuth.getCurrentUser().isEmailVerified()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    //user = mAuth.getCurrentUser();
                                    sp.edit().putBoolean("logged", true).apply();

                                    // remember user's email address and password_et if ckeckbox is checked
                                    if(checkBox.isChecked()){
                                        rememberSp.edit().putString("userEmail", email).apply();
                                        rememberSp.edit().putBoolean("isRemembered", true).apply();
                                        Log.d(TAG, "user email saved");
                                    }else{          //if checkBox not checked, clear the sharedPreference
                                        rememberSp.edit().clear().commit();
                                    }
                                    clearError(mail_layout);
                                    clearError(password_layout);
                                    openHomeActivity();
                                }
                                else{
                                    // cancel the timer and reset the login flag since failed, allow user to login again
                                    timer.cancel();
                                    loginInButton.setEnabled(true);
                                    loginClicked = false;
                                    Log.d(TAG, "no verification");
                                    displayError(mail_layout, mail_et,"Please check your Email for verification");
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                if (task.getException().getClass().equals(FirebaseAuthInvalidUserException.class)) {
                                    displayError(mail_layout, mail_et, "This Email is not registered");
                                }
                                else {
                                    displayError(password_layout, password_et, "Incorrect password");
                                }
                                // reset the login button and the flag since failed, allow user to login again
                                loginClicked = false;
                                loginInButton.setEnabled(true);
                                Log.d(TAG, "open home activity failed");
                            }
                        }
                    });
        }else{
            // reset the login button and the flag since failed, allow user to login again
            loginClicked = false;
            loginInButton.setEnabled(true);
            Log.d(TAG, "invalid email or password");
        }
    }

    public void openSignUpPage(View v){
        // lock the signUpButoon when openSignUpPage is called
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

    public void openForgetActivity(){
        if(forgetClicked){
            return;
        }
        else{
            forgetClicked = true;
        }
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);

    }

    // check if the password_et length is greater than or equal than 8
    public boolean validPassword(String password){
        return password.length() >= PASSWORD_LENGTH;
    }



    /**
     * validateForm, check if email matches the standard format and password_et length is greater than 8
     * @return
     */
    private boolean validateForm(String email, String password){
        boolean isValidPassword = validPassword(password);

        Matcher matcher= Pattern.compile(validEmail).matcher(email);
        if (matcher.matches()){
            clearError(mail_layout);
            if(isValidPassword){
                clearError(password_layout);
                return true;
            }
            else{
                displayError(password_layout, password_et, "Password length should be at least 8");
                return false;
            }
        }
        else{
            displayError(mail_layout, mail_et, "Not a valid Email");
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
     * After timer is done, finish the current LoginActivity and start a new one
     */
    @Override
    public void onSessionTimeout(){
        Log.d(TAG, "login session timeout");
        startActivity(new Intent(this, LoginActivity.class));
        sp.edit().putBoolean("isTimeout", true).apply();
        //Toast.makeText(getApplicationContext(),"Login session timeout!", Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * After Clicked login Button, timer begins. After 10s, make the login session timeout
     */
    public void startLoginSession(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeoutListener.onSessionTimeout();
            }
        }, 10000);
    }

    // create a sessionTimeOut Listner
    public void registerSessionListner(SessionTimeoutListener listener){
        this.timeoutListener = listener;
    }

    /**
     * Google Third Party Login Method
     */
    private void googleSignIn() {
        // if clicked one, lock the method to avoid open homeActivity twice
        if( loginClicked ){
            return;
        }
        else{
            loginClicked = true;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle three third party login based on different requestCode,
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                loginClicked = false;
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        /*
        else if(requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE){
            //  twitter related handling
            TwitterSigninButton.onActivityResult(requestCode, resultCode, data);

        }*/
        else{
            // facebook related handling
            Log.d(TAG, "facebook onActivityResult");
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Connect Google Account to firebase
     * @param acct user's google account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        // create a timer and a listener, after 10s, login session timeout if activity no response
        //registerSessionListner(this);
        //startLoginSession();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //timer.cancel();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            sp.edit().putBoolean("logged", true).apply();
                            openHomeActivity();
                        } else {
                            //timer.cancel();
                            // If sign in fails, display a message to the user
                            loginClicked = false;
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login in Failed",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void initializeFacebook(){
        mCallbackManager = CallbackManager.Factory.create();
        facebookImageButton = findViewById(R.id.facebook_login);
        facebookImageButton.setReadPermissions("email","public_profile");
        facebookImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        loginClicked = false;
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onError(FacebookException error) {
                        loginClicked = false;
                        Log.d(TAG, "facebook:onError", error);

                    }
                });
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        if( loginClicked ){
            return;
        }
        else{
            loginClicked = true;
        }
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        //registerSessionListner(this);
        //startLoginSession();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //timer.cancel();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            sp.edit().putBoolean("logged", true).apply();
                            FirebaseUser user = mAuth.getCurrentUser();
                            openHomeActivity();
                        } else {
                            //timer.cancel();
                            loginClicked = false;
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

/*
    private void initializeTwitter(){
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();

        Twitter.initialize(twitterConfig);
        TwitterSigninButton= findViewById(R.id.buttonTwitterLogin);
        TwitterSigninButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                loginClicked = false;
            }
        });
    }

    // [START auth_with_twitter]
    private void handleTwitterSession(TwitterSession session) {
        if( loginClicked ){
            return;
        }
        else{
            loginClicked = true;
        }
        Log.d(TAG, "handleTwitterSession:" + session);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        registerSessionListner(this);
        startLoginSession();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            timer.cancel();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            sp.edit().putBoolean("logged", true).apply();
                            openHomeActivity();

                        } else {
                            timer.cancel();
                            loginClicked = false;
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_twitter]
    */
}
