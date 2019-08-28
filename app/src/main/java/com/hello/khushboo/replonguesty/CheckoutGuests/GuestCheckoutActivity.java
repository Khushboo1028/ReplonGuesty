package com.hello.khushboo.replonguesty.CheckoutGuests;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.hello.khushboo.replonguesty.GuestDataFirebase;
import com.hello.khushboo.replonguesty.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class GuestCheckoutActivity extends AppCompatActivity {

    public static final String TAG = "GuestCheckoutActivity";
    ImageView back_checkout,filter;
    RecyclerView recyclerView;
    CheckoutAdapter adapter;
    String checkout_time = "--NA--";
    String document_id;
    DocumentReference soc_id_ref;
    TextView tv_empty_list;

    ListenerRegistration getDataListener;
    TextView start_date,end_date;
    DatePickerDialog pickerDialog;

    int dayStart=0, monthStart, yearStart, dayEnd, monthEnd, yearEnd;
    Calendar cldrStart;
    Date dateStart, dateEnd;
    Button apply_filters;
    EditText searchView;

    ProgressBar progressBar;


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

        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        filter=findViewById(R.id.filters);
        tv_empty_list = (TextView)findViewById(R.id.empty_list);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showFilterDialogue();
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

        progressBar.setVisibility(View.VISIBLE);

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestlist = getString(R.string.guestlist);
        Log.i(TAG,"Doc ref is "+docRef);

       getDataListener= docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                   getDataListener=soc_id_ref.collection(guestlist).orderBy("date_created", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                                    progressBar.setVisibility(View.GONE);
                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else {
                                        checkout_list.clear();
//                                      mAdapter.notifyDataSetChanged();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                            tv_empty_list.setVisibility(View.VISIBLE);
                                            showMessage("Sorry!","There are no guests checked in.",R.drawable.ic_error_dialog);
                                        } else {

                                            tv_empty_list.setVisibility(View.GONE);

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

                                                    Boolean checkout;
                                                    if(document.getBoolean("checkout")==null ||!document.getBoolean("checkout")){
                                                        checkout=FALSE;
                                                    }else{
                                                        checkout=TRUE;
                                                    }

                                                    checkout_list.add(new GuestDataFirebase(
                                                           document.getString("car_type"),
                                                           checkout,
                                                            out_date,
                                                            in_date,
                                                            document.getDocumentReference("document_id"),
                                                            document.get("flat_no").toString(),
                                                            document.getBoolean("frequent_visitor"),
                                                            document.getString("name"),
                                                            document.getString("phone_number"),
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

                                                    Boolean checkout;
                                                    if(document.getBoolean("checkout")==null ||!document.getBoolean("checkout")){
                                                        checkout=FALSE;
                                                    }else{
                                                        checkout=TRUE;
                                                    }
                                                    checkout_list.add(new GuestDataFirebase(
                                                            document.getString("car_type"),
                                                            checkout,
                                                            checkout_time,
                                                            in_date,
                                                            document.getDocumentReference("document_id"),
                                                            document.get("flat_no").toString(),
                                                            document.getBoolean("frequent_visitor"),
                                                            document.getString("name"),
                                                            document.getString("phone_number"),
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
                }else {
                    Log.i(TAG,"An error occurred: "+ e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    //showMessage("Error","An internal error occured: ",R.drawable.ic_error_dialog);
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
                    Log.i(TAG,"Checkout is "+guestDataFirebase.getCheckout());
                    checkoutHolder.btn_checkout.setVisibility(View.GONE);
                    checkoutHolder.dateAndTime_out.setText(String.valueOf(guestDataFirebase.getCheckout_time()));
                }else{
                    checkoutHolder.btn_checkout.setVisibility(View.VISIBLE);
                }

                checkoutHolder.btn_checkout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG,"Button pressed");
                        checkout(guestDataFirebase.getDocument_id().getId());
                    }
                });

                checkoutHolder.row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GuestCheckoutActivity.this,GuestCheckoutViewActivity.class);
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


    public void showMessage(String title, String message,int image){

        final Dialog dialog = new Dialog(GuestCheckoutActivity.this);
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

    private void showFilterDialogue() {

        final Boolean[] boolean_checkout = {FALSE};
        final Boolean[] boolean_checkin = {FALSE};
        final Boolean[] boolean_all = {TRUE};
        final Boolean[] boolean_date = {FALSE};
        final int[] dayStart = {0};
        final int[] monthStart = new int[1];
        final int[] yearStart = new int[1];
        final int[] dayEnd = new int[1];
        final int[] monthEnd = new int[1];
        final int[] yearEnd = new int[1];

        LayoutInflater factory = LayoutInflater.from(this);
        final View filterDialogView = factory.inflate(R.layout.checkout_filters, null);
        final AlertDialog filterDialog = new AlertDialog.Builder(this).create();
        filterDialog.setView(filterDialogView);

        final Button btn_checkout = (Button) filterDialogView.findViewById(R.id.btn_checkout);
        final Button btn_checkin = (Button) filterDialogView.findViewById(R.id.btn_checkin);
        final Button btn_all = (Button) filterDialogView.findViewById(R.id.btn_all);
        final Button btn_apply_filter = (Button) filterDialogView.findViewById(R.id.apply_filters);
        final TextView start_date = (TextView) filterDialogView.findViewById(R.id.start_date);
        final TextView end_date = (TextView) filterDialogView.findViewById(R.id.end_date);



        btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( boolean_checkin[0]){
                    boolean_checkin[0] =FALSE;
                    btn_checkin.setTextColor(Color.parseColor("#e55039"));
                    btn_checkin.setBackgroundColor(Color.parseColor("#1A707070"));
                }
                if( boolean_all[0]){
                    boolean_all[0] =FALSE;
                    btn_all.setTextColor(Color.parseColor("#000000"));
                    btn_all.setBackgroundColor(Color.parseColor("#1A707070"));
                }
                boolean_checkout[0] =TRUE;
                Log.i(TAG,"Button pressed");
                btn_checkout.setBackgroundColor(Color.parseColor("#38ada9"));
                btn_checkout.setTextColor(Color.parseColor("#FFFFFF"));

            }
        });

        btn_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( boolean_checkout[0]){
                    boolean_checkout[0] =FALSE;
                    btn_checkout.setTextColor(Color.parseColor("#38ada9"));
                    btn_checkout.setBackgroundColor(Color.parseColor("#1A707070"));
                }

                if( boolean_all[0]){
                    boolean_all[0] =FALSE;
                    btn_all.setTextColor(Color.parseColor("#000000"));
                    btn_all.setBackgroundColor(Color.parseColor("#1A707070"));
                }
                boolean_checkin[0] =TRUE;
                Log.i(TAG,"Button pressed");
                btn_checkin.setBackgroundColor(Color.parseColor("#e55039"));
                btn_checkin.setTextColor(Color.parseColor("#FFFFFF"));

            }
        });

        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( boolean_checkout[0]){
                    boolean_checkout[0] =FALSE;
                    btn_checkout.setTextColor(Color.parseColor("#38ada9"));
                    btn_checkout.setBackgroundColor(Color.parseColor("#1A707070"));
                }

                if( boolean_checkin[0]){
                    boolean_checkin[0] =FALSE;
                    btn_checkin.setTextColor(Color.parseColor("#e55039"));
                    btn_checkin.setBackgroundColor(Color.parseColor("#1A707070"));
                }

                boolean_all[0]=TRUE;
                Log.i(TAG,"Button pressed");
                btn_all.setBackgroundColor(Color.parseColor("#000000"));
                btn_all.setTextColor(Color.parseColor("#FFFFFF"));

            }
        });


        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cldrStart = Calendar.getInstance();
                dayStart[0] = cldrStart.get(Calendar.DAY_OF_MONTH);
                monthStart[0] = cldrStart.get(Calendar.MONTH);
                yearStart[0] = cldrStart.get(Calendar.YEAR);
                pickerDialog = new DatePickerDialog(GuestCheckoutActivity.this,R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                                try {
                                    //All your parse Operations
                                    dateStart=sdf.parse(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    dateEnd=sdf.parse(dayOfMonth+1 + "/" + (monthOfYear + 1) + "/" + year);
                                } catch (ParseException e) {
                                    //Handle exception here, most of the time you will just log it.
                                    e.printStackTrace();
                                }

                                start_date.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                end_date.setText("Set End Date");
                            }
                        }, yearStart[0], monthStart[0], dayStart[0]);

                pickerDialog.show();


            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dayStart[0] == 0) {
                    showMessage("Error!", "Please Enter Start Date First",R.drawable.ic_error_dialog);

                }
                else {

                    dayEnd[0] = dayStart[0] + 1;
                    monthEnd[0] = monthStart[0];
                    yearEnd[0] = yearStart[0];

                    pickerDialog = new DatePickerDialog(GuestCheckoutActivity.this, R.style.DialogTheme,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                                    try {
                                        //All your parse Operations
                                        dateEnd=sdf.parse(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    } catch (ParseException e) {
                                        //Handle exception here, most of the time you will just log it.
                                        e.printStackTrace();
                                    }

                                    if(dateEnd.before(dateStart)){
                                        showMessage("Error","Please Enter the Date after Start Date",R.drawable.ic_error_dialog);
                                    }
                                    else {
                                        end_date.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    }
                                }
                            }, yearEnd[0], monthEnd[0], dayEnd[0]);
                    pickerDialog.show();

                }
                boolean_date[0] =TRUE;
            }
        });


        btn_apply_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, boolean_date[0].toString());

                if (boolean_checkin[0] && boolean_date[0]) {
                    Log.i(TAG, "In 1");
                    getDataWithDateAndCheck(TRUE);
                } else if (boolean_checkout[0] && boolean_date[0]) {
                    Log.i(TAG, "In 2");
                    getDataWithDateAndCheck(FALSE);

                } else if (boolean_all[0] && boolean_date[0]) {
                    Log.i(TAG, "In 3");
                    getDataWithDates();

                } else if (boolean_checkin[0]) {
                    Log.i(TAG, "In 4");
                    getDataWithCheckout(FALSE);

                } else if (boolean_checkout[0]) {
                    Log.i(TAG, "In 5");
                    getDataWithCheckout(TRUE);

                } else if (boolean_all[0]) {
                    Log.i(TAG, "In 6");
                    getData();
                }
                callAdapter();
                recyclerView.setAdapter(adapter);
                filterDialog.dismiss();

            }
        });


        filterDialog.show();

    }

    private void getDataWithCheckout(final Boolean aTrue) {

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestData = getString(R.string.guestlist);

        getDataListener=docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if(snapshot!=null && snapshot.exists()) {
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());
                    soc_id_ref = (DocumentReference) snapshot.get("society_id");


                    getDataListener=soc_id_ref.collection(guestData).orderBy("date_created")
                            .whereEqualTo("checkout",aTrue)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {


                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else{

                                        checkout_list.clear();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                        }
                                        else{

                                            for (QueryDocumentSnapshot document : snapshots) {

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

                                            recyclerView.setAdapter(adapter);

                                        }
                                    }


                                }
                            });
                }
            }
        });


    }

    private void getDataWithDateAndCheck(final Boolean aTrue) {

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestData = getString(R.string.guestlist);

        getDataListener=docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if(snapshot!=null && snapshot.exists()) {
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());
                    soc_id_ref = (DocumentReference) snapshot.get("society_id");


                    getDataListener=soc_id_ref.collection(guestData).orderBy("date_created")
                            .whereEqualTo("checkout",aTrue)
                            .startAt(new Timestamp(dateStart))
                            .endAt(new Timestamp(dateEnd))
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {


                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else{

                                        checkout_list.clear();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                        }
                                        else{

                                            for (QueryDocumentSnapshot document : snapshots) {

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

                                            recyclerView.setAdapter(adapter);

                                        }
                                    }


                                }
                            });
                }
            }
        });

    }


    private void getDataWithDates(){

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestData = getString(R.string.guestlist);




       getDataListener= docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if(snapshot!=null && snapshot.exists()) {
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());
                    soc_id_ref = (DocumentReference) snapshot.get("society_id");


                    getDataListener=soc_id_ref.collection(guestData).orderBy("date_created")
                            .startAt(new Timestamp(dateStart)).endAt(new Timestamp(dateEnd))
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {


                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else{

                                        checkout_list.clear();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                        }
                                        else{

                                            for (QueryDocumentSnapshot document : snapshots) {

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

                                            recyclerView.setAdapter(adapter);

                                        }
                                    }


                                }
                            });
                }
            }
        });
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
