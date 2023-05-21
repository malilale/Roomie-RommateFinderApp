package com.roomie.app;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder>{

    final SelectListener listener;
    Context context;
    ArrayList<Notification> list;

    public NotificationAdapter(Context context, ArrayList<Notification> list, SelectListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item,parent,false);
        return new NotificationAdapter.MyViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.MyViewHolder holder, int position) {
        Notification notification = list.get(position);

        holder.tv_notification.setText(notification.getMessage());
        String date = notification.getDate()+"  "+notification.getTime();
        holder.tv_date.setText(date);

        if(!notification.getImgUrl().isEmpty())
            Picasso.get().load(notification.getImgUrl()).into(holder.img_profile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_notification, tv_date;
        private ImageView img_profile;

        public MyViewHolder(@NonNull View itemView, SelectListener listener) {
            super(itemView);

            tv_notification = itemView.findViewById(R.id.tv_notification);
            tv_date = itemView.findViewById(R.id.tv_notdate);
            img_profile = itemView.findViewById(R.id.img_not_profile);

            itemView.setOnClickListener(view -> {
                if (listener != null){
                    int posisiton = getAdapterPosition();
                    if(posisiton != RecyclerView.NO_POSITION)
                        listener.onItemClicked(posisiton);
                }
            });
        }
    }

}
