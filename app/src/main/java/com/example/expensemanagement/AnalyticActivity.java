package com.example.expensemanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AnalyticActivity extends AppCompatActivity {
    private CardView monthCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytic);

        monthCardView = findViewById(R.id.monthCardView);

        monthCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AnalyticActivity.this, MonthAnalyticsActivity.class);
                startActivity(intent);
            }
        });
    }
}