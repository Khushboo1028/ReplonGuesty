package com.hello.khushboo.replonguesty.AddGuests;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hello.khushboo.replonguesty.Image;
import com.hello.khushboo.replonguesty.R;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class AddVehicleActivity extends AppCompatActivity {

    public static final String TAG = "AddVehicleActivity";
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 56;
    public static final int MEDIA_TYPE_IMAGE = 1;

    ImageView back_add_veh;
    Button add_vehicle;
    EditText et_vehicle_number;
    String car_type;

    //for images
    Uri selectedImageURI;
    Uri file_camera_uri;
    String imageURL="";
    StorageTask uploadTask;
    FirebaseStorage storage;
    StorageReference storageReference;
    ImageView vehicle_image;
    Bitmap bitmap_photo;
    List<Image> vehicleList;
    View btn_vehicle_add;


    private static final int REQUEST_CAMERA = 100;
    private static final int RESULT_LOAD_IMAGE = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainrel);
        relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                InputMethodManager imm =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });

        back_add_veh = (ImageView) findViewById(R.id.back_add_veh);
        add_vehicle = (Button) findViewById(R.id.add_vehicle);
        et_vehicle_number=(EditText)findViewById(R.id.vehicle_number);


        back_add_veh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //for camera intent
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Spinner element
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);


        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        //categories.add("");
        categories.add("4 - Wheeler");
        categories.add("2 - Wheeler");



        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);




        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                car_type= String.valueOf(spinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                car_type="";


            }
        });

        //add Image
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        vehicle_image=(ImageView)findViewById(R.id.vehicle_image);
        vehicleList=new ArrayList<>();
        btn_vehicle_add=(View) findViewById(R.id.borderview);
        btn_vehicle_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShowPhoto();
            }
        });



        add_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vehicle_number=et_vehicle_number.getText().toString().toUpperCase();
                Intent intent=new Intent();
                intent.putExtra("vehicle_number",vehicle_number);
                intent.putExtra("car_type",car_type);
                intent.putExtra("vehicle_image_URL",imageURL);
                if(selectedImageURI!=null){
                    intent.putExtra("selectedImageUri",selectedImageURI.toString());
                }

                setResult(RESULT_OK,intent);
                finish();


            }
        });
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
                        ActivityCompat.requestPermissions(AddVehicleActivity.this, PERMISSIONS, PERMISSION_ALL);
                    }

                    if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {


                        if (ActivityCompat.shouldShowRequestPermissionRationale(AddVehicleActivity.this,
                                Manifest.permission.CAMERA) == true){


                            final Dialog dialog1 = new Dialog(AddVehicleActivity.this);
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
                            ActivityCompat.requestPermissions(AddVehicleActivity.this, new String[]{Manifest.permission.CAMERA},
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


            }else{
                vehicle_image.setImageURI(file_camera_uri);
                selectedImageURI=file_camera_uri;
                // uploadImage();
            }



        }
//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK  && data != null && data.getData() != null) {
//            selectedImageURI = data.getData();
//
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageURI);
//                // bitmap_photo = Bitmap.createScaledBitmap(bitmap,50,67,false);
//                vehicle_image.setImageBitmap(bitmap);
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



}
