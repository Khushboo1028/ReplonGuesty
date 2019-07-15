package com.hello.khushboo.replonguesty.AddGuests;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hello.khushboo.replonguesty.MultiSpinner;
import com.hello.khushboo.replonguesty.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hello.khushboo.replonguesty.AddGuests.AddVehicleActivity.calculateInSampleSize;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class AddGuestActivity extends AppCompatActivity implements MultiSpinner.OnMultipleItemsSelectedListener {

    public static final String TAG = "AddGuestActivity";
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 78;
    ImageView back_add_guest;
    Button btn_add_vehicle, btn_add_guest, btn_get_otp;
    MultiSpinner multiSpinner;
    Spinner spinner;

    private static final int REQUEST_CAMERA = 100;
    private static final int RESULT_LOAD_IMAGE = 69;

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
    Uri selectedImageURI,downloadUri,selectedImageURIProfile;
    StorageTask uploadTask,uploadTask2;

    StorageReference storageReference;
    FirebaseStorage storage;

    CircleImageView guest_image;
    Bitmap bitmap_photo;
    String profile_image_URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest);

        String[] array = {"A - 101", "A - 102","A - 103","A - 104","A - 201","A - 202","A - 203","A - 204","A - 301","A - 302","A - 303","A - 304","A - 401","A - 402","A - 403","A - 404"};
        multiSpinner =  (MultiSpinner) findViewById(R.id.mySpinner);
        multiSpinner.setItems(array);
        multiSpinner.setSelection(new int[]{});
        multiSpinner.setListener(this);
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

                Log.i(TAG,"VERFICATION ID IS"+mVerificationId);
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
//        btn_get_otp = (Button) findViewById(R.id.btn_get_otp);

//        btn_get_otp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                put_otp.setVisibility(View.VISIBLE);
//                verify_otp.setVisibility(View.VISIBLE);
//
//                PhoneAuthProvider.getInstance().verifyPhoneNumber("+91 "+et_phoneNumber.getText().toString(),120, TimeUnit.SECONDS,AddGuestActivity.this,mCallbacks);
//
//
//            }
//        });

//        verify_otp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                btn_add_guest.setEnabled(false);
//                if(put_otp.getText().toString().equals("")){
//                    showMessage("Error!","Enter The OTP Provided",R.drawable.ic_error_dialog);
//                }
//                else{
//
//                    verifyPhoneNumberWithCode(mVerificationId,put_otp.getText().toString());
//                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, put_otp.getText().toString());
//                    Log.i(TAG,credential.getProvider());
//
////                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, put_otp.getText().toString());
////                    mAuth.signInWithCredential(credential).addOnCompleteListener(AddGuestActivity.this, new OnCompleteListener<AuthResult>() {
////                        @Override
////                        public void onComplete(@NonNull Task<AuthResult> task) {
////                            if (task.isSuccessful()) {
////                                Toast.makeText(AddGuestActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
////                                mAuth.signOut();
////                                mAuth.signInWithEmailAndPassword("khushboo1028@gmail.com","hello12345");
////                                btn_add_guest.setEnabled(true);
////                            } else {
////                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
////                                    Toast.makeText(AddGuestActivity.this, "Verification Failed, Invalid credentials", Toast.LENGTH_SHORT).show();
////                                }
////                            }
////                        }
////                    });
//
//                }
//            }
//        });


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
                    Log.i(TAG,"DATA MALE CHE");

                    if(checkBox.isChecked()){
                        bool_frequent_visitor=TRUE;
                        Log.i(TAG,"THIS IS FREQUENT USER");

                        if(selectedImageUriString!=null && selectedImageURIProfile==null){
                            Log.i(TAG,"in 1");
                            selectedImageURI= Uri.parse(selectedImageUriString);
                            uploadImage();
                            Log.i(TAG,"in 2");

                        }else if(selectedImageUriString==null && selectedImageURIProfile==null){
                            addData();
                            Log.i(TAG,"in 3");
                        }else if(selectedImageURIProfile!=null && selectedImageUriString==null){
                            Log.i(TAG,"in 4");
                            uploadProfileImage();
                        }else if(selectedImageUriString!=null && selectedImageURIProfile!=null){
                            Log.i(TAG,"in 5");
                            selectedImageURI= Uri.parse(selectedImageUriString);
                           uploadProfileandVehicleImage();
                        }




                    }else{
                        Log.i(TAG,"THIS IS NOT A FREQUENT USER");
                        bool_frequent_visitor=FALSE;


//

                        if(selectedImageUriString!=null && selectedImageURIProfile==null){
                            Log.i(TAG,"in 1");
                            selectedImageURI= Uri.parse(selectedImageUriString);
                            uploadImage();
                            Log.i(TAG,"in 2");

                        }else if(selectedImageUriString==null && selectedImageURIProfile==null){
                            addData();
                            Log.i(TAG,"in 3");
                        }else if(selectedImageURIProfile!=null && selectedImageUriString==null){
                            Log.i(TAG,"in 4");
                            uploadProfileImage();
                        }else if(selectedImageUriString!=null && selectedImageURIProfile!=null){
                            selectedImageURI= Uri.parse(selectedImageUriString);
                            Log.i(TAG,"in 5");
                            uploadProfileandVehicleImage();
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
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }

                }
            }
        });
        builder.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);


            }else{
                bitmap_photo=(Bitmap)data.getExtras().get("data");
                guest_image.setImageBitmap(bitmap_photo);
                selectedImageURIProfile=bitmapToUriConverter(bitmap_photo);
                // uploadImage();
            }


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



    public Uri bitmapToUriConverter(Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 200, 200,
                    true);
            File file = new File(getApplicationContext().getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = getApplicationContext().openFileOutput(file.getName(),
                    Context.MODE_PRIVATE);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 60, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

        Log.i(TAG,"The selected indices are "+indices);

        if(indices.isEmpty()){
            showMessage("Error!","Please select a flat",R.drawable.ic_error_dialog);

        }

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
        final String document_ref="document_ref";
        final String vehicle_number_fb = getString(R.string.vehicle_number);
        final String car_type_fb = getString(R.string.car_type);
        final String vehicle_image_url = getString(R.string.vehicle_image_url);
        final String frequent_visitor = "frequent_visitor";
        final String user_id = currentFirebaseUser.getUid();


        guestArrayList = new ArrayList<ContentAddGuest>();



        DocumentReference docRef = db.collection(user).document(user_id);
        mProgressBar.setVisibility(View.VISIBLE);

        Log.i(TAG,"DOCUMENT REFERENCE IS "+docRef.toString());

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    data.put("profile_image_URL",profile_image_URL);



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
        uploadTask = ref.putFile(selectedImageURI);

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
        final StorageReference ref = storageReference.child("guest_images/"+ UUID.randomUUID().toString());
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


        final StorageReference ref2 = storageReference.child("guest_images/"+ UUID.randomUUID().toString());
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
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadUri = task.getResult();
                                profile_image_URL =downloadUri.toString();
                                Log.i(TAG,"The URL for this image is "+downloadUri);
                                // Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();

                                addData();


                            } else {
                                // Handle failures
                                // ...
                                Log.i(TAG,"error is "+task.getException());
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


}
