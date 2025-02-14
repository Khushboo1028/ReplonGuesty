package com.hello.khushboo.replonguesty.AddGuests;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hello.khushboo.replonguesty.DefaultTextConfig;
import com.hello.khushboo.replonguesty.MultiSpinner;
import com.hello.khushboo.replonguesty.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class AddGuestActivity extends AppCompatActivity implements MultiSpinner.OnMultipleItemsSelectedListener {

    public static final String TAG = "AddGuestActivity";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 78;
    public static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 43;
    ImageView back_add_guest;
    Button btn_add_vehicle, btn_add_guest, btn_get_otp;

    MultiSpinner multiSpinner;
    Spinner spinner;

    private static final int REQUEST_CAMERA = 100;
    private static final int RESULT_LOAD_IMAGE = 69;

    ListenerRegistration listenerRegistration;
    EditText et_name,et_phoneNumber,veh_num,put_otp;
    String purpose,phone;
    TextView veh_type,verify_otp;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    ArrayList<ContentAddGuest> guestArrayList;
    List<String> flat_list;
    PhoneAuthCredential cred;

    ProgressBar mProgressBar;
    private int mRequestCode = 101;

    String vehicle_number="--NA--";
    String car_type= "--NA--";
    String vehicle_image_URL ="";

    CheckBox checkBox;
    Boolean bool_frequent_visitor;
    String selectedImageUriString;
    Uri selectedImageURI,downloadUri,downloadUri2,selectedImageURIProfile,file_camera_uri;
    StorageTask uploadTask,uploadTask2;

    StorageReference storageReference;
    FirebaseStorage storage;

    CircleImageView guest_image;
    Bitmap bitmap_photo;
    String profile_image_URL;

    Boolean FLAG1,FLAG2,FLAG3;
    FirebaseFirestore db;
    String unique_id,flat_doc_id;
    List categories;
    FirebaseUser firebaseUser;
    boolean isConnected,monitoringConnectivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DefaultTextConfig defaultTextConfig = new DefaultTextConfig();
        defaultTextConfig.adjustFontScale(getResources().getConfiguration(), AddGuestActivity.this);
        setContentView(R.layout.activity_add_guest);

//        String[] array = {"A - 101", "A - 102","A - 103","A - 104","A - 201","A - 202","A - 203","A - 204","A - 301","A - 302","A - 303","A - 304","A - 401","A - 402","A - 403","A - 404"};
        multiSpinner =  (MultiSpinner) findViewById(R.id.mySpinner);
        spinner = findViewById(R.id.spinner1);

        final List<String> purposes = new ArrayList<String>();
        purposes.add("Business");
        purposes.add("Personal");
        purposes.add("Delivery");
        purposes.add("Others");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, purposes);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                purpose = String.valueOf(spinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        unique_id=getIntent().getStringExtra("unique_id");
        final Query flat_Ref= db.collection(getString(R.string.society)).whereEqualTo("unique_id",unique_id);

        categories = new ArrayList<String>();
        listenerRegistration=flat_Ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                        flat_doc_id=snapshot.getDocuments().get(0).getId();
                        multiSpinner.setItems(categories);
                        multiSpinner.setSelection(new int[]{});
                        multiSpinner.setListener(AddGuestActivity.this);


                    }

                    Log.i(TAG, "categories is " + categories);

                    if(categories.isEmpty()){
                        categories.add("No flats available");
                    }



                }


            }
        });

        back_add_guest = (ImageView) findViewById(R.id.back_add_guest);
        btn_add_vehicle = (Button) findViewById(R.id.add_veh);
        checkBox = findViewById(R.id.check_box_freq);
        guest_image=(CircleImageView)findViewById(R.id.guest_image);

        et_name=(EditText)findViewById(R.id.name);
        et_phoneNumber=(EditText)findViewById(R.id.phoneNumber);
