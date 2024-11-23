import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            DB db = new DB();

            db.TruncateEverything();
        } catch (SQLException e) {
            DB.PrintSQLExecption(e);
        }
    }
}