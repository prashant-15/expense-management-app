package com.example.expensemanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TodayItemsAdapter extends RecyclerView.Adapter<TodayItemsAdapter.ViewHolder> {

    private Context mContext;
    private List<Data> dataList;
    private String postKey="";
    private String item="";
    private String note="";
    private int amount=0;

    public TodayItemsAdapter(Context mContext, List<Data> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrieve_layout,parent,false);
        return new TodayItemsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayItemsAdapter.ViewHolder holder, int position) {
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postKey = data.getId();
                item = data.getItem();
                amount = data.getAmount();
                note = data.getNote();
                updateData();
            }
        });
    }

    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View myView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();

        final TextView myItem = myView.findViewById(R.id.itemName);
        final EditText myAmount = myView.findViewById(R.id.amount);
        final EditText myNote = myView.findViewById(R.id.note);

        myItem.setText(item);
        myAmount.setText(String.valueOf(amount));
        myAmount.setSelection(String.valueOf(amount).length());
        myNote.setText(note);
        myNote.setSelection(note.length());

        Button delBtn = myView.findViewById(R.id.delBtn);
        Button updateBtn = myView.findViewById(R.id.updateBtn);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = Integer.parseInt(myAmount.getText().toString());
                note = myNote.getText().toString();

                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = DateTime.now();
                Months months = Months.monthsBetween(epoch, now);

                String itemNMonth = item+months.getMonths();

                Data data = new Data(item, date, postKey, note, itemNMonth, amount, months.getMonths());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.child(postKey).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext, "Update successfully!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(mContext, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.child(postKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(mContext, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
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
