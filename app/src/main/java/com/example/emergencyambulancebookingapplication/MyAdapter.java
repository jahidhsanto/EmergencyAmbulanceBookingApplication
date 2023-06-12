package com.example.emergencyambulancebookingapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Driver> driverArrayList;
    private final RecyclerViewInterface recyclerViewInterface;

    public MyAdapter(Context context, ArrayList<Driver> driverArrayList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.driverArrayList = driverArrayList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);

        return new MyViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Driver driver = driverArrayList.get(position);

        holder.firstName.setText(driver.fullName);
        holder.lastName.setText(driver.companyName);
        holder.Age.setText(String.valueOf(driver.ambulanceCategory));

    }

    @Override
    public int getItemCount() {
        return driverArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView firstName, lastName, Age;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            firstName = itemView.findViewById(R.id.tvfirstName);
            lastName = itemView.findViewById(R.id.tvlastName);
            Age = itemView.findViewById(R.id.tvage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
