package com.hello.khushboo.replonguesty.FrequentVisitors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hello.khushboo.replonguesty.AddGuests.AddGuestActivity;
import com.hello.khushboo.replonguesty.CheckoutGuests.GuestCheckoutViewActivity;
import com.hello.khushboo.replonguesty.GuestDataFirebase;
import com.hello.khushboo.replonguesty.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class FrequentVisitorsActivity extends AppCompatActivity {

    public static final String TAG = "FrequentVisitorActivity";
    String user;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentFirebaseUser;

    String user_id;
    DocumentReference docRef;

    DocumentReference soc_id_ref;
    ListenerRegistration getDataListener;

    List<GuestDataFirebase> frequent_list;
    VisitorsAdapter mAdapter;
    RecyclerView recyclerView;

    String[] flats;

    TextView no_freq_visitors;

    ImageView back;
    EditText searchView;
    List<GuestDataFirebase> guestArrayList;
    ProgressBar progressBar;

    ListenerRegistration getGetDataListener;
    List<String> flat_nos,categories;
    String unique_id,flat_doc_id;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequent_visitors);

        user=getString(R.string.user);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentFirebaseUser=mAuth.getCurrentUser();
        user_id=currentFirebaseUser.getUid();

        docRef=db.collection(user).document(user_id);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); //so it doesn't matter if element size increases or decreases
        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        no_freq_visitors = (TextView) findViewById(R.id.no_freq_visitors);

        unique_id=getIntent().getStringExtra("unique_id");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));//by default manager is vertical

        frequent_list=new ArrayList();
        //array = new String[]{"A - 101", "A - 102","A - 103","A - 104","A - 201","A - 202","A - 203","A - 204","A - 301","A - 302","A - 303","A - 304","A - 401","A - 402","A - 403","A - 404"};

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        unique_id=getIntent().getStringExtra("unique_id");


        categories = new ArrayList<>();

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


        getData();

        getFlats();
        mAdapter = new VisitorsAdapter(this,frequent_list){

            @Override
            public void onBindViewHolder(@NonNull final VisitorViewHolder holder, int i) {


                //so this i is position that will give you the specified item from the product list!


                final GuestDataFirebase guestDataFirebase=frequent_list.get(i);

                holder.guest_name.setText(String.valueOf(guestDataFirebase.getName()));
                holder.purpose.setText(String.valueOf(guestDataFirebase.getPurpose()));

                if(guestDataFirebase.getCheckout()!=null) {


                    if (guestDataFirebase.getCheckout()) {
                        holder.btn_checkin.setVisibility(View.VISIBLE);
                        holder.curr_checkin.setVisibility(View.GONE);
                    } else {
                        holder.btn_checkin.setVisibility(View.GONE);
                        holder.curr_checkin.setVisibility(View.VISIBLE);
                    }
                    if (String.valueOf(guestDataFirebase.getVehicle_no()).equals("")) {
                        holder.veh_num.setText("--NA--");
                    } else {
                        holder.veh_num.setText(String.valueOf(guestDataFirebase.getVehicle_no()));
                    }
                }
                holder.phone.setText(String.valueOf(guestDataFirebase.getPhone_no()));

                holder.btn_checkin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flat_nos = new ArrayList<>();
                        Log.i(TAG,"CHECKIN CLICKED");

                        final AlertDialog.Builder builder =
                                new AlertDialog.Builder(FrequentVisitorsActivity.this);
                        builder.setTitle("Select Flat Nos.")
                                .setMultiChoiceItems(flats, null,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            public void onClick(DialogInterface dialog, int item, boolean isChecked) {

                                                if (isChecked) {
                                                    // if the user checked the item, add it to the selected items
                                                    flat_nos.add(flats[item]);
                                                }

                                                else if (flat_nos.contains(flats[item])) {
                                                    // else if the item is already in the array, remove it
                                                    flat_nos.remove(String.valueOf(flats[item]));
                                                }
                                            }
                                        })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        String selectedIndex = "";
                                        for(String i : flat_nos){
                                            selectedIndex += i + ", ";
                                        }
                                        Log.i(TAG,"FLAT NOS SELECTED ARE: " + selectedIndex);
                                        //addData();
                                        holder.curr_checkin.setVisibility(View.VISIBLE);
                                        holder.btn_checkin.setVisibility(View.GONE);

                                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                        final String guestData = getString(R.string.guestlist);
                                        final String user = getString(R.string.user);
                                        final String guest_name = getString(R.string.name);
                                        final String guest_phone_number = getString(R.string.phone_number);
                                        final String guest_purpose = getString(R.string.purpose);
                                        final String guest_flat_no = getString(R.string.flat_no);
                                        final String date_created = getString(R.string.date_created);
                                        final String guest_user_id = getString(R.string.user_id);
                                        final String document_id = getString(R.string.document_id);
                                        final String document_ref=getString(R.string.document_ref);
                                        final String vehicle_number_fb = getString(R.string.vehicle_number);
                                        final String car_type_fb = getString(R.string.car_type);
                                        final String profile_image_url = getString(R.string.profile_image_url);
                                        final String frequent_visitor = getString(R.string.vehicle_image_url);
                                        final String vehicle_image_url=getString(R.string.vehicle_image_url);
                                        final String user_id = currentFirebaseUser.getUid();


                                        guestArrayList = new ArrayList<GuestDataFirebase>();



                                        final DocumentReference docRef = db.collection(user).document(user_id);
                                        Log.i(TAG,"DOCUMENT REFERENCE IS "+docRef.toString());

                                        getDataListener=docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot,
                                                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    Log.w(TAG, "Listen failed.", e);
                                                    return;
                                                }
                                                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                                                        ? "Local" : "Server";

                                                Log.i(TAG,"Source is "+source);


                                                if (snapshot != null && snapshot.exists()) {

                                                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());


                                                    final DocumentReference soc_id_ref = (DocumentReference) snapshot.get("society_id");

                                                    Log.i(TAG, "Society id is " + soc_id_ref);

