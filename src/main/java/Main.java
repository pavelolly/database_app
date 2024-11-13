import com.raylib.java.Raylib;
import com.raylib.java.core.Color;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void PrintDb() {
        String url = "jdbc:postgresql://localhost:5432/db";
        String user = "postgres";
        String password = "S3ptAnd69postgres";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM books";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getString("book_name") + ", " +
                        rs.getString("publisher") + ", " +
                        rs.getString("price"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Raylib raylib = new Raylib(800, 600, "Hello from Java and Raylib, it finally works!!");

        while (!raylib.core.WindowShouldClose()) {
            raylib.core.BeginDrawing();

                raylib.core.ClearBackground(new Color(0x18, 0x18, 0x18, 0xff));

            raylib.core.EndDrawing();
        }

        raylib.core.CloseWindow();
    }
}