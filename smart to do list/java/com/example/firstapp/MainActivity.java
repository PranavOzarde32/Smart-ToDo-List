package com.example.firstapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstapp.TaskAdapter;
import com.example.firstapp.TaskDatabaseHelper;
import com.example.firstapp.NotificationHelper;
import com.example.firstapp.ReminderBroadcast;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private TaskDatabaseHelper dbHelper;
    private List<Task> taskList;
    private TaskAdapter adapter;

    private EditText inputTask;
    private Button addButton, setReminderButton;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationHelper.createNotificationChannel(this);

        dbHelper = new TaskDatabaseHelper(this);
        taskList = dbHelper.getAllTasks();
        checkAndResetTasksIfNeeded(taskList);

        inputTask = findViewById(R.id.inputTask);
        addButton = findViewById(R.id.addButton);
        setReminderButton = findViewById(R.id.setReminderButton);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskList, task -> dbHelper.updateTask(task));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder tgt) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                int position = vh.getAdapterPosition();
                Task task = taskList.get(position);
                taskList.remove(position);
                dbHelper.deleteTask(task.getId());
                adapter.notifyItemRemoved(position);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        addButton.setOnClickListener(v -> {
            String taskName = inputTask.getText().toString().trim();
            if (taskName.isEmpty()) {
                Toast.makeText(this, "Enter a task name", Toast.LENGTH_SHORT).show();
                return;
            }

            String today = getCurrentDate();
            Task newTask = new Task(0, taskName, false, today);
            dbHelper.addTask(newTask);
            refreshTaskList();
            inputTask.setText("");
        });

        setReminderButton.setOnClickListener(v -> showTimePicker());

        // Load previously set time
        SharedPreferences prefs = getSharedPreferences("reminder_prefs", MODE_PRIVATE);
        int hour = prefs.getInt("hour", 8);
        int minute = prefs.getInt("minute", 0);
        scheduleReminder(hour, minute);
    }

    private void showTimePicker() {
        final Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(MainActivity.this, (view, selectedHour, selectedMinute) -> {
            saveReminderTime(selectedHour, selectedMinute);
            scheduleReminder(selectedHour, selectedMinute);
            Toast.makeText(this, "Reminder set for " + selectedHour + ":" + String.format("%02d", selectedMinute), Toast.LENGTH_SHORT).show();
        }, hour, minute, true);

        timePicker.setTitle("Select Reminder Time");
        timePicker.show();
    }

    private void saveReminderTime(int hour, int minute) {
        SharedPreferences prefs = getSharedPreferences("reminder_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.apply();
    }

    private void scheduleReminder(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void refreshTaskList() {
        taskList.clear();
        taskList.addAll(dbHelper.getAllTasks());
        adapter.notifyDataSetChanged();
    }

    private void checkAndResetTasksIfNeeded(List<Task> tasks) {
        String today = getCurrentDate();
        for (Task task : tasks) {
            if (!task.getLastCheckedDate().equals(today)) {
                task.setCompleted(false);
                task.setLastCheckedDate(today);
                dbHelper.updateTask(task);
            }
        }
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
