package com.example.expensemanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FixedSpanExpenseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalMonthAmountSpent;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private FixedSpanExpenseAdapter fixedSpanSpendingAdapter;
    private List<Data> dataList;
    private FirebaseAuth mAuth;
    private DatabaseReference expenseRef;

    private String type="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixed_span_expense);

        if(getIntent().getExtras()!=null) {
            type =  getIntent().getStringExtra("type").toUpperCase();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(type+"'s Spending");
        totalMonthAmountSpent = findViewById(R.id.totalMonthAmount);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();

        dataList = new ArrayList<>();
        fixedSpanSpendingAdapter = new FixedSpanExpenseAdapter(FixedSpanExpenseActivity.this, dataList);
        recyclerView.setAdapter(fixedSpanSpendingAdapter);

        if(type.equals("MONTH")){
            readMonthExpenseItems();
        }
    }

    private void readMonthExpenseItems() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = DateTime.now();
        Months months = Months.monthsBetween(epoch, now);

        expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = expenseRef.orderByChild("month").equalTo(months.getMonths());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    dataList.add(data);
                }

                fixedSpanSpendingAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                int totAmount=0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)dataSnapshot.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totAmount+=pTotal;

                    totalMonthAmountSpent.setText("Month's Total Spending: \u20B9"+totAmount);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }
}