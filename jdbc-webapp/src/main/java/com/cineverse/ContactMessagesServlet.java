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

@WebServlet("/contact-messages-api")
public class ContactMessagesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try (Connection connection = DatabaseUtil.getConnection()) {
            DatabaseUtil.ensureContactMessagesTable(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT id, full_name, email, subject, message, status, created_at FROM contact_messages WHERE status <> 'CHECKED' ORDER BY id DESC");
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
                            .append("\"fullName\":\"").append(escapeJson(resultSet.getString("full_name"))).append("\",")
                            .append("\"email\":\"").append(escapeJson(resultSet.getString("email"))).append("\",")
                            .append("\"subject\":\"").append(escapeJson(resultSet.getString("subject"))).append("\",")
                            .append("\"message\":\"").append(escapeJson(resultSet.getString("message"))).append("\",")
                            .append("\"status\":\"").append(escapeJson(resultSet.getString("status"))).append("\",")
                            .append("\"createdAt\":\"").append(escapeJson(String.valueOf(resultSet.getTimestamp("created_at")))).append("\"")
                            .append("}");
                    first = false;
                }

                json.append("]");
                out.print(json);
            }
        } catch (SQLException exception) {
            throw new ServletException("Unable to load contact messages.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = valueOrEmpty(request.getParameter("action"));
        String idValue = valueOrEmpty(request.getParameter("id"));

        if (!"checked".equals(action)) {
            throw new ServletException("Unsupported contact message action.");
        }

        try (Connection connection = DatabaseUtil.getConnection()) {
            DatabaseUtil.ensureContactMessagesTable(connection);

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE contact_messages SET status = 'CHECKED' WHERE id = ?")) {
                statement.setInt(1, Integer.parseInt(idValue));
                statement.executeUpdate();
            }

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException exception) {
            throw new ServletException("A valid message id is required.", exception);
        } catch (SQLException exception) {
            throw new ServletException("Unable to update contact message status.", exception);
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
