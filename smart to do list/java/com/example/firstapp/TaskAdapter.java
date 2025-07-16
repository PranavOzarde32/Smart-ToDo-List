package com.example.firstapp;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstapp.R;
import com.example.firstapp.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private OnTaskStatusChangedListener listener;

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskStatusChangedListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.checkBox.setText(task.getName());
        holder.checkBox.setChecked(task.isCompleted());

        holder.checkBox.setPaintFlags(task.isCompleted() ?
                holder.checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                holder.checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            task.setLastCheckedDate(getCurrentDate());
            listener.onTaskStatusChanged(task);
        });

        holder.checkBox.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit Task");

            final EditText input = new EditText(context);
            input.setText(task.getName());
            builder.setView(input);

            builder.setPositiveButton("Update", (dialog, which) -> {
                task.setName(input.getText().toString());
                listener.onTaskStatusChanged(task);
                notifyItemChanged(position);
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}
