package com.hello.khushboo.replonguesty.CheckoutGuests;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hello.khushboo.replonguesty.GuestDataFirebase;
import com.hello.khushboo.replonguesty.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import static java.lang.Boolean.TRUE;

public class GuestCheckoutActivity extends AppCompatActivity {

    public static final String TAG = "GuestCheckoutActivity";
    ImageView back_checkout,filter;
    RecyclerView recyclerView;
    CheckoutAdapter adapter;
    String checkout_time = "--NA--";
    String document_id;
    DocumentReference soc_id_ref;

    TextView start_date,end_date;
    DatePickerDialog pickerDialog;

    int dayStart=0, monthStart, yearStart, dayEnd, monthEnd, yearEnd;
    Calendar cldrStart;
    Date dateStart, dateEnd;
    Button apply_filters;
    EditText searchView;



    List<GuestDataFirebase> checkout_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_checkout);

        back_checkout = (ImageView) findViewById(R.id.back_checkout);
        back_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        checkout_list = new ArrayList<>();

        filter=findViewById(R.id.filters);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //showFilterDialogue();
            }
        });

        searchView = findViewById(R.id.search_field);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());

            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);


        getData();
//        adapter=new CheckoutAdapter(getApplicationContext(),checkout_list);
//        recyclerView.setAdapter(adapter);
    }

    private void filter(String text){
        List<GuestDataFirebase> temp = new ArrayList();
        for(GuestDataFirebase d: checkout_list){
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if(d.getName().toLowerCase().contains(text)){
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);

    }

    private void getData() {

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestlist = getString(R.string.guestlist);
        Log.i(TAG,"Doc ref is "+docRef);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {

                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if(snapshot!=null && snapshot.exists()){
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());

                    soc_id_ref=(DocumentReference) snapshot.get("society_id");


                    Log.i(TAG,"society id " + soc_id_ref);

                    soc_id_ref.collection(guestlist).orderBy("date_created", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else {
                                        checkout_list.clear();
//                                      mAdapter.notifyDataSetChanged();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                        } else {

                                            for (QueryDocumentSnapshot document : snapshots) {
                                                String name=document.get("name").toString();
                                                Log.i(TAG,"name is "+name);

                                                if (document.get("checkout_time") != null) {

                                                    Date fb_date_out = ((Timestamp) document.get("checkout_time")).toDate();

                                                    Date fb_date = ((Timestamp) document.get("date_created")).toDate();

                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String in_date = sfd.format(fb_date);

                                                    sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String out_date = sfd.format(fb_date_out);

                                                    checkout_list.add(new GuestDataFirebase(
                                                           document.getString("car_type"),
                                                            document.getBoolean("checkout"),
                                                            out_date,
                                                            in_date,
                                                            document.getDocumentReference("document_id"),
                                                            document.get("flat_no").toString(),
                                                            document.getBoolean("frequent_visitor"),
                                                            document.getString("name"),
                                                           "",
                                                            document.getString("profile_image_url"),
                                                            document.getString("purpose"),
                                                            document.getString("user_id"),
                                                            document.getString("vehicle_image_url"),
                                                            document.getString("vehicle_number")
                                                    ));


                                                } else {

                                                    Date fb_date = ((Timestamp) document.get("date_created")).toDate();

                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String in_date = sfd.format(fb_date);

                                                    checkout_list.add(new GuestDataFirebase(
                                                            document.getString("car_type"),
                                                            document.getBoolean("checkout"),
                                                            checkout_time,
                                                            in_date,
                                                            document.getDocumentReference("document_id"),
                                                            document.get("flat_no").toString(),
                                                            document.getBoolean("frequent_visitor"),
                                                            document.getString("name"),
                                                            "",
                                                            document.getString("profile_image_url"),
                                                            document.getString("purpose"),
                                                            document.getString("user_id"),
                                                            document.getString("vehicle_image_url"),
                                                            document.getString("vehicle_number")
                                                    ));


                                                }

                                            }
                                            callAdapter();
//                                            adapter=new CheckoutAdapter(getApplicationContext(),checkout_list);
                                            recyclerView.setAdapter(adapter);
                                        }
                                    }

                                }
                            });
                }

            }
        });

    }

    private void callAdapter(){
        adapter=new CheckoutAdapter(getApplicationContext(),checkout_list){

            @Override
            public void onBindViewHolder(@NonNull CheckoutHolder checkoutHolder, int i) {

                final GuestDataFirebase guestDataFirebase=checkoutList.get(i);

                Log.i(TAG,"inCallAdapter");
                checkoutHolder.guest_name.setText(String.valueOf(guestDataFirebase.getName()));
                checkoutHolder.guest_purpose.setText(String.valueOf(guestDataFirebase.getPurpose()));
                checkoutHolder.guest_flatno.setText(String.valueOf(guestDataFirebase.getFlat_no()));
                checkoutHolder.dateAndTime.setText(String.valueOf(guestDataFirebase.getCheck_in_time()));
                checkoutHolder.dateAndTime_out.setText(String.valueOf(guestDataFirebase.getCheckout_time()));

                if(guestDataFirebase.getCheckout()){
                    checkoutHolder.btn_checkout.setVisibility(View.GONE);
                    checkoutHolder.dateAndTime_out.setText(String.valueOf(guestDataFirebase.getCheckout_time()));
                }

                checkoutHolder.btn_checkout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG,"Button pressed");
                        checkout(guestDataFirebase.getDocument_id().getId());
                    }
                });

            }
        };

    }


    private void checkout(String document_id_checkout) {


       // final FirebaseFirestore db = FirebaseFirestore.getInstance();
       // final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String guestlist = getString(R.string.guestlist);
//        final String user = getString(R.string.user);
//        final String user_id = currentFirebaseUser.getUid();

//        Log.i(TAG,"Document id inside checkout is "+document_id);
//        Log.i(TAG,"Soc_id_ref "+soc_id_ref.toString());

        soc_id_ref.collection(guestlist).document(document_id_checkout).update("checkout", TRUE);
        soc_id_ref.collection(guestlist).document(document_id_checkout).update("checkout_time", new Timestamp(new Date()));


    }
    @Override
    public void onBackPressed() {
        finish();
    }
}
