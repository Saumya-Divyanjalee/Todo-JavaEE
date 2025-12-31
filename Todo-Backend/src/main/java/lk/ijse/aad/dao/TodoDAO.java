package lk.ijse.aad.dao;

import lk.ijse.aad.util.DBConnection;
import lk.ijse.aad.model.Todo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO {

    // Added: Static block to initialize DB table if not exists
    static {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS todos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM', " +
                    "completed BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
            stmt.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create todos table", e);
        }
    }

    public List<Todo> getAllTodos() throws SQLException {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todos ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                todos.add(extractTodoFromResultSet(rs));
            }
        }
        return todos;
    }

    public Todo getTodoById(int id) throws SQLException {
        String sql = "SELECT * FROM todos WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractTodoFromResultSet(rs);
            }
        }
        return null;
    }

    public Todo createTodo(Todo todo) throws SQLException {
        String sql = "INSERT INTO todos (title, description, priority, completed, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, todo.getTitle());
            pstmt.setString(2, todo.getDescription());
            pstmt.setString(3, todo.getPriority());
            pstmt.setBoolean(4, todo.isCompleted());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                todo.setId(rs.getInt(1));
            }
        }
        return getTodoById(todo.getId());
    }

    public Todo updateTodo(int id, Todo todo) throws SQLException {
        String sql = "UPDATE todos SET title = ?, description = ?, priority = ?, completed = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitle());
            pstmt.setString(2, todo.getDescription());
            pstmt.setString(3, todo.getPriority());
            pstmt.setBoolean(4, todo.isCompleted());
            pstmt.setInt(5, id);

            int rows = pstmt.executeUpdate();
            if (rows == 0) return null;
        }
        return getTodoById(id);
    }

    public boolean deleteTodo(int id) throws SQLException {
        String sql = "DELETE FROM todos WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Todo extractTodoFromResultSet(ResultSet rs) throws SQLException {
        Todo todo = new Todo();
        todo.setId(rs.getInt("id"));
        todo.setTitle(rs.getString("title"));
        todo.setDescription(rs.getString("description"));
        todo.setCompleted(rs.getBoolean("completed"));
        todo.setPriority(rs.getString("priority"));
        todo.setCreatedAt(rs.getTimestamp("created_at"));
        todo.setUpdatedAt(rs.getTimestamp("updated_at"));
        return todo;
    }
}