//        put_otp=(EditText) findViewById(R.id.put_otp);
//        verify_otp = findViewById(R.id.verify_otp);
//        put_otp.setVisibility(View.GONE);
//        verify_otp.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();


        //for camera intent
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Toast.makeText(getApplicationContext(), "Verification Complete", Toast.LENGTH_SHORT).show();

                showMessage("Success!!","OTP verified!" + credential,R.drawable.ic_success_dialog);
                cred = credential;
                //btn_add_guest.setEnabled(true);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(), "Verification Failed", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"Error is "+e.getMessage());
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Toast.makeText(getApplicationContext(), "Code Sent", Toast.LENGTH_SHORT).show();
                mVerificationId = verificationId;
                mResendToken = token;

                Log.i(TAG,"VERIFICATION ID IS"+mVerificationId);
                Log.i(TAG,"RESEND TOKEN"+mResendToken);

            }
        };


        mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        veh_num = (EditText) findViewById(R.id.veh_num);
        veh_type = (TextView) findViewById(R.id.veh_type);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainrel);
        relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                InputMethodManager imm =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        back_add_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_add_guest =(Button)findViewById(R.id.add_guest);

        guest_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShowPhoto();
            }
        });



        btn_add_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phone = et_phoneNumber.getText().toString();
                Log.i(TAG,"ADD GUEST PRESSED");




                if (et_name.getText().toString().isEmpty() || multiSpinner.getSelectedIndices().isEmpty() || phone.isEmpty()){
                    showMessage("Error!","Please enter all details",R.drawable.ic_error_dialog);
                }
                else if(phone.length()!=10){
                    showMessage("Error!","Please enter a correct Phone Number",R.drawable.ic_error_dialog);

                }
                else if(phone.charAt(0)!='9' && phone.charAt(0)!='8' && phone.charAt(0)!='7'){

                    showMessage("Error!","Please enter a correct Phone Number",R.drawable.ic_error_dialog);

                }


                else{


                    if(!isConnected){
                        final Dialog dialog = new Dialog(AddGuestActivity.this);
                        dialog.setContentView(R.layout.dialog_new);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        Log.i(TAG,"NEW DIALOG");

                        Button btn_positive = dialog.findViewById(R.id.btn_positive);
                        Button btn_negative = dialog.findViewById(R.id.btn_negative);
                        TextView dialog_title = dialog.findViewById(R.id.dialog_title);
                        TextView dialog_message = dialog.findViewById(R.id.dialog_message);
                        ImageView dialog_icon = dialog.findViewById(R.id.dialog_img);

                        dialog_title.setText("Internet Unavailable");
                        dialog_message.setText("Poor network connectivity detected! Please check your internet connection");
                        //        btn_negative.setVisibility(View.GONE);
                        //        btn_positive.setVisibility(View.GONE);

                        btn_positive.setText("OK");
                        btn_negative.setText("Go to Settings");
                        btn_positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        btn_negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(myIntent);
                            }
                        });
                        dialog_icon.setImageResource(R.drawable.ic_no_internet);
                        dialog.show();
                    }else {


                        if (checkBox.isChecked()) {
                            bool_frequent_visitor = TRUE;
                            Log.i(TAG, "THIS IS FREQUENT USER");

                            if (selectedImageUriString != null && selectedImageURIProfile == null) {
                                Log.i(TAG, "in 1");
                                selectedImageURI = Uri.parse(selectedImageUriString);
                                uploadImage();
                                Log.i(TAG, "in 2");

                            } else if (selectedImageUriString == null && selectedImageURIProfile == null) {
                                addData();
                                Log.i(TAG, "in 3");
                            } else if (selectedImageURIProfile != null && selectedImageUriString == null) {
                                Log.i(TAG, "in 4");
                                uploadProfileImage();
                            } else if (selectedImageUriString != null && selectedImageURIProfile != null) {
                                Log.i(TAG, "in 5");
                                selectedImageURI = Uri.parse(selectedImageUriString);
                                uploadProfileandVehicleImage();
                            }


                        } else {
                            Log.i(TAG, "THIS IS NOT A FREQUENT USER");
                            bool_frequent_visitor = FALSE;

                            if (selectedImageUriString != null && selectedImageURIProfile == null) {
                                Log.i(TAG, "in 1");
                                selectedImageURI = Uri.parse(selectedImageUriString);
                                uploadImage();
                                Log.i(TAG, "in 2");

                            } else if (selectedImageUriString == null && selectedImageURIProfile == null) {
                                addData();
                                Log.i(TAG, "in 3");
                            } else if (selectedImageURIProfile != null && selectedImageUriString == null) {
                                Log.i(TAG, "in 4");
                                uploadProfileImage();
                            } else if (selectedImageUriString != null && selectedImageURIProfile != null) {
                                selectedImageURI = Uri.parse(selectedImageUriString);
                                Log.i(TAG, "in 5");
                                uploadProfileandVehicleImage();
                            }
                        }
                    }

                }


