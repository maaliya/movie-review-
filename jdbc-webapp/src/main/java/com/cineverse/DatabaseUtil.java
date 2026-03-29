package com.cineverse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/moviereviews";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Jdbc@mysql";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("MySQL JDBC driver not found in web application.", exception);
        }
    }

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void ensureContactMessagesTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS contact_messages (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    full_name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    subject VARCHAR(50) NOT NULL,
                    message TEXT NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }

        if (!hasContactStatusColumn(connection)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE contact_messages ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
            }
        }
    }

    public static void ensureReviewsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS movie_reviews (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    movie_name VARCHAR(100) NOT NULL,
                    rating DECIMAL(3,1) NOT NULL,
                    review_text TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static boolean hasContactStatusColumn(Connection connection) throws SQLException {
        String sql = """
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'contact_messages'
                  AND column_name = 'status'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }
}
