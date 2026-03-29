package com.cineverse;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/contact-message")
public class ContactMessageServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String fullName = valueOrEmpty(request.getParameter("fullname"));
        String email = valueOrEmpty(request.getParameter("email"));
        String subject = valueOrEmpty(request.getParameter("subject"));
        String message = valueOrEmpty(request.getParameter("message"));

        if (fullName.isBlank() || email.isBlank() || subject.isBlank() || message.isBlank()) {
            response.sendRedirect("contact.html");
            return;
        }

        try (Connection connection = DatabaseUtil.getConnection()) {
            DatabaseUtil.ensureContactMessagesTable(connection);

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO contact_messages (full_name, email, subject, message, status) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, fullName);
                statement.setString(2, email);
                statement.setString(3, subject);
                statement.setString(4, message);
                statement.setString(5, "PENDING");
                statement.executeUpdate();
            }

            response.sendRedirect("contact.html?status=sent");
        } catch (SQLException exception) {
            throw new ServletException("Unable to save contact message.", exception);
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
