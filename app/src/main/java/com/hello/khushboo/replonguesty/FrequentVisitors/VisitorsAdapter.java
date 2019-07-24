package com.hello.khushboo.replonguesty.FrequentVisitors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.hello.khushboo.replonguesty.GuestDataFirebase;
import com.hello.khushboo.replonguesty.R;

import java.util.List;


public class VisitorsAdapter extends RecyclerView.Adapter<VisitorsAdapter.VisitorViewHolder>{




        private Context mContext;
        private List<GuestDataFirebase> frequent_list;

        public VisitorsAdapter(Context mContext, List <GuestDataFirebase>frequent_list) {
            this.mContext=mContext;
            this.frequent_list=frequent_list;
        }

        @NonNull
        @Override
        public VisitorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater=LayoutInflater.from(viewGroup.getContext());
            View view=layoutInflater.inflate(R.layout.row_freq_visitors,null);
            VisitorViewHolder holder=new VisitorViewHolder(view);
            return holder;
        }


        //Will bind data to ViewHolder(UI elements)
        @Override
        public void onBindViewHolder(@NonNull VisitorViewHolder holder, int i) {


            //so this i is position that will give you the specified item from the product list!


            final GuestDataFirebase guestData=frequent_list.get(i);

            holder.guest_name.setText(String.valueOf(guestData.getName()));
            holder.purpose.setText(String.valueOf(guestData.getPurpose()));
            if (String.valueOf(guestData.getVehicle_no()).equals("")){
                holder.veh_num.setText("--NA--");
            }else {
                holder.veh_num.setText(String.valueOf(guestData.getVehicle_no()));
            }
            holder.phone.setText(String.valueOf(guestData.getPhone_no()));




        }

        public void updateList(List<GuestDataFirebase> list){
            frequent_list = list;
            notifyDataSetChanged();
        }


        //Will return the size of the list  ie the number of elements available inside the list that is 5
        @Override
        public int getItemCount() {
            return frequent_list.size();
        }

        class VisitorViewHolder extends RecyclerView.ViewHolder {


            TextView guest_name,purpose,phone,veh_num,curr_checkin;
            RelativeLayout row;
            Button btn_checkin;


            public VisitorViewHolder(@NonNull View itemView) {
                super(itemView);

                guest_name = itemView.findViewById(R.id.guest_name);
                purpose = itemView.findViewById(R.id.guest_purpose);
                phone = itemView.findViewById(R.id.phone);
                veh_num = itemView.findViewById(R.id.veh_num);
                curr_checkin = itemView.findViewById(R.id.checkedin_text);
                row = (RelativeLayout)itemView.findViewById(R.id.row);
                btn_checkin=(Button)itemView.findViewById(R.id.btn_checkin);


            }
        }





}
