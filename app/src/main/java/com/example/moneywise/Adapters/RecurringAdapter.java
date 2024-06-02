package com.example.moneywise.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Models.Recurring;
import com.example.moneywise.R;

import java.util.List;

public class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.RecurringViewHolder> {

    private Context context;
    private List<Recurring> recurringList;
    private OnItemLongPressListener longPressListener;

    public interface OnItemLongPressListener {
        void onItemLongPressed(Recurring recurring);
    }

    public RecurringAdapter(Context context, List<Recurring> recurringList) {
        this.context = context;
        this.recurringList = recurringList;
    }

    public void setOnItemLongPressListener(OnItemLongPressListener listener) {
        this.longPressListener = listener;
    }

    @NonNull
    @Override
    public RecurringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring, parent, false);
        return new RecurringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringViewHolder holder, int position) {
        Recurring recurring = recurringList.get(position);
        holder.bind(recurring);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longPressListener != null) {
                    longPressListener.onItemLongPressed(recurring);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return recurringList.size();
    }

    public class RecurringViewHolder extends RecyclerView.ViewHolder {
        TextView subCategoryTextView, startDateTextView, endDateTextView, amountTextView;

        public RecurringViewHolder(@NonNull View itemView) {
            super(itemView);
            subCategoryTextView = itemView.findViewById(R.id.subCategoryTextView);
            startDateTextView = itemView.findViewById(R.id.startDate_textview);
            endDateTextView = itemView.findViewById(R.id.endDate_textview);
            amountTextView = itemView.findViewById(R.id.amountTextView);
        }

        public void bind(Recurring recurring) {
            subCategoryTextView.setText(recurring.getSubcategory());
            startDateTextView.setText("Start Date: " + recurring.getDate());
            endDateTextView.setText("End Date: " + recurring.getEndDateFrequency());

            // Format amount based on type
            String amount = recurring.getAmount() + "€";
            if (recurring.getType().equals("Income")) {
                amountTextView.setText("+ " + amount);
                amountTextView.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else if (recurring.getType().equals("Expense")) {
                amountTextView.setText("- " + amount);
                amountTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        }
    }
}
