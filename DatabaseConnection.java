import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/moviereviews";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Jdbc@mysql";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("JDBC connection established successfully.");
            System.out.println();
            System.out.println("Movie Reviews:");
            System.out.println("-------------");

            String query = "SELECT id, movie_name, rating, review_text FROM reviews";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    System.out.println("ID: " + resultSet.getInt("id"));
                    System.out.println("Movie: " + resultSet.getString("movie_name"));
                    System.out.println("Rating: " + resultSet.getDouble("rating"));
                    System.out.println("Review: " + resultSet.getString("review_text"));
                    System.out.println();
                }
            }
        } catch (SQLException exception) {
            System.out.println("Failed to connect to the database.");
            exception.printStackTrace();
        }
    }
}
