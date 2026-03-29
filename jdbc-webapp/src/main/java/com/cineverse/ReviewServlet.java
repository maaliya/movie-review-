package com.cineverse;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet("/reviews-api")
public class ReviewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try (Connection connection = DatabaseUtil.getConnection()) {
            DatabaseUtil.ensureReviewsTable(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT id, movie_name, rating, review_text FROM movie_reviews ORDER BY id DESC");
                 PrintWriter out = response.getWriter()) {

                StringBuilder json = new StringBuilder();
                json.append("[");
                boolean first = true;

                while (resultSet.next()) {
                    if (!first) {
                        json.append(",");
                    }

                    json.append("{")
                            .append("\"id\":").append(resultSet.getInt("id")).append(",")
                            .append("\"movieName\":\"").append(escapeJson(resultSet.getString("movie_name"))).append("\",")
                            .append("\"rating\":").append(resultSet.getDouble("rating")).append(",")
                            .append("\"reviewText\":\"").append(escapeJson(resultSet.getString("review_text"))).append("\"")
                            .append("}");
                    first = false;
                }

                json.append("]");
                out.print(json);
            }
        } catch (SQLException exception) {
            throw new ServletException("Unable to load reviews.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = valueOrEmpty(request.getParameter("action"));
        String idValue = valueOrEmpty(request.getParameter("id"));
        String movieName = valueOrEmpty(request.getParameter("movieName"));
        String ratingValue = valueOrEmpty(request.getParameter("rating"));
        String reviewText = valueOrEmpty(request.getParameter("reviewText"));

        try (Connection connection = DatabaseUtil.getConnection()) {
            DatabaseUtil.ensureReviewsTable(connection);

            switch (action) {
                case "create" -> createReview(connection, movieName, ratingValue, reviewText);
                case "update" -> updateReview(connection, idValue, movieName, ratingValue, reviewText);
                case "delete" -> deleteReview(connection, idValue);
                default -> throw new ServletException("Unsupported review action: " + action);
            }

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            throw new ServletException("Unable to process review action.", exception);
        }
    }

    private void createReview(Connection connection, String movieName, String ratingValue, String reviewText)
            throws SQLException, ServletException {
        validateReview(movieName, ratingValue, reviewText);

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO movie_reviews (movie_name, rating, review_text) VALUES (?, ?, ?)")) {
            statement.setString(1, movieName);
            statement.setDouble(2, Double.parseDouble(ratingValue));
            statement.setString(3, reviewText);
            statement.executeUpdate();
        }
    }

    private void updateReview(Connection connection, String idValue, String movieName, String ratingValue, String reviewText)
            throws SQLException, ServletException {
        validateReview(movieName, ratingValue, reviewText);
        int id = parseId(idValue);

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE movie_reviews SET movie_name = ?, rating = ?, review_text = ? WHERE id = ?")) {
            statement.setString(1, movieName);
            statement.setDouble(2, Double.parseDouble(ratingValue));
            statement.setString(3, reviewText);
            statement.setInt(4, id);
            statement.executeUpdate();
        }
    }

    private void deleteReview(Connection connection, String idValue) throws SQLException, ServletException {
        int id = parseId(idValue);

        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM movie_reviews WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void validateReview(String movieName, String ratingValue, String reviewText) throws ServletException {
        if (movieName.isBlank() || ratingValue.isBlank() || reviewText.isBlank()) {
            throw new ServletException("Movie name, rating, and review text are required.");
        }

        double rating;
        try {
            rating = Double.parseDouble(ratingValue);
        } catch (NumberFormatException exception) {
            throw new ServletException("Rating must be a valid number.", exception);
        }

        if (rating < 0 || rating > 10) {
            throw new ServletException("Rating must be between 0 and 10.");
        }
    }

    private int parseId(String idValue) throws ServletException {
        try {
            return Integer.parseInt(idValue);
        } catch (NumberFormatException exception) {
            throw new ServletException("A valid review id is required.", exception);
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeJson(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
