package com.example.paisafy.Model;

public class Todo {
    private int id, userId;
    private String task;
    private String date;
    private String time;
    private String status;

    public Todo(int id, int userId, String task, String date, String time, String status) {
        this.id = id;
        this.userId = userId;
        this.task = task;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTask() {
        return task;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