//
            }
        });


        btn_add_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AddVehicleActivity.class);
                startActivityForResult(intent,mRequestCode);

            }
        });



    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        Log.i(TAG,"RESEND TOKEN IN METHOD IS"+mResendToken);
        //Log.i(TAG,"NEW CREDENTIAL : "+cred.getSmsCode());

        if(code.equals(mResendToken)&&verificationId.equals(mVerificationId)){
            Toast.makeText(AddGuestActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
//            mAuth.signOut();
//            mAuth.signInWithEmailAndPassword("khushboo1028@gmail.com","hello12345");
            btn_add_guest.setEnabled(true);
        }
        else
            Toast.makeText(this,"Please provide correct OTP",Toast.LENGTH_SHORT).show();
    }

    public void dialogShowPhoto() {
        String takePhoto = "Take Photo";
        String chooseFromLibrary = "Choose from Gallery";
        final CharSequence[] items = {takePhoto};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        final String finalTakephoto = takePhoto;
        final String finalChooseFromLibrary = chooseFromLibrary;

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
//                if (items[item].equals(finalChooseFromLibrary)) {
//                    Intent intent = new Intent(
//                            Intent.ACTION_PICK,
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
//                }
//                else
                if (items[item].equals(finalTakephoto)) {

                    int PERMISSION_ALL = 1;
                    String[] PERMISSIONS = {
                            android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.WRITE_CONTACTS,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_SMS,
                            android.Manifest.permission.CAMERA
                    };

                    if(!hasPermissions(getApplicationContext(), PERMISSIONS)){
                        ActivityCompat.requestPermissions(AddGuestActivity.this, PERMISSIONS, PERMISSION_ALL);
                    }

                    if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {


                        if (ActivityCompat.shouldShowRequestPermissionRationale(AddGuestActivity.this,
                                Manifest.permission.CAMERA) == true){


                            final Dialog dialog1 = new Dialog(AddGuestActivity.this);
                            dialog1.setContentView(R.layout.dialog_new);
                            dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            Log.i(TAG,"NEW DIALOG");

                            Button btn_positive = dialog1.findViewById(R.id.btn_positive);
                            Button btn_negative = dialog1.findViewById(R.id.btn_negative);
                            TextView dialog_title = dialog1.findViewById(R.id.dialog_title);
                            TextView dialog_message = dialog1.findViewById(R.id.dialog_message);
                            ImageView dialog_icon = dialog1.findViewById(R.id.dialog_img);

                            dialog_title.setText("Permission Denied");
                            dialog_message.setText("You might have denied the permission for using the Camera App. Please go to phone settings and enable the permission for Home.");
                            //        btn_negative.setVisibility(View.GONE);
                            //        btn_positive.setVisibility(View.GONE);

                            btn_positive.setText("OK");
                            btn_negative.setText("Go to Settings");
                            btn_positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog1.dismiss();
                                }
                            });
                            btn_negative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                    Intent myIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                    startActivity(myIntent);

                                    Intent i = new Intent();
                                    i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                    i.setData(Uri.parse("package:" + getPackageName()));
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(i);
                                }
                            });
                            dialog_icon.setImageResource(R.drawable.ic_error_dialog);
                            dialog1.show();

                        }else {
                            ActivityCompat.requestPermissions(AddGuestActivity.this, new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        }


                    }else{
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        file_camera_uri =  getOutputMediaFileUri(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, file_camera_uri);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }

                }
            }
        });
        builder.show();


    }
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "ReplonHome");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                        FLAG1=TRUE;
            }

            else{
                guest_image.setImageURI(file_camera_uri);
                selectedImageURIProfile=file_camera_uri;

            }

                // uploadImage();



        }


        if(requestCode == mRequestCode && resultCode == RESULT_OK){

//            if(bitmap_photo!=null){
//                guest_image
//            }
            vehicle_number= data.getStringExtra("vehicle_number");
            car_type= data.getStringExtra("car_type");
            selectedImageUriString=data.getStringExtra("selectedImageUri");
            Log.i(TAG,"selected image uri si "+selectedImageUriString);

            veh_num.setText(vehicle_number);
            veh_type.setText(car_type);
            btn_add_vehicle.setVisibility(View.GONE);

        }


