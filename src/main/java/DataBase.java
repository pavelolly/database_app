import java.sql.*;
import java.util.concurrent.Callable;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.ToString;

public class DataBase implements AutoCloseable {
    private final Connection connection;

    public DataBase() throws SQLException {
        Dotenv dotenv = Dotenv.load();
        String DB_URL = dotenv.get("DB_URL");
        String USER   = dotenv.get("USER");
        String PASS   = dotenv.get("PASS");
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            PrintSQLExecption(e);
        }
    }

    // ------------- Objects -------------

    @ToString
    public static class University {
        public String name;
        public String url;
        public Boolean state    = false;
        public Boolean campus   = false;
        public Boolean military = false;

        public University(String name) {
            this.name = name;
        }

        public University Url(String url) {
            this.url = url;
            return this;
        }

        public University StateFlag(boolean state) {
            this.state = state;
            return this;
        }

        public University CampusFlag(boolean campus) {
            this.campus = campus;
            return this;
        }

        public University MilitaryFlag(boolean military) {
            this.military = military;
            return this;
        }
    }

    @ToString
    public static class Building {
        public String name;
        public String address;

        public Building(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }

    @ToString
    public static class Department {
        public String name;
        public Integer headmaster_id;
        public String url;
        public String email;

        public Department(String name) {
            this.name = name;
        }

        public Department HeadmasterId(int headmaster_id) {
            this.headmaster_id = headmaster_id;
            return this;
        }

        public Department Url(String url) {
            this.url = url;
            return this;
        }

        public Department Email(String email) {
            this.email = email;
            return this;
        }
    }

    @ToString
    public static class Employee {
        public String first_name;
        public String last_name;
        public String patronymic;

        public Employee(String first_name) {
            this.first_name = first_name;
        }

        public Employee LastName(String last_name) {
            this.last_name = last_name;
            return this;
        }

        public Employee Patronymic(String patronymic) {
            this.patronymic = patronymic;
            return this;
        }
    }

    @ToString
    public static class Specialty {
        public String code;
        public String name;
        public String qualification;

        public Specialty(String code, String name, String qualification) {
            this.code = code;
            this.name = name;
            this.qualification = qualification;
        }
    }

    @ToString
    public static class SpecialtyAtUniversity {
        public String study_form;
        public Integer month_to_study;
        public Integer free_places;
        public Integer paid_places;

        public SpecialtyAtUniversity(String study_form, Integer month_to_study, int free_places, int paid_places) {
            this.study_form = study_form;
            this.month_to_study = month_to_study;
            this.free_places = free_places;
            this.paid_places = paid_places;
        }
    }

    // ------------- Utils -------------

    public static void PrintSQLExecption(SQLException e) {
        System.err.println("[SQL ERROR] " + e.getMessage());
        e.printStackTrace();
    }

    private static void PrintSQL(String sql) {
        System.out.println("[STATEMENT] " + sql + ";");
    }

    private static void PrintSQL(PreparedStatement ps) {
        System.out.println("[STATEMENT] " + ps + ";");
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
                case University university -> {
                    ps.setString(param_idx, university.name);
                    ps.setString(param_idx + 1, university.url);
                    ps.setBoolean(param_idx + 2, university.state);
                    ps.setBoolean(param_idx + 3, university.campus);
                    ps.setBoolean(param_idx + 4, university.military);
                    param_idx += 5;
                }
                case Building building -> {
                    ps.setString(param_idx, building.name);
                    ps.setString(param_idx + 1, building.address);
                    param_idx += 2;
                }
                case Department department -> {
                    ps.setString(param_idx, department.name);
                    ps.setObject(param_idx + 1, department.headmaster_id);
                    ps.setString(param_idx + 2, department.url);
                    ps.setString(param_idx + 3, department.email);
                    param_idx += 4;
                }
                case Specialty specialty -> {
                    ps.setString(param_idx, specialty.code);
                    ps.setString(param_idx + 1, specialty.name);
                    ps.setString(param_idx + 2, specialty.qualification);
                    param_idx += 3;
                }
                case SpecialtyAtUniversity specailty_at_university -> {
                    ps.setString(param_idx, specailty_at_university.study_form);
                    ps.setInt(param_idx + 1, specailty_at_university.month_to_study);
                    ps.setInt(param_idx + 2, specailty_at_university.free_places);
                    ps.setInt(param_idx + 3, specailty_at_university.paid_places);
                    param_idx += 4;
                }
                case Employee employee -> {
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

    private <T> T RunAsTransaction(Callable<T> transaction) throws SQLException {
        try {
            connection.setAutoCommit(false);
            T result = transaction.call();
            connection.commit();
            return result;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);

            // unreachable
            return null;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ------------- Insertions -------------

    public int AddUniversity(University university) throws SQLException {
        String sql = "INSERT INTO university VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = PrepareStatement(sql, university)) {
            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt(1);
        }
    }

    public void AddBuilding(int university_id, Building building) throws SQLException {
        String sql = "INSERT INTO building VALUES (?, ?, ?)";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, building)) {
            ExecuteUpdate(ps);
        }
    }

    public int AddDepartment(int university_id, Department department) throws SQLException {
        String sql = "INSERT INTO department VALUES (?, DEFAULT, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department)) {
            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt(1);
        }
    }

    public int AddFaculty(int university_id, Department department) throws SQLException {
        return RunAsTransaction(() -> {
            int department_id = AddDepartment(university_id, department);

            String sql = "INSERT INTO faculty VALUES (?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id)) {
                ExecuteUpdate(ps);
            }
            return department_id;
        });
    }

    public int AddCathedra(int university_id, Department department, int faculty_id) throws SQLException {
        return RunAsTransaction(() -> {
            int department_id = AddDepartment(university_id, department);

            String sql = "INSERT INTO cathedra VALUES (?, ?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id, faculty_id)) {
                ExecuteUpdate(ps);
            }
            return department_id;
        });
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
                                       Specialty specialty,
                                       SpecialtyAtUniversity specailty_at_university)
            throws SQLException
    {
        RunAsTransaction(() -> {
            String sql1 = "INSERT INTO specialty VALUES (?, ?, ?) ON CONFLICT ON CONSTRAINT specialty_pkey DO NOTHING";
            String sql2 = "INSERT INTO specialty_at_university VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = PrepareStatement(sql1, specialty);
                 PreparedStatement ps2 = PrepareStatement(sql2, university_id, faculty_id, specialty.code, specailty_at_university)) {
                ExecuteUpdate(ps1);
                ExecuteUpdate(ps2);
            }
            return null;
        });
    }

    public void AddSubjectForSpecialty(int university_id, int faculty_id,
                                       String specialty_code, String study_form, String subject,
                                       int number_of_hours)
            throws SQLException
    {
        RunAsTransaction(() -> {
            String sql1 = "INSERT INTO subject VALUES (?) ON CONFLICT ON CONSTRAINT subject_pkey DO NOTHING";
            String sql2 = "INSERT INTO hours VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = PrepareStatement(sql1, subject);
                 PreparedStatement ps2 = PrepareStatement(sql2,
                         university_id, faculty_id, specialty_code, study_form, subject, number_of_hours)) {
                ExecuteUpdate(ps1);
                ExecuteUpdate(ps2);
            }
            return null;
        });
    }

    public int AddEmployee(int university_id, Employee employee) throws SQLException {
        String sql_get_id = "SELECT next_employee_id FROM university WHERE id = " + university_id;
        int employee_id;
        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql_get_id);
            rs.next();
            employee_id = rs.getInt(1);
        }

        RunAsTransaction(() -> {
            String sql1 = "INSERT INTO employee VALUES (?, ?, ?, ?, ?)";
            String sql2 = "UPDATE university SET next_employee_id = next_employee_id + 1 WHERE id = " + university_id;
            try (PreparedStatement ps = PrepareStatement(sql1, university_id, employee_id, employee);
                 Statement s = connection.createStatement())
            {
                ExecuteUpdate(ps);
                ExecuteUpdate(s, sql2);
            }
            return null;
        });

        return employee_id;
    }

    public void AddJob(int university_id, int department_id, int employee_id, String job) throws SQLException {
        String sql = "INSERT INTO job VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id, employee_id, job)) {
            ExecuteUpdate(ps);
        }
    }

    public void AddSubjectForEmployee(int university_id, int employee_id, String subject) throws SQLException {
        RunAsTransaction(() -> {
            String sql1 = "INSERT INTO subject VALUES (?) ON CONFLICT ON CONSTRAINT subject_pkey DO NOTHING";
            try (PreparedStatement ps = PrepareStatement(sql1, subject)) {
                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO professor VALUES (?, ?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql2, university_id, employee_id, subject)) {
                ExecuteUpdate(ps);
            }
            return null;
        });
    }


    // ------------- Selections -------------

    int GetDepartmentId(int university_id, String name) throws SQLException {
        String sql = "SELECT id FROM department WHERE university_id = ? AND name = ?";
        try (PreparedStatement ps = PrepareStatement(sql, university_id, name)) {
            return ExecuteQuery(ps).getInt("id");
        }
    }

    // ------------- Deletions -------------

    public void TruncateEverything() throws SQLException {
        RunAsTransaction(() -> {
            try (Statement s = connection.createStatement()) {
                ExecuteUpdate(s, "TRUNCATE TABLE university CASCADE");
                ExecuteUpdate(s, "TRUNCATE TABLE subject CASCADE");
                ExecuteUpdate(s, "TRUNCATE TABLE specialty CASCADE");
            }
            return null;
        });
    }
}
