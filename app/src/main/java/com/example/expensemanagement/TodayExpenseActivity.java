package com.example.expensemanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TodayExpenseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalAmountSpent;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference expenseRef;

    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_expense);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("TODAY's Spending");
        totalAmountSpent = findViewById(R.id.totalAmountSpent);
        progressBar = findViewById(R.id.progressBar);

        fab = findViewById(R.id.fab);
        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        dataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(TodayExpenseActivity.this, dataList);
        recyclerView.setAdapter(todayItemsAdapter);

        readItems();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemSpentOn();
            }
        });
    }

    private void readItems() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Data data = dataSnapshot.getValue(Data.class);
                    dataList.add(data);
                }

                todayItemsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                int totAmount=0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)dataSnapshot.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totAmount+=pTotal;

                    totalAmountSpent.setText("Today's Total Spending: \u20B9"+totAmount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addItemSpentOn() {
        AlertDialog.Builder addItemDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        addItemDialog.setView(myView);
        final AlertDialog dialog = addItemDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.spinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final EditText note = myView.findViewById(R.id.note);
        final Button cancelBtn = myView.findViewById(R.id.cancelBtn);
        final Button saveBtn = myView.findViewById(R.id.saveBtn);

        note.setVisibility(View.VISIBLE);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String expenseAmount = amount.getText().toString();
                String expenseItem = itemSpinner.getSelectedItem().toString();
                String expenseNote = note.getText().toString();

                if(TextUtils.isEmpty(expenseAmount)){
                    amount.setError("Please enter amount!");
                    return;
                }
                if(expenseItem.equals("Select item")){
                    Toast.makeText(TodayExpenseActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                    return;
                }
//                if(TextUtils.isEmpty(expenseNote)){
//                    note.setError("Note required!");
//                    return;
//                }
                else{
                    loader.setMessage("Adding budget item...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = expenseRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = DateTime.now();
                    Months months = Months.monthsBetween(epoch, now);

                    String itemNMonth = expenseItem+months.getMonths();

                    Data data = new Data(expenseItem, date, id, expenseNote, itemNMonth, Integer.parseInt(expenseAmount), months.getMonths());
                    expenseRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(TodayExpenseActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(TodayExpenseActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
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
}