package com.example.firstapp;

public class Task {
    private int id;
    private String name;
    private boolean isCompleted;
    private String lastCheckedDate;

    public Task(int id, String name, boolean isCompleted, String lastCheckedDate) {
        this.id = id;
        this.name = name;
        this.isCompleted = isCompleted;
        this.lastCheckedDate = lastCheckedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getLastCheckedDate() { return lastCheckedDate; }
    public void setLastCheckedDate(String date) { this.lastCheckedDate = date; }
}
