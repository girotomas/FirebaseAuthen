package giro.tomas.com.googlemaptracker.Firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import giro.tomas.com.googlemaptracker.Locations;
import giro.tomas.com.googlemaptracker.R;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 123;
    GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "MainActivity";
    FirebaseDatabase database;
    private FirebaseAuth mAuth;
    DatabaseReference myUsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser =mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private  void signIn(){
        Intent signInIntent= mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // SignIn successful
            firebaseAuthWithGoogle(account);

        }catch (ApiException e){
            Log.w(TAG, "handleSignInResult: signIn Result failed code="+e.getStatusCode());
            updateUI(null);
        }
    }


    private  void updateUI(FirebaseUser account){
        Log.i(TAG, "updateUI: the account is: "+ (account!=null? account.getEmail(): "no account"));
        if(account==null){
            return;
        }else{
            database= FirebaseDatabase.getInstance();
            myUsersRef = database.getReference("users");
            User user= new User(account.getEmail(),account.getDisplayName());
            myUsersRef.child(account.getUid()).setValue(user);
            Intent intent = new Intent(LogInActivity.this,Locations.class);
            startActivity(intent);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        Log.d(TAG, "firebaseAuthWithGoogle: "+acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Sign in success, update ui with the signed-in user
                            Log.d(TAG, "signINWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            // If sign-in fails, displa a message to the user
                            Log.w(TAG, "signInWithCredential:failure",task.getException() );
                            Toast.makeText(LogInActivity.this,"authentification failed",Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

}
