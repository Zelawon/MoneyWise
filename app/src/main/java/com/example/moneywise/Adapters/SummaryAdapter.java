package com.example.moneywise.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Models.Summary;
import com.example.moneywise.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {

    private Context mContext;
    private List<Summary> mSummaryList;

    public SummaryAdapter(Context context, List<Summary> summaryList) {
        mContext = context;
        mSummaryList = summaryList;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        Summary summary = mSummaryList.get(position);

        // Set date
        holder.dateTextView.setText(summary.getDate());

        // Set income
        holder.incomeTextView.setText(String.format("%.2f€", summary.getIncome()));

        // Set expense
        holder.expenseTextView.setText(String.format("%.2f€", summary.getExpense()));

        // Set sum
        holder.sumTextView.setText(String.format("%.2f€", summary.getSum()));

        // Set number of transactions
        holder.numTransactionsTextView.setText(String.format("%d transactions", summary.getNumTransactions()));

        // Set most spending category
        holder.mostSpendingTextView.setText(summary.getMostSpendingCategory());

        populatePieChart(holder.pieChart, summary);
    }

    private void populatePieChart(PieChart pieChart, Summary summary) {
        // Create data entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        float income = (float) summary.getIncome();
        float expense = Math.abs((float) summary.getExpense()); //Make a negative number positive
        float entryIncome = (income / (income + expense)) * 100; //Percentage of Income: (Income / (Income + Expenses)) * 100
        float entryExpense = (expense / (income + expense)) * 100; //Percentage of Expenses: (Expenses / (Income + Expenses)) * 100
        entries.add(new PieEntry(entryExpense, ""));
        entries.add(new PieEntry(entryIncome, ""));

        // Create data set
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawValues(true); // Enable value labels
        dataSet.setValueTextColor(Color.WHITE); // Set value text color to black
        dataSet.setValueTextSize(14f); // Set value text size
        dataSet.setValueTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Set bold typeface

        // Custom ValueFormatter to add '%' symbol
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return (int) value + "%";
            }
        });

        // Retrieve colors from colors.xml
        int colorEntry1 = ContextCompat.getColor(mContext, R.color.light_red);
        int colorEntry2 = ContextCompat.getColor(mContext, R.color.green5);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(colorEntry1);
        colors.add(colorEntry2);
        dataSet.setColors(colors);

        // Customize data set
        dataSet.setSliceSpace(3f); // Space between slices
        dataSet.setSelectionShift(5f); // Shift distance for selected slice

        // Create pie data
        PieData pieData = new PieData(dataSet);

        // Set pie data to chart
        pieChart.setData(pieData);

        // Customize pie chart
        pieChart.setRotationEnabled(false); // Disable rotation
        pieChart.setHighlightPerTapEnabled(false); // Disable value highlighting
        pieChart.setDrawHoleEnabled(false); // Disable hole in the center
        pieChart.getDescription().setEnabled(false); // Disable description
        pieChart.getLegend().setEnabled(false); // Hide legend

        // Refresh chart
        pieChart.invalidate();
    }

    @Override
    public int getItemCount() {
        return mSummaryList.size();
    }

    public void setSummaryList(List<Summary> newList) {
        mSummaryList = newList;
        notifyDataSetChanged();
    }

    public class SummaryViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView incomeTextView;
        TextView expenseTextView;
        TextView sumTextView;
        TextView numTransactionsTextView;
        TextView mostSpendingTextView;
        PieChart pieChart;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_TextView);
            incomeTextView = itemView.findViewById(R.id.income_TextView);
            expenseTextView = itemView.findViewById(R.id.expense_TextView);
            sumTextView = itemView.findViewById(R.id.sum_TextView);
            numTransactionsTextView = itemView.findViewById(R.id.numTransaction_textview);
            mostSpendingTextView = itemView.findViewById(R.id.mostSpending_Textview);
            pieChart = itemView.findViewById(R.id.pieChart);
        }
    }
}
