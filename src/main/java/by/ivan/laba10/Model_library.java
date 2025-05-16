package by.ivan.laba10;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Model_library implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(Model_library.class.getName());
    private static final String URL = "jdbc:postgresql://localhost:5432/library_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    private Connection connection;

    public Model_library() throws SQLException {
        try {
            // Явная регистрация драйвера
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Connected to database successfully");
            createTableIfNotExists();
        } catch (ClassNotFoundException e) {
            logger.severe("PostgreSQL JDBC Driver not found");
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        } catch (SQLException e) {
            logger.severe("Database connection failed: " + e.getMessage());
            throw e;
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS books (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(255) NOT NULL,
                publisher VARCHAR(255),
                year INTEGER,
                pages INTEGER
            )""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("year"),
                        rs.getInt("pages")
                ));
            }
        }
        return books;
    }

    public void addBook(Book book) throws SQLException {
        String sql = """
            INSERT INTO books (title, author, publisher, year, pages)
            VALUES (?, ?, ?, ?, ?) RETURNING id""";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setInt(4, book.getYear());
            stmt.setInt(5, book.getPages());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    book.setId(rs.getInt(1));
                }
            }
        }
    }

    public void updateBook(Book book) throws SQLException {
        String sql = """
            UPDATE books SET 
                title = ?, 
                author = ?, 
                publisher = ?, 
                year = ?, 
                pages = ? 
            WHERE id = ?""";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setInt(4, book.getYear());
            stmt.setInt(5, book.getPages());
            stmt.setInt(6, book.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Book not found with id: " + book.getId());
            }
        }
    }

    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Book not found with id: " + id);
            }
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}