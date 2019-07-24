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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.hello.khushboo.replonguesty.AddGuests.AddGuestActivity;
import com.hello.khushboo.replonguesty.CheckoutGuests.GuestCheckoutActivity;
import com.hello.khushboo.replonguesty.FrequentVisitors.FrequentVisitorsActivity;

import javax.annotation.Nullable;

public class
MainActivity extends AppCompatActivity {

    public static final String TAG = "Main Activity";
    Button logout;
    ImageView back_add_guest;

    RelativeLayout add,out,freq;
    TextView welcome_text;

    String soc_name;
    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  mAuth=FirebaseAuth.getInstance();


        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });
        welcome_text = (TextView) findViewById(R.id.welcome_text);
      //  db = FirebaseFirestore.getInstance();
//        DocumentReference doc_ref = db.collection(getString(R.string.user)).document(mAuth.getCurrentUser().getUid());
//        doc_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w(TAG, "Listen failed.", e);
//                    return;
//                }
//                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
//                        ? "Local" : "Server";
//
//                Log.i(TAG,"Source is "+source);
//
//                Log.i(TAG,"snapshot exits?  "+snapshot.exists());
//
//                if (snapshot != null &&snapshot.exists()) {
//
//                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());
//
//
//                    final DocumentReference soc_id_ref = (DocumentReference) snapshot.get("society_id");
//
//                    Log.i(TAG, "Society ref is " + soc_id_ref);
//                    soc_id_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
//                            if (e != null) {
//                                Log.w(TAG, "Listen failed.", e);
//                                return;
//                            }
//                            String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
//                                    ? "Local" : "Server";
//
//                            Log.i(TAG,"Source is "+source);
//
//                            Log.i(TAG,"snapshot exits?  "+snapshot.exists());
//
//                            if (snapshot != null &&snapshot.exists()) {
//
//                                soc_name = snapshot.getString("society_name");
//                                Log.i(TAG,"SOC NAME:"+soc_name);
//                                welcome_text.setText("Welcome to "+ soc_name);
//                            }
//                        }
//                    });
//                }
//            }
//        });



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
                startActivity(new Intent(getApplicationContext(), GuestCheckoutActivity.class));
            }
        });

        freq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FrequentVisitorsActivity.class));
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

                      db = FirebaseFirestore.getInstance();
        DocumentReference doc_ref = db.collection(getString(R.string.user)).document(mAuth.getCurrentUser().getUid());
        doc_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                Log.i(TAG,"Source is "+source);

                Log.i(TAG,"snapshot exits?  "+snapshot.exists());

                if (snapshot != null &&snapshot.exists()) {

                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());


                    final DocumentReference soc_id_ref = (DocumentReference) snapshot.get("society_id");

                    Log.i(TAG, "Society ref is " + soc_id_ref);
                    soc_id_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }
                            String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                                    ? "Local" : "Server";

                            Log.i(TAG,"Source is "+source);

                            Log.i(TAG,"snapshot exits?  "+snapshot.exists());

                            if (snapshot != null &&snapshot.exists()) {

                                soc_name = snapshot.getString("society_name");
                                Log.i(TAG,"SOC NAME:"+soc_name);
                                welcome_text.setText("Welcome to "+ soc_name);
                            }
                        }
                    });
                }
            }
        });
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
