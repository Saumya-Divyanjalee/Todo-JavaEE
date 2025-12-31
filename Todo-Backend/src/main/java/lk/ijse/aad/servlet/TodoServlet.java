package lk.ijse.aad.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.aad.dao.TodoDAO;
import lk.ijse.aad.model.Todo;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/todos/*")
public class TodoServlet extends HttpServlet {
    private TodoDAO todoDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        todoDAO = new TodoDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setJsonResponse(resp);

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all todos
                List<Todo> todos = todoDAO.getAllTodos();
                resp.getWriter().write(gson.toJson(todos));
            } else {
                // Get specific todo
                int id = Integer.parseInt(pathInfo.substring(1));
                Todo todo = todoDAO.getTodoById(id);

                if (todo != null) {
                    resp.getWriter().write(gson.toJson(todo));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Todo not found\"}");
                }
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setJsonResponse(resp);

        try {
            Todo todo = gson.fromJson(req.getReader(), Todo.class);
            Todo createdTodo = todoDAO.createTodo(todo);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(createdTodo));
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setJsonResponse(resp);

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Todo ID required\"}");
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));
            Todo todo = gson.fromJson(req.getReader(), Todo.class);
            Todo updatedTodo = todoDAO.updateTodo(id, todo);

            if (updatedTodo != null) {
                resp.getWriter().write(gson.toJson(updatedTodo));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Todo not found\"}");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setJsonResponse(resp);

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Todo ID required\"}");
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));
            boolean deleted = todoDAO.deleteTodo(id);

            if (deleted) {
                resp.getWriter().write("{\"message\": \"Todo deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Todo not found\"}");
            }
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private void setJsonResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setJsonResponse(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleError(HttpServletResponse resp, Exception e) throws IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        e.printStackTrace();
    }
}