package com.hello.khushboo.replonguesty.CheckoutGuests;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hello.khushboo.replonguesty.R;


import de.hdodenhof.circleimageview.CircleImageView;

public class GuestCheckoutViewActivity extends AppCompatActivity {

    private static final String TAG = "GuestCheckoutView";
    ImageView back;
    TextView guest_name,phno,veh_num, veh_type, checkin_time, checkout_time,purpose,flat_nos;
    CircleImageView veh_img,profile_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_checkout_view);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        veh_img = findViewById(R.id.veh_img);
        profile_img= (CircleImageView) findViewById(R.id.profile_img);
        guest_name = findViewById(R.id.guest_name);
        phno = findViewById(R.id.guest_phno);
        veh_num = findViewById(R.id.veh_num);
        veh_type = findViewById(R.id.veh_type);
        checkin_time = findViewById(R.id.checkin_time);
        checkout_time = findViewById(R.id.checkout_time);
        purpose = findViewById(R.id.purpose);
        flat_nos = findViewById(R.id.flat_nos);


        guest_name.setText(getIntent().getStringExtra("name"));
        phno.setText(getIntent().getStringExtra("phno"));

        if (getIntent().getStringExtra("veh_num").equals("")){
            veh_num.setText("--NA--");
            veh_type.setText("--NA--");
        }else {
            veh_num.setText(getIntent().getStringExtra("veh_num"));
            veh_type.setText(getIntent().getStringExtra("veh_type"));
        }
        phno.setTextIsSelectable(true);

        veh_type.setText(getIntent().getStringExtra("veh_type"));
        checkin_time.setText(getIntent().getStringExtra("checkin"));
        if (getIntent().getStringExtra("checkout").equals(null))
        {
            checkout_time.setText("--NA--");
        }else {
            checkout_time.setText(getIntent().getStringExtra("checkout"));
        }
        purpose.setText(getIntent().getStringExtra("purpose"));
        flat_nos.setText(getIntent().getStringExtra("flat_nos"));

        Log.i(TAG,"IMG URL IS: "+getIntent().getStringExtra("veh_img"));

        if(!getIntent().getStringExtra("veh_img").equals("")) {
            Glide.with(getApplicationContext()).load(getIntent().getStringExtra("veh_img")).into(veh_img);
        }
        else
        {
            veh_img.setImageResource(R.drawable.show_vehicle_guest);
        }

        Log.i(TAG,"PROFILE IMAGE: " + getIntent().getStringExtra("profile_img"));
        if(!getIntent().getStringExtra("profile_img").equals("")) {
            Glide.with(getApplicationContext()).load(getIntent().getStringExtra("profile_img")).into(profile_img);
        }
        else
        {
            profile_img.setImageResource(R.drawable.ic_default_guest);
        }






    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
