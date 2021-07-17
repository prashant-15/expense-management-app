package com.example.expensemanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FixedSpanExpenseAdapter extends RecyclerView.Adapter<FixedSpanExpenseAdapter.ViewHolder> {

    private Context mContext;
    private List<Data> dataList;

    public FixedSpanExpenseAdapter(Context mContext, List<Data> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrieve_layout,parent,false);
        return new FixedSpanExpenseAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FixedSpanExpenseAdapter.ViewHolder holder, int position) {
        final Data data = dataList.get(position);

        holder.item.setText("Item: "+data.getItem());
        holder.amount.setText("Amount: \u20B9"+data.getAmount());
        holder.date.setText("Date: "+data.getDate());
        holder.note.setText(data.getNote()!=null ?  "Note: "+data.getNote() : "");

        switch (data.getItem()) {
            case "Transport":
                holder.imageView.setImageResource(R.drawable.transport);
                break;
            case "Food":
                holder.imageView.setImageResource(R.drawable.food);
                break;
            case "House":
                holder.imageView.setImageResource(R.drawable.house);
                break;
            case "Entertainment":
                holder.imageView.setImageResource(R.drawable.entertainment);
                break;
            case "Education":
                holder.imageView.setImageResource(R.drawable.education);
                break;
            case "Charity":
                holder.imageView.setImageResource(R.drawable.charity);
                break;
            case "Apparel":
                holder.imageView.setImageResource(R.drawable.apparel);
                break;
            case "Health":
                holder.imageView.setImageResource(R.drawable.health);
                break;
            case "Personal":
                holder.imageView.setImageResource(R.drawable.personal);
                break;
            case "Other":
                holder.imageView.setImageResource(R.drawable.other);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView item, amount, date, note;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.item);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
            note = itemView.findViewById(R.id.note);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
