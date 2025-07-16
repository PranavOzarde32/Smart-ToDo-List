package com.example.firstapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import kotlinx.coroutines.scheduling.Task;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Query("DELETE FROM tasks")
    void deleteAll();
}

