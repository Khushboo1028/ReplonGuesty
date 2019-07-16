package com.hello.khushboo.replonguesty.CheckoutGuests;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hello.khushboo.replonguesty.GuestDataFirebase;
import com.hello.khushboo.replonguesty.R;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutHolder> {

    Context mContext;
    List<GuestDataFirebase> checkoutList;

    public CheckoutAdapter(Context mContext, List<GuestDataFirebase> checkoutList) {
        this.mContext = mContext;
        this.checkoutList = checkoutList;
    }

    @NonNull
    @Override
    public CheckoutHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater=LayoutInflater.from(mContext);
        View view=layoutInflater.inflate(R.layout.row_checkout_list,null);
        CheckoutHolder holder=new CheckoutHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutHolder checkoutHolder, int i) {

        Log.i("","inCallAdapter");

        final GuestDataFirebase guestDataFirebase=checkoutList.get(i);

        checkoutHolder.guest_name.setText(String.valueOf(guestDataFirebase.getName()));
        checkoutHolder.guest_purpose.setText(String.valueOf(guestDataFirebase.getPurpose()));
        checkoutHolder.guest_flatno.setText(String.valueOf(guestDataFirebase.getFlat_no()));
        checkoutHolder.dateAndTime.setText(String.valueOf(guestDataFirebase.getCheck_in_time()));
        checkoutHolder.dateAndTime_out.setText(String.valueOf(guestDataFirebase.getCheckout_time()));

        if(guestDataFirebase.getCheckout()){
            checkoutHolder.btn_checkout.setVisibility(View.GONE);
            checkoutHolder.dateAndTime_out.setText(String.valueOf(guestDataFirebase.getCheckout_time()));
        }


    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    public void updateList(List<GuestDataFirebase> list){
        checkoutList = list;
        notifyDataSetChanged();
    }


    public class CheckoutHolder extends RecyclerView.ViewHolder{
        TextView guest_name,guest_purpose,guest_flatno,dateAndTime, dateAndTime_out;
        RelativeLayout row;
        Button btn_checkout;


        public CheckoutHolder(@NonNull View itemView) {
            super(itemView);

            guest_name = itemView.findViewById(R.id.guest_name);
            guest_purpose = itemView.findViewById(R.id.guest_purpose);
            guest_flatno = itemView.findViewById(R.id.guest_flatno);
            dateAndTime = itemView.findViewById(R.id.dateAndTime);
            dateAndTime_out = itemView.findViewById(R.id.dateAndTime_out);
            row = (RelativeLayout)itemView.findViewById(R.id.row);
            btn_checkout=(Button)itemView.findViewById(R.id.btn_checkout);


        }
    }
}
