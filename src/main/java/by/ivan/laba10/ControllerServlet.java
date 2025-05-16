package by.ivan.laba10;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet(name = "ControllerServlet", value = "/books/*")
public class ControllerServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ControllerServlet.class.getName());
    private static final Gson gson = new Gson();
    private Model_library model;

    @Override
    public void init() {
        try {
            model = new Model_library();
            logger.info("Library model initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize library model: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try {
            resp.getWriter().write(gson.toJson(model.getAllBooks()));
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            Book book = gson.fromJson(reader, Book.class);
            if (!isValidBook(book)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid book data");
                return;
            }

            model.addBook(book);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(book));
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add book");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            Book book = gson.fromJson(reader, Book.class);
            if (!isValidBook(book)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid book data");
                return;
            }

            model.updateBook(book);
            resp.getWriter().write(gson.toJson(book));
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update book");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing book ID");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            model.deleteBook(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete book");
        }
    }

    private boolean isValidBook(Book book) {
        return book != null &&
                book.getTitle() != null && !book.getTitle().isEmpty() &&
                book.getAuthor() != null && !book.getAuthor().isEmpty();
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"" + message + "\"}");
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        logger.warning("Error " + status + ": " + message);
    }
}