//
                                                    if(guestDataFirebase.getProfile_image_url()==null){
                                                        guestDataFirebase.setProfile_image_url("");
                                                    }

                                                    final Map<String, Object> data = new HashMap<>();
                                                    data.put(date_created, new Timestamp(new Date()));
                                                    data.put(guest_name,guestDataFirebase.getName());
                                                    data.put(guest_phone_number, guestDataFirebase.getPhone_no());
                                                    data.put(guest_purpose, guestDataFirebase.getPurpose());
                                                    data.put(guest_flat_no, flat_nos);
                                                    data.put(guest_user_id, user_id);
                                                    data.put(vehicle_number_fb, guestDataFirebase.getVehicle_no());
                                                    data.put(car_type_fb, guestDataFirebase.getCar_type());
                                                    data.put(profile_image_url, guestDataFirebase.getProfile_image_url());
                                                    data.put(getString(R.string.checkout), FALSE);
                                                    data.put(getString(R.string.checkout_time), null);
                                                    data.put(getString(R.string.frequent_visitor),TRUE);
                                                    data.put(vehicle_image_url,guestDataFirebase.getVehicle_image_url());

                                                    DocumentReference document_id=soc_id_ref.collection(guestData).document();
                                                    data.put(document_ref,document_id);

                                                    document_id.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {


                                                            guestDataFirebase.getDocument_id().update(getString(R.string.frequent_visitor),FALSE)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.i(TAG,"Frequent visitor updated");
                                                                            progressBar.setVisibility(View.GONE);
                                                                            showMessage("Success","Guest checked in",R.drawable.ic_success_dialog);

                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    showMessage("Unable to add guest","An internal error occurred",R.drawable.ic_error_dialog);

                                                                }
                                                            });


                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressBar.setVisibility(View.GONE);
                                                            showMessage("Unable to add guest","An internal error occurred",R.drawable.ic_error_dialog);

                                                        }
                                                    });

                                                } else {
                                                    Log.d(TAG, source + " data: null");
                                                }
                                            }
                                        });


                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FrequentVisitorsActivity.this, GuestCheckoutViewActivity.class);
                        intent.putExtra("name",String.valueOf(guestDataFirebase.getName()));
                        intent.putExtra("checkin",String.valueOf(guestDataFirebase.getCheck_in_time()));
                        intent.putExtra("checkout",String.valueOf(guestDataFirebase.getCheckout_time()));
                        intent.putExtra("purpose",String.valueOf(guestDataFirebase.getPurpose()));
                        intent.putExtra("veh_img",String.valueOf(guestDataFirebase.getVehicle_image_url()));
                        intent.putExtra("veh_type",String.valueOf(guestDataFirebase.getCar_type()));
                        intent.putExtra("veh_num",String.valueOf(guestDataFirebase.getVehicle_no()));
                        intent.putExtra("flat_nos",String.valueOf(guestDataFirebase.getFlat_no()));
                        intent.putExtra("phno",String.valueOf(guestDataFirebase.getPhone_no()));
                        intent.putExtra("profile_img",String.valueOf(guestDataFirebase.getProfile_image_url()));
                        startActivity(intent);
                    }
                });


            }

        };

    }

    private void getFlats(){
        final Query flat_Ref= db.collection(getString(R.string.society)).whereEqualTo("unique_id",unique_id);
        getDataListener=flat_Ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {


                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot.getDocuments().isEmpty()) {
                    Log.i(TAG,"no societies");

                } else {

                    if(snapshot.getDocuments().get(0).get("flats_unavailable")!=null ) {

                        categories = (ArrayList) ((ArrayList) snapshot.getDocuments().get(0).get("flats_unavailable"));
                        Collections.sort(categories);
                        flats = categories.toArray(new String[categories.size()]);
                        Log.i(TAG,"flats are "+flats);
                        mAdapter.notifyDataSetChanged();


                    }

                    Log.i(TAG, "categories is " + categories);

                    if(categories.isEmpty()){
                        categories.add("No flats available");
                    }



                }


            }
        });

    }

    private void filter(String text){
        List<GuestDataFirebase> temp = new ArrayList();
        for(GuestDataFirebase d: frequent_list){
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if(d.getName().toLowerCase().contains(text)){
                temp.add(d);
            }
        }
        //update recyclerview
        mAdapter.updateList(temp);

    }

    private void getData(){
        Log.i(TAG,"In get data");

        progressBar.setVisibility(View.VISIBLE);

        frequent_list.clear();
        getDataListener=docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable final DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source=snapshot!=null && snapshot.getMetadata().hasPendingWrites()
                        ?"Local" : "Server";

                if(snapshot!=null && snapshot.exists()){

                    Log.i(TAG,"Data is here "+snapshot.getData());

                    soc_id_ref=(DocumentReference)snapshot.get("society_id");
                    Log.i(TAG,"Society id is "+soc_id_ref.toString());

                    soc_id_ref.collection(getString(R.string.guestlist))
                            .whereEqualTo(getString(R.string.frequent_visitor),TRUE)
                            .orderBy("date_created", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                    progressBar.setVisibility(View.GONE);
                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());

                                    } else {


                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No frequent_visitors");
                                            no_freq_visitors.setVisibility(View.VISIBLE);
                                            showMessage("Sorry!","There are no frequent visitors",R.drawable.ic_error_dialog);
                                        } else {
                                            Log.i(TAG,"THERE ARE FREQ VISITORS");
                                            no_freq_visitors.setVisibility(View.GONE);
                                            frequent_list.clear();
                                            for (QueryDocumentSnapshot document : snapshots) {


                                                Timestamp date1 = (Timestamp) document.get("date_created");

//                                        SimpleDateFormat sfd_day = new SimpleDateFormat("dd");
//                                        String day = sfd_day.format(date1.toDate());
//
//                                        SimpleDateFormat sfd_mon = new SimpleDateFormat("MMM");
//                                        String mon = sfd_mon.format(date1.toDate());
//
//                                        SimpleDateFormat sfd_year = new SimpleDateFormat("yyyy");
//                                        String year = sfd_year.format(date1.toDate());

                                                SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("MMMM d, yyyy");
                                                String date_viewFormat = sfd_viewFormat.format(date1.toDate());

                                                SimpleDateFormat sfd_time = new SimpleDateFormat("hh:mm a");
                                                String time = sfd_time.format(date1.toDate());





                                                frequent_list.add(
                                                        new GuestDataFirebase(

                                                                document.getString("car_type"),
                                                                document.getBoolean("checkout"),
                                                                "",
                                                                date_viewFormat,
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



                                                        )
                                                );

                                                recyclerView.setAdapter(mAdapter);
                                                Log.i(TAG,"Success! data received");
                                                Log.i(TAG,"car type is "+frequent_list.get(0).getCar_type());


                                            }

                                        }

                                    }
                                }

                            });
                }else {
                    Log.i(TAG,"An error occured: "+ e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    showMessage("Error","An internal error occured: ",R.drawable.ic_error_dialog);
                }

            }
        });
    }

    public void showMessage(String title, String message,int image){

        final Dialog dialog = new Dialog(FrequentVisitorsActivity.this);
        dialog.setContentView(R.layout.dialog_new);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Log.i(TAG,"NEW DIALOG");

        Button btn_positive = dialog.findViewById(R.id.btn_positive);
        Button btn_negative = dialog.findViewById(R.id.btn_negative);
        TextView dialog_title = dialog.findViewById(R.id.dialog_title);
        TextView dialog_message = dialog.findViewById(R.id.dialog_message);
        ImageView dialog_icon = dialog.findViewById(R.id.dialog_img);

        dialog_title.setText(title);
        dialog_message.setText(message);
        btn_negative.setVisibility(View.GONE);
        btn_positive.setVisibility(View.GONE);

//        btn_positive.setText("OK");
//        btn_negative.setText("Go to Settings");
//        btn_positive.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        btn_negative.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(myIntent);
//            }
//        });
        dialog_icon.setImageResource(image);
        dialog.show();

    }

    @Override
    public void onStop() {
        super.onStop();

        if (getDataListener!= null) {
            getDataListener.remove();
            getDataListener = null;
        }

    }

}
