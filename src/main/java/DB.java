import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {
    private final Connection connection;
    private final static String DB_URL = "jdbc:postgresql://localhost:5432/universities";
    private final static String USER = "postgres";
    private final static String PASS = "S3ptAnd69postgres";

    public DB() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // ------------- Utils -------------

    public static void PrintSQLExecption(SQLException e) {
        System.err.println("[SQL ERROR]: " + e.getMessage());
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

    // ------------- Insertions -------------

    public int AddUniversity(DBObject.University university) throws SQLException {
        String sql = "INSERT INTO university VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, university.name);
            ps.setString(2, university.url);
            ps.setBoolean(3, university.state);
            ps.setBoolean(4, university.campus);
            ps.setBoolean(5, university.military);

            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt(1);
        }
    }

    public void AddBuilding(int university_id, DBObject.Building building) throws SQLException {
        String sql = "INSERT INTO building VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, university_id);
            ps.setString(2, building.name);
            ps.setString(3, building.address);

            ExecuteUpdate(ps);
        }
    }

    public int AddDepartment(int university_id, DBObject.Department department) throws SQLException {
        String sql = "INSERT INTO department VALUES (?, DEFAULT, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, university_id);
            ps.setString(2, department.name);
            ps.setObject(3, department.headmaster_id);
            ps.setString(4, department.url);
            ps.setString(4, department.email);

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
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, university_id);
                ps.setInt(2, department_id);

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
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, university_id);
                ps.setInt(2, department_id);
                ps.setInt(3, faculty_id);

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
        String sql = "INSERT INTO location VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, university_id);
            ps.setInt(2, department_id);
            ps.setString(3, bulding_name);
            ps.setString(4, head_office);

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
                                       DBObject.SpecailtyAtUniversity specailty_at_university)
            throws SQLException
    {
        try {
            connection.setAutoCommit(false);

            String sql1 = "INSERT INTO specialty VALUES (?, ?, ?) ON CONFLICT ON CONSTRAINT specialty_pkey DO NOTHING";
            try (PreparedStatement ps = connection.prepareStatement(sql1)) {
                ps.setString(1, specialty.code);
                ps.setString(2, specialty.name);
                ps.setString(3, specialty.qualification);

                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO specialty_at_university VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql2)) {
                ps.setInt(1, university_id);
                ps.setInt(2, faculty_id);
                ps.setString(3, specialty.code);
                ps.setString(4, specailty_at_university.study_form);
                ps.setInt(5, specailty_at_university.month_to_study);
                ps.setInt(6, specailty_at_university.number_of_free_places);
                ps.setInt(7, specailty_at_university.number_of_paid_places);

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
            try (PreparedStatement ps = connection.prepareStatement(sql1)) {
                ps.setString(1, subject);

                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO hours VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql2)) {
                ps.setInt(1, university_id);
                ps.setInt(2, faculty_id);
                ps.setString(3, specialty_code);
                ps.setString(4, study_form);
                ps.setString(5, subject);
                ps.setInt(6, number_of_hours);

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

    public int AddEmployee(int university_id, DBObject.Employee employee) throws SQLException {
        String sql = "INSERT INTO employee VALUES (?, DEFAULT, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, university_id);
            ps.setString(2, employee.first_name);
            ps.setString(3, employee.last_name);
            ps.setString(4, employee.patronymic);

            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt(1);
        }
    }

    public void AddJob(int university_id, int department_id, int employee_id, String job) throws SQLException {
        String sql = "INSERT INTO job VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, university_id);
            ps.setInt(2, department_id);
            ps.setInt(3, employee_id);
            ps.setString(4, job);

            ExecuteUpdate(ps);
        }
    }

    public void AddSubjectToEmployee(int university_id, int employee_id, String subject) throws SQLException {
        try {
            connection.setAutoCommit(false);
            String sql1 = "INSERT INTO subject VALUES (?) ON CONFLICT ON CONSTRAINT subject_pkey DO NOTHING";
            try (PreparedStatement ps = connection.prepareStatement(sql1)) {
                ps.setString(1, subject);

                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO professor VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql2)) {
                ps.setInt(1, university_id);
                ps.setInt(2, employee_id);
                ps.setString(3, subject);

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
