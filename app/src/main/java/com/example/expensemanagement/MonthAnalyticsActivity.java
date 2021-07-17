package com.example.expensemanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Map;

public class MonthAnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String[] items;

    private FirebaseAuth mAuth;
    private DatabaseReference personalRef;

    private TextView totalAmountSpent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_analytics);

        toolbar = findViewById(R.id.toolbar);
        totalAmountSpent = findViewById((R.id.totalAmountSpent));

        items = getResources().getStringArray(R.array.expenseItems);

        mAuth = FirebaseAuth.getInstance();
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(mAuth.getCurrentUser().getUid());

        getTotalMonthsItemsExpense(items);
        getBudgetState(items);
        getTotalMonthSpending();
    }

    private void getBudgetState(String[] items) {
        for(String item: items) {
            TextView remainingBudget = getRemainingBudgetView(item);
            personalRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        float itemTotal, itemBudget;
                        if(snapshot.hasChild("month"+item)){
                            itemTotal = Integer.parseInt(snapshot.child("month"+item).getValue().toString());
                        }
                        else {
                            itemTotal=0;
                        }
                        if(snapshot.hasChild("budget"+item)){
                            itemBudget =  Integer.parseInt(snapshot.child("budget"+item).getValue().toString());
                        }
                        else{
                            itemBudget = 0;
                        }
                        if(itemBudget == 0) {
                            remainingBudget.setText("Budget not set");
                        }
                        else {
                            float percentRemaining = 100 - (itemTotal*100/itemBudget);
                            remainingBudget.setText("RemainingBudget: " + percentRemaining + "%");
                        }
                    }
                    else {
                        Toast.makeText(MonthAnalyticsActivity.this, "budget Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getTotalMonthsItemsExpense(String[] items) {
        for(String item: items) {
            MutableDateTime epoch = new MutableDateTime();
            epoch.setDate(0);
            DateTime now = new DateTime();
            Months months = Months.monthsBetween(epoch, now);

            TextView analyticsItemAmount = getAmountView(item);
            LinearLayout linearLayout = getLinearLayoutView(item);

            String itemNMonth = item+(months.getMonths());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
            Query query = reference.orderByChild("itemNMonth").equalTo(itemNMonth);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        int totAmount = 0;
                        for (DataSnapshot ds :  snapshot.getChildren()) {
                            Map<String, Object> map = (Map<String, Object>) ds.getValue();
                            Object total = map.get("amount");
                            int pTotal = Integer.parseInt(String.valueOf(total));
                            totAmount += pTotal;
                            analyticsItemAmount.setText("Spent: \u20B9" + totAmount);
                        }
                        personalRef.child("month"+item).setValue(totAmount);
                    } else {
                        linearLayout.setVisibility(View.GONE);
                        personalRef.child("month"+item).setValue(0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MonthAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private TextView getAmountView(String item) {
        switch (item) {
            case "Food":
                return findViewById(R.id.analyticsFoodAmount);
            case "Transport":
                return findViewById(R.id.analyticsTransportAmount);
            case "House":
                return findViewById(R.id.analyticsHouseAmount);
            case "Entertainment":
                return findViewById(R.id.analyticsEntertainmentAmount);
            case "Education":
                return findViewById(R.id.analyticsEducationAmount);
            case "Charity":
                return findViewById(R.id.analyticsCharityAmount);
            case "Apparel":
                return findViewById(R.id.analyticsApparelAmount);
            case "Health":
                return findViewById(R.id.analyticsHealthAmount);
            case "Personal":
                return findViewById(R.id.analyticsPersonalAmount);
            case "Other":
                return findViewById(R.id.analyticsOtherAmount);
        }
        return null;
    }
    private TextView getRemainingBudgetView(String item) {
        switch (item) {
            case "Food":
                return findViewById(R.id.remainingBudgetFood);
            case "Transport":
                return findViewById(R.id.remainingBudgetTransport);
            case "House":
                return findViewById(R.id.remainingBudgetHouse);
            case "Entertainment":
                return findViewById(R.id.remainingBudgetEntertainment);
            case "Education":
                return findViewById(R.id.remainingBudgetEducation);
            case "Charity":
                return findViewById(R.id.remainingBudgetCharity);
            case "Apparel":
                return findViewById(R.id.remainingBudgetApparel);
            case "Health":
                return findViewById(R.id.remainingBudgetHealth);
            case "Personal":
                return findViewById(R.id.remainingBudgetPersonal);
            case "Other":
                return findViewById(R.id.remainingBudgetOther);
        }
        return null;
    }
    private LinearLayout getLinearLayoutView(String item) {
        switch (item) {
            case "Food":
                return findViewById(R.id.linearLayoutFood);
            case "Transport":
                return findViewById(R.id.linearLayoutTransport);
            case "House":
                return findViewById(R.id.linearLayoutHouse);
            case "Entertainment":
                return findViewById(R.id.linearLayoutEntertainment);
            case "Education":
                return findViewById(R.id.linearLayoutEducation);
            case "Charity":
                return findViewById(R.id.linearLayoutCharity);
            case "Apparel":
                return findViewById(R.id.linearLayoutApparel);
            case "Health":
                return findViewById(R.id.linearLayoutHealth);
            case "Personal":
                return findViewById(R.id.linearLayoutPersonal);
            case "Other":
                return findViewById(R.id.linearLayoutOther);
        }
        return null;
    }

    private void getTotalMonthSpending(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = reference.orderByChild("month").equalTo(months.getMonths());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    int totalAmount = 0;
                    for (DataSnapshot ds :  dataSnapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount+=pTotal;

                    }
                    totalAmountSpent.setText("Total Months's spending: \u20B9"+ totalAmount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}