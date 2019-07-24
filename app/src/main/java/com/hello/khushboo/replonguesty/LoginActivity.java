package com.hello.khushboo.replonguesty;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    Button cont;
    private FirebaseAuth mAuth;

    EditText et_email;
    EditText et_password;
    Button btn_login;

    TextView invalid_text;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
      //  actionBar.hide();

        et_email=(EditText)findViewById(R.id.username);
        et_password=(EditText)findViewById(R.id.password);
        btn_login=(Button)findViewById(R.id.login);

        invalid_text = (TextView)findViewById(R.id.invalid_text);

        mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainrel);
        relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                InputMethodManager imm =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });


        et_email.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                et_email.setBackgroundResource(R.drawable.edittext_borders);
                et_password.setBackgroundResource(R.drawable.edittext_borders);
                return false;
            }
        });

        et_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                et_email.setBackgroundResource(R.drawable.edittext_borders);
                et_password.setBackgroundResource(R.drawable.edittext_borders);
                return false;
            }
        });



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=et_email.getText().toString().toLowerCase();
                String password=et_password.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    invalid_text.setText("Please Enter all the Details");
                    changeUI();
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    v.getBackground().setAlpha(100);
                    mAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()){

                                        mProgressBar.setVisibility(View.GONE);

                                        Intent login_intent = new Intent(LoginActivity.this, MainActivity.class);
                                        login_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(login_intent);
                                        overridePendingTransition(0,0);
                                        finish();

                                    }
                                    else{
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                        String exception=task.getException().toString();
                                        int index=exception.indexOf(":");
                                        String data=exception.substring(index+1).trim();
                                        showMessage("Error",data,R.drawable.ic_error_dialog);
                                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        btn_login.setEnabled(true);

                                        changeUI();



                                    }

                                }


                            });
                }


            }



            private void changeUI() {

                Vibrator vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vibrator.vibrate(500);
                }
                et_email.setBackgroundResource(R.drawable.edittext_borders_red);
                et_password.setBackgroundResource(R.drawable.edittext_borders_red);

            }
        });




    }

    public void showMessage(String title, String message,int image){

//        ,R.drawable.ic_error_dialog

        final Dialog dialog = new Dialog(LoginActivity.this);
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
        dialog_icon.setImageResource(image);
        dialog.show();

    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        //finish();
        //finishAffinity();

    }
}
