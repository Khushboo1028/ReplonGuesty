package com.hello.khushboo.replonguesty;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hello.khushboo.replonguesty.AddGuests.AddGuestActivity;

public class
MainActivity extends AppCompatActivity {

    public static final String TAG = "Main Activity";
    Button logout;
    ImageView back_add_guest;

    RelativeLayout add,out,freq;

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        add = (RelativeLayout) findViewById(R.id.gcheckin);
        out = (RelativeLayout) findViewById(R.id.gcheckout);
        freq = findViewById(R.id.freq_visitors);


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddGuestActivity.class));

            }
        });

        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), GuestCheckoutActivity.class));
            }
        });

        freq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   startActivity(new Intent(getApplicationContext(), FrequentVisitorsActivity.class));
            }
        });
        setupFirebaseAuth();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //checks to see if the @param user is logged in
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG,"Checking if user is logged in");
        if(user==null){
            Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setupFirebaseAuth() {
        Log.d(TAG,"onAuthStateChanged:Setting up firebase auth");

        // Obtain the FirebaseAnalytics instance.
        mAuth = FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser=mAuth.getCurrentUser();

                //checks if the user is logged in
                checkCurrentUser(currentUser);
                if(currentUser!=null){
                    //user is signed in
                    Log.d(TAG,"onAuthStateChanged:signed_in");
                }
                else{
                    //user is signed out
                    Log.d(TAG,"onAuthStateChanged:signed_out");
                }


            }
        };

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkCurrentUser(currentUser);

    }
    @Override
    public void onStop() {
        super.onStop();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuthListener!=null){
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
}
