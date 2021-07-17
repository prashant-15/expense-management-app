package com.example.expensemanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.zip.Inflater;

public class BudgetActivity extends AppCompatActivity {

    private TextView totalBudget;
    private RecyclerView recyclerView;

    private FloatingActionButton fab;

    private DatabaseReference budgetRef, personalRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private String postKey = "";
    private String item = "";
    private int amount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        totalBudget = findViewById(R.id.totalBudget);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0) {
                    int totAmount = 0;

                    for(DataSnapshot snap: snapshot.getChildren()) {
                        Data data = snap.getValue(Data.class);
                        totAmount+=data.getAmount();
                        String sTotal = String.valueOf("Month budget: \u20B9"+totAmount);
                        totalBudget.setText(sTotal);
                    }
                    personalRef.child("budget").setValue(totAmount);
                }
                else{
                    personalRef.child("budget").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    private void addItem() {
        AlertDialog.Builder addItemDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View addItemView = inflater.inflate(R.layout.input_layout, null);
        addItemDialog.setView(addItemView);
        final AlertDialog dialog = addItemDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = addItemView.findViewById(R.id.spinner);
        final EditText amount = addItemView.findViewById(R.id.amount);
        final Button cancelBtn = addItemView.findViewById(R.id.cancelBtn);
        final Button saveBtn = addItemView.findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budgetAmount = amount.getText().toString();
                String budgetItem = itemSpinner.getSelectedItem().toString();

                if(TextUtils.isEmpty(budgetAmount)){
                    amount.setError("Please enter amount!");
                    return;
                }
                if(budgetItem.equals("Select Item")){
                    Toast.makeText(BudgetActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                }
                else{
                    loader.setMessage("Adding budget item...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = budgetRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = DateTime.now();
                    Months months = Months.monthsBetween(epoch, now);

                    String itemNMonth = item + months.getMonths();

                    Data data = new Data(budgetItem, date, id, null, itemNMonth, Integer.parseInt(budgetAmount), months.getMonths());
                    budgetRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(BudgetActivity.this, "Budget item added successfully", Toast.LENGTH_SHORT).show();
                                personalRef.child("budget"+budgetItem).setValue(Integer.parseInt(budgetAmount));
                            }
                            else{
                                Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>().setQuery(budgetRef, Data.class).build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setItemName("Budget Item: " + model.getItem());
                holder.setItemAmount("Budget Amount: \u20B9" + model.getAmount());
                holder.setDate("Date: " + model.getDate());

                holder.note.setVisibility(View.GONE);

                switch (model.getItem()) {
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

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postKey = getRef(position).getKey();
                        item = model.getItem();
                        amount = model.getAmount();
                        updateData();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged(); //re check if correct
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View myView;
        public ImageView imageView;
        public TextView note, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            myView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            note = itemView.findViewById(R.id.note);
            date = itemView.findViewById(R.id.date);
        }

        public void setItemName (String itemName) {
            TextView item = myView.findViewById(R.id.item);
            item.setText(itemName);
        }
        public void setItemAmount (String itemAmount) {
            TextView amount = myView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }
        public void setDate (String itemDate) {
            TextView date = myView.findViewById(R.id.date);
            date.setText(itemDate);
        }
    }

    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(myView);
        final AlertDialog dialog = myDialog.create();

        final TextView myItem = myView.findViewById(R.id.itemName);
        final EditText myAmount = myView.findViewById(R.id.amount);
        final EditText myNote = myView.findViewById(R.id.note);

        myNote.setVisibility(View.GONE);

        myItem.setText(item);
        myAmount.setText(String.valueOf(amount));
        myAmount.setSelection(String.valueOf(amount).length());

        Button delBtn = myView.findViewById(R.id.delBtn);
        Button updateBtn = myView.findViewById(R.id.updateBtn);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = Integer.parseInt(myAmount.getText().toString());

                DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = DateTime.now();
                Months months = Months.monthsBetween(epoch, now);

                String itemNMonth = item+months.getMonths();

                Data data = new Data(item, date, postKey, null, itemNMonth, amount, months.getMonths());
                budgetRef.child(postKey).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(BudgetActivity.this, "Update successfully!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                budgetRef.child(postKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(BudgetActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}