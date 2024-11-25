import java.sql.*;
import java.util.concurrent.Callable;

public class DB implements AutoCloseable {
    private final Connection connection;
    private final static String DB_URL = "jdbc:postgresql://localhost:5432/universities";
    private final static String USER = "postgres";
    private final static String PASS = "S3ptAnd69postgres";

    public DB() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            PrintSQLExecption(e);
        }
    }

    // ------------- Utils -------------

    public static void PrintSQLExecption(SQLException e) {
        System.err.println("[SQL ERROR]: " + e.getMessage());
        e.printStackTrace();
    }

    private static void PrintSQL(String sql) {
        System.out.println("[STATEMENT]: " + sql + ";");
    }

    private static void PrintSQL(PreparedStatement ps) {
        System.out.println("[STATEMENT]: " + ps + ";");
    }

    private boolean Execute(Statement s, String sql) throws SQLException {
        PrintSQL(sql);
        return s.execute(sql);
    }

    private int ExecuteUpdate(Statement s, String sql) throws SQLException {
        PrintSQL(sql);
        return s.executeUpdate(sql);
    }

    private ResultSet ExecuteQuery(Statement s, String sql) throws SQLException {
        PrintSQL(sql);
        return s.executeQuery(sql);
    }

    private boolean Execute(PreparedStatement ps) throws SQLException {
        PrintSQL(ps);
        return ps.execute();
    }

    private int ExecuteUpdate(PreparedStatement ps) throws SQLException {
        PrintSQL(ps);
        return ps.executeUpdate();
    }

    private ResultSet ExecuteQuery(PreparedStatement ps) throws SQLException {
        PrintSQL(ps);
        return ps.executeQuery();
    }

    private PreparedStatement PrepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        int param_idx = 1;
        for (Object param : params) {
            switch (param) {
                case DBObject.University university -> {
                    ps.setString(param_idx, university.name);
                    ps.setString(param_idx + 1, university.url);
                    ps.setBoolean(param_idx + 2, university.state);
                    ps.setBoolean(param_idx + 3, university.campus);
                    ps.setBoolean(param_idx + 4, university.military);
                    param_idx += 5;
                }
                case DBObject.Building building -> {
                    ps.setString(param_idx, building.name);
                    ps.setString(param_idx + 1, building.address);
                    param_idx += 2;
                }
                case DBObject.Department department -> {
                    ps.setString(param_idx, department.name);
                    ps.setObject(param_idx + 1, department.headmaster_id);
                    ps.setString(param_idx + 2, department.url);
                    ps.setString(param_idx + 3, department.email);
                    param_idx += 4;
                }
                case DBObject.Specialty specialty -> {
                    ps.setString(param_idx, specialty.code);
                    ps.setString(param_idx + 1, specialty.name);
                    ps.setString(param_idx + 2, specialty.qualification);
                    param_idx += 3;
                }
                case DBObject.SpecialtyAtUniversity specailty_at_university -> {
                    ps.setString(param_idx, specailty_at_university.study_form);
                    ps.setInt(param_idx + 1, specailty_at_university.month_to_study);
                    ps.setInt(param_idx + 2, specailty_at_university.number_of_free_places);
                    ps.setInt(param_idx + 3, specailty_at_university.number_of_paid_places);
                    param_idx += 4;
                }
                case DBObject.Employee employee -> {
                    ps.setString(param_idx, employee.first_name);
                    ps.setString(param_idx + 1, employee.last_name);
                    ps.setString(param_idx + 2, employee.patronymic);
                    param_idx += 3;
                }
                case null, default -> {
                    ps.setObject(param_idx, param);
                    param_idx += 1;
                }
            }
        }
        return ps;
    }

    void RunAsTransaction(Callable<Void> transaction) throws SQLException {
        try {
            connection.setAutoCommit(false);
            transaction.call();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            System.err.println("[ERROR]: " + e.getMessage());
            System.exit(1);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ------------- Insertions -------------

    public int AddUniversity(DBObject.University university) throws SQLException {
        String sql = "INSERT INTO university VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = PrepareStatement(sql, university)) {
            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt(1);
        }
    }

    public void AddBuilding(int university_id, DBObject.Building building) throws SQLException {
        String sql = "INSERT INTO building VALUES (?, ?, ?)";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, building)) {
            ExecuteUpdate(ps);
        }
    }

    public int AddDepartment(int university_id, DBObject.Department department) throws SQLException {
        String sql = "INSERT INTO department VALUES (?, DEFAULT, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department)) {
            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt(1);
        }
    }

    public int AddFaculty(int university_id, DBObject.Department department) throws SQLException {
        try {
            connection.setAutoCommit(false);

            int department_id = AddDepartment(university_id, department);

            String sql = "INSERT INTO faculty VALUES (?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id)) {
                ExecuteUpdate(ps);
            }
            connection.commit();
            return department_id;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public int AddCathedra(int university_id, DBObject.Department department, int faculty_id) throws SQLException {
        try {
            connection.setAutoCommit(false);

            int department_id = AddDepartment(university_id, department);

            String sql = "INSERT INTO cathedra VALUES (?, ?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id, faculty_id)) {
                ExecuteUpdate(ps);
            }
            connection.commit();
            return department_id;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void LocateDepartmentAtBuilding(int university_id, int department_id, String bulding_name, String head_office)
            throws SQLException
    {
        String sql = "INSERT INTO location VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id, bulding_name, head_office)) {
            ExecuteUpdate(ps);
        }
    }

    public void LocateDepartmentAtBuilding(int university_id, int department_id, String bulding_name)
        throws SQLException
    {
        LocateDepartmentAtBuilding(university_id, department_id, bulding_name, null);
    }

    public void AddSpecialtyForFaculty(int university_id,
                                       int faculty_id,
                                       DBObject.Specialty specialty,
                                       DBObject.SpecialtyAtUniversity specailty_at_university)
            throws SQLException
    {
        try {
            connection.setAutoCommit(false);

            String sql1 = "INSERT INTO specialty VALUES (?, ?, ?) ON CONFLICT ON CONSTRAINT specialty_pkey DO NOTHING";
            try (PreparedStatement ps = PrepareStatement(sql1, specialty)) {
                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO specialty_at_university VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql2, university_id, faculty_id, specialty.code, specailty_at_university)) {
                ExecuteUpdate(ps);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void AddSubjectForSpecialty(int university_id, int faculty_id,
                                       String specialty_code, String study_form, String subject,
                                       int number_of_hours)
            throws SQLException
    {
        try {
            connection.setAutoCommit(false);

            String sql1 = "INSERT INTO subject VALUES (?) ON CONFLICT ON CONSTRAINT subject_pkey DO NOTHING";
            String sql2 = "INSERT INTO hours VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = PrepareStatement(sql1, subject);
                 PreparedStatement ps2 = PrepareStatement(sql2,
                         university_id, faculty_id, specialty_code, study_form, subject, number_of_hours)) {
                ExecuteUpdate(ps1);
                ExecuteUpdate(ps2);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public int AddEmployee(int university_id, DBObject.Employee employee) throws SQLException {
        String sql_get_id = "SELECT next_employee_id FROM university WHERE id = " + university_id;
        int employee_id;
        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql_get_id);
            rs.next();
            employee_id = rs.getInt(1);
        }

        try {
            connection.setAutoCommit(false);

            String sql1 = "INSERT INTO employee VALUES (?, ?, ?, ?, ?)";
            String sql2 = "UPDATE university SET next_employee_id = next_employee_id + 1 WHERE id = " + university_id;

            try (PreparedStatement ps = PrepareStatement(sql1, university_id, employee_id, employee);
                 Statement s = connection.createStatement())
            {
                ExecuteUpdate(ps);
                ExecuteUpdate(s, sql2);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }

        return employee_id;
    }

    public void AddJob(int university_id, int department_id, int employee_id, String job) throws SQLException {
        String sql = "INSERT INTO job VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id, employee_id, job)) {
            ExecuteUpdate(ps);
        }
    }

    public void AddSubjectForEmployee(int university_id, int employee_id, String subject) throws SQLException {
        try {
            connection.setAutoCommit(false);
            String sql1 = "INSERT INTO subject VALUES (?) ON CONFLICT ON CONSTRAINT subject_pkey DO NOTHING";
            try (PreparedStatement ps = PrepareStatement(sql1, subject)) {
                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO professor VALUES (?, ?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql2, university_id, employee_id, subject);) {
                ExecuteUpdate(ps);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }


    // ------------- Selections -------------

    int GetDepartmentId(int university_id, String name) throws SQLException {
        String sql = "SELECT id FROM department WHERE university_id = ? AND name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, university_id);
            ps.setString(2, name);

            return ps.executeQuery().getInt("id");
        }
    }

    // ------------- Deletions -------------

    public void TruncateEverything() throws SQLException {
        connection.setAutoCommit(false);
        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, "TRUNCATE TABLE university CASCADE");
            ExecuteUpdate(s, "TRUNCATE TABLE subject CASCADE");
            ExecuteUpdate(s, "TRUNCATE TABLE specialty CASCADE");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
