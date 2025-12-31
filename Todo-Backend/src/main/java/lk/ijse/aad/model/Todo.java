package lk.ijse.aad.model;

import java.sql.Timestamp;

public class Todo {
    private int id;
    private String title;
    private String description;
    private boolean completed;
    private String priority;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Todo() {}

    public Todo(String title, String description, String priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.completed = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}