//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK  && data != null && data.getData() != null) {
//            selectedImageURI = data.getData();
//
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageURIProfile);
//                // bitmap_photo = Bitmap.createScaledBitmap(bitmap,50,67,false);
//                guest_image.setImageBitmap(bitmap);
//
//                Log.i(TAG,"Image set");
//                //uploadImage();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//        }
    }





    @Override
    public void selectedIndices(List<Integer> indices) {

        Log.i(TAG,"The selected indices are "+indices);

        if(indices.isEmpty()){
            showMessage("Error!","Please select a flat",R.drawable.ic_error_dialog);

        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void selectedStrings(List<String> flats) {
        Log.i(TAG, "Selected flats are " + flats);
        flat_list=flats;

    }


    public void showMessage(String title, String message,int image){

        final Dialog dialog = new Dialog(AddGuestActivity.this);
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


    private void addData() {



        et_name.setEnabled(false);
        et_phoneNumber.setEnabled(false);
        multiSpinner.setEnabled(false);
        veh_num.setEnabled(false);
        spinner.setEnabled(false);
        checkBox.setEnabled(false);


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String guestlist = getString(R.string.guestlist);
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
        final String vehicle_image_url = getString(R.string.vehicle_image_url);
        final String frequent_visitor = getString(R.string.frequent_visitor);
        final String user_id = currentFirebaseUser.getUid();


        guestArrayList = new ArrayList<ContentAddGuest>();



        DocumentReference docRef = db.collection(user).document(user_id);
        mProgressBar.setVisibility(View.VISIBLE);

        Log.i(TAG,"DOCUMENT REFERENCE IS "+docRef.toString());

        listenerRegistration=docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                Log.i(TAG,"snapshot exits?  "+snapshot.exists());

                if (snapshot != null &&snapshot.exists()) {

                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());


                    final DocumentReference soc_id_ref = (DocumentReference) snapshot.get("society_id");


                    Log.i(TAG, "Society ref is " + soc_id_ref);

                    String name = et_name.getText().toString();
                    phone = et_phoneNumber.getText().toString();
//                    Long phoneNumber;
//
//                    if(!phone.equals("")){
//                        phoneNumber = Long.parseLong(et_phoneNumber.getText().toString());}
//                    else {
//                        phoneNumber = Long.parseLong("0");
//                    }


//                    String dateInString = new java.text.SimpleDateFormat("EEEE, dd/MM/yyyy/hh:mm:ss")
//                            .format(cal.getTime())
//                    SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd/MM/yyyy/hh:mm:ss");
//                    Date parsedDate = formatter.parse(dateInString);



//                    vehicle_image_URL= getIntent().getStringExtra("vehicle_image_URL");


//                    if(selectedImageUriString!=null){
//                        selectedImageURI= Uri.parse(selectedImageUriString);
//                        uploadImage();
//                    }

                    Log.i(TAG,"IMG URL IS "+ vehicle_image_url);
                    if(vehicle_image_URL ==null){
                        vehicle_image_URL ="";
                    }

                    if(profile_image_URL==null){
                        profile_image_URL="";
                    }
                    final Map<String, Object> data = new HashMap<>();
                    data.put(date_created, new Timestamp(new Date()));
                    data.put(guest_name, name);
                    data.put(guest_phone_number, phone);
                    data.put(guest_purpose, purpose);
                    data.put(guest_flat_no, flat_list);
                    data.put(guest_user_id, user_id);
                    data.put(vehicle_number_fb, veh_num.getText().toString());
                    data.put(car_type_fb, car_type);
                    data.put(vehicle_image_url, vehicle_image_URL);
                    data.put("checkout", FALSE);
                    data.put("checkout_time", null);
                    data.put(frequent_visitor,bool_frequent_visitor);
                    data.put("profile_image_url",profile_image_URL);



                    DocumentReference doc_id=soc_id_ref.collection(guestlist).document();
                    data.put(document_id,doc_id);


                    doc_id.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void avoid) {
                                    mProgressBar.setVisibility(View.GONE);
                                    showMessage("Success", "Guest checked in successfully",R.drawable.ic_success_dialog);

                                    et_name.setEnabled(true);
                                    spinner.setEnabled(true);
                                    et_phoneNumber.setEnabled(true);
                                    multiSpinner.setEnabled(true);
                                    checkBox.setEnabled(true);

                                    et_name.setText("");
                                    et_phoneNumber.setText("");
                                    veh_num.setText("");
                                    veh_type.setText("");
                                    selectedImageUriString=null;
                                    selectedImageURI=null;
                                    vehicle_image_URL ="";
                                    profile_image_URL="";
                                    checkBox.setChecked(FALSE);
                                    guest_image.setImageResource(R.drawable.ic_default_guest);
                                    btn_add_vehicle.setVisibility(View.VISIBLE);
                                    multiSpinner.setSelection(new int[]{});

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document"+ e.getMessage());
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    showMessage("Error", "Unable to Add guest",R.drawable.ic_error_dialog);
                                }
                            });

                } else {
                    Log.d(TAG, source + " data: null");
                    mProgressBar.setVisibility(View.GONE);
                    showMessage("Error", "An internal error occurred",R.drawable.ic_error_dialog);
                }
            }
        });
    }


    private void uploadImage(){

        et_name.setEnabled(false);
        et_phoneNumber.setEnabled(false);
        multiSpinner.setEnabled(false);
        veh_num.setEnabled(false);
        spinner.setEnabled(false);
        checkBox.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        final StorageReference ref = storageReference.child("guest_vehicle_images/"+ UUID.randomUUID().toString());

        try {

            Bitmap bitmap_photo = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageURI);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap_photo.compress(Bitmap.CompressFormat.JPEG,30,stream);
            byte[] byteArray = stream.toByteArray();
            bitmap_photo.recycle();

            uploadTask=ref.putBytes(byteArray);

        } catch (IOException e) {
            e.printStackTrace();
        }



        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    vehicle_image_URL =downloadUri.toString();
                    Log.i(TAG,"The URL for this image is "+downloadUri);
                    Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();

                    addData();


                } else {
                    // Handle failures
                    // ...
                    Log.i(TAG,"error is "+task.getException());
                }
            }
        });
    }

    private void uploadProfileImage(){

        et_name.setEnabled(false);
        et_phoneNumber.setEnabled(false);
        multiSpinner.setEnabled(false);
        veh_num.setEnabled(false);
        spinner.setEnabled(false);
        checkBox.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        final StorageReference ref = storageReference.child("guest_profile_images/"+ UUID.randomUUID().toString());
        uploadTask = ref.putFile(selectedImageURIProfile);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    profile_image_URL =downloadUri.toString();
                    Log.i(TAG,"The URL for this image is "+downloadUri);
                    Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();

                    addData();


                } else {
                    // Handle failures
                    // ...
                    Log.i(TAG,"error is "+task.getException());
                }
            }
        });
    }

        private void uploadProfileandVehicleImage(){

        et_name.setEnabled(false);
        et_phoneNumber.setEnabled(false);
        multiSpinner.setEnabled(false);
        veh_num.setEnabled(false);
        spinner.setEnabled(false);
        checkBox.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        final StorageReference ref = storageReference.child("guest_vehicle_images/"+ UUID.randomUUID().toString());
        Log.i(TAG,"uri is "+selectedImageURI);
        uploadTask = ref.putFile(selectedImageURI);


        final StorageReference ref2 = storageReference.child("guest_profile_images/"+ UUID.randomUUID().toString());
        uploadTask2 = ref2.putFile(selectedImageURIProfile);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    vehicle_image_URL =downloadUri.toString();
                    Log.i(TAG,"The URL for this image is "+downloadUri);
                    Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();

                    uploadTask2.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref2.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task2) {
                            if (task2.isSuccessful()) {
                                downloadUri2 = task2.getResult();
                                profile_image_URL =downloadUri2.toString();
                                Log.i(TAG,"The Profile URL for this image is "+downloadUri2);
                                // Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();

                                addData();


                            } else {
                                // Handle failures
                                // ...
                                Log.i(TAG,"error is "+task2.getException());
                            }
                        }
                    });


                } else {
                    // Handle failures
                    // ...
                    Log.i(TAG,"error is "+task.getException());
                }
            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();

        if (listenerRegistration!= null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }

    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            Log.i(TAG, "INTERNET CONNECTED");
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Log.i(TAG,"Internet lost");
            final Dialog dialog = new Dialog(AddGuestActivity.this);
            dialog.setContentView(R.layout.dialog_new);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Log.i(TAG,"NEW DIALOG");

            Button btn_positive = dialog.findViewById(R.id.btn_positive);
            Button btn_negative = dialog.findViewById(R.id.btn_negative);
            TextView dialog_title = dialog.findViewById(R.id.dialog_title);
            TextView dialog_message = dialog.findViewById(R.id.dialog_message);
            ImageView dialog_icon = dialog.findViewById(R.id.dialog_img);

            dialog_title.setText("Internet Unavailable");
            dialog_message.setText("Poor network connectivity detected! Please check your internet connection");
            //        btn_negative.setVisibility(View.GONE);
            //        btn_positive.setVisibility(View.GONE);

            btn_positive.setText("OK");
            btn_negative.setText("Go to Settings");
            btn_positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            btn_negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog_icon.setImageResource(R.drawable.ic_no_internet);
            dialog.show();
        }
    };

    // Method to check network connectivity in Main Activity
    private void checkConnectivity() {
        // here we are getting the connectivity service from connectivity manager
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        // Getting network Info
        // give Network Access Permission in Manifest
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // isConnected is a boolean variable
        // here we check if network is connected or is getting connected
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            // SHOW ANY ACTION YOU WANT TO SHOW
            // WHEN WE ARE NOT CONNECTED TO INTERNET/NETWORK
            Log.i(TAG, " NO NETWORK!");
            // if Network is not connected we will register a network callback to  monitor network
            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(), connectivityCallback);
            monitoringConnectivity = true;

            final Dialog dialog = new Dialog(AddGuestActivity.this);
            dialog.setContentView(R.layout.dialog_new);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Log.i(TAG,"NEW DIALOG");

            Button btn_positive = dialog.findViewById(R.id.btn_positive);
            Button btn_negative = dialog.findViewById(R.id.btn_negative);
            TextView dialog_title = dialog.findViewById(R.id.dialog_title);
            TextView dialog_message = dialog.findViewById(R.id.dialog_message);
            ImageView dialog_icon = dialog.findViewById(R.id.dialog_img);

            dialog_title.setText("Internet Unavailable");
            dialog_message.setText("Poor network connectivity detected! Please check your internet connection");
            //        btn_negative.setVisibility(View.GONE);
            //        btn_positive.setVisibility(View.GONE);

            btn_positive.setText("OK");
            btn_negative.setText("Go to Settings");
            btn_positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            btn_negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog_icon.setImageResource(R.drawable.ic_no_internet);
            dialog.show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectivity();

    }

    @Override
    protected void onPause() {
        // if network is being moniterd then we will unregister the network callback
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
        super.onPause();
    }


}
