import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.ToString;
import tuples.Triple;

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
    // @EqualsAndHashCode
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

    // ------------- Print -------------

    public static void PrintSQLExecption(SQLException e) {
        System.err.println("[SQL ERROR]");
        // Stolen from JDBCTutorial by Oracle
        while (e != null) {
            System.err.println("    SQLState: " + e.getSQLState());
            System.err.println("    Error Code: " + e.getErrorCode());
            System.err.println("    Message: " + e.getMessage());
            Throwable t = e.getCause();
            while (t != null) {
                System.out.println("    Cause: " + t);
                t = t.getCause();
            }
            System.err.println();
            e = e.getNextException();
        }
        // e.printStackTrace();
    }

    private static void PrintSQL(String sql) {
        System.out.println("[STATEMENT] " + sql + ";");
    }

    private static void PrintSQL(PreparedStatement ps) {
        System.out.println("[STATEMENT] " + ps + ";");
    }

    // ------------- Execute Wrappers -------------

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

    // ------------- Utils -------------

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

    private interface RunnableSQL {
        void run() throws SQLException;
    }

    private void RunAsTransaction(RunnableSQL transaction) throws SQLException {
        RunAsTransaction(() -> {
            transaction.run();
            return null;
        });
    }

    // ------------- Add -------------

    public int AddUniversity(University university) throws SQLException {
        String sql = "INSERT INTO university VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING university_id";

        try (PreparedStatement ps = PrepareStatement(sql, university)) {
            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt("university_id");
        }
    }

    public void AddBuilding(int university_id, Building building) throws SQLException {
        String sql = "INSERT INTO building VALUES (?, ?, ?)";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, building)) {
            ExecuteUpdate(ps);
        }
    }

    public int AddDepartment(int university_id, Department department) throws SQLException {
        String sql = "INSERT INTO department VALUES (?, ?, ?, ?, ?, DEFAULT) RETURNING department_id";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department)) {
            ResultSet result = ExecuteQuery(ps);
            result.next();
            return result.getInt("department_id");
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
        try (PreparedStatement ps = PrepareStatement(sql, university_id, bulding_name, head_office, department_id)) {
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
                 PreparedStatement ps2 = PrepareStatement(sql2, university_id, specialty.code, specailty_at_university, faculty_id)) {
                ExecuteUpdate(ps1);
                ExecuteUpdate(ps2);
            }
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
                         university_id, specialty_code, study_form, subject, number_of_hours, faculty_id)) {
                ExecuteUpdate(ps1);
                ExecuteUpdate(ps2);
            }
        });
    }

    public int AddEmployee(int university_id, Employee employee) throws SQLException {
        String sql_get_id = "SELECT next_employee_id FROM university WHERE university_id = " + university_id;
        int employee_id;
        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql_get_id);
            rs.next();
            employee_id = rs.getInt(1);
        }

        RunAsTransaction(() -> {
            String sql1 = "INSERT INTO employee VALUES (?, ?, ?, ?, ?)";
            String sql2 = "UPDATE university SET next_employee_id = next_employee_id + 1 WHERE university_id = " + university_id;
            try (PreparedStatement ps = PrepareStatement(sql1, university_id, employee_id, employee);
                 Statement s = connection.createStatement()) {
                ExecuteUpdate(ps);
                ExecuteUpdate(s, sql2);
            }
        });

        return employee_id;
    }

    public void AddJob(int university_id, int department_id, int employee_id, String job_name) throws SQLException {
        String sql = "INSERT INTO job VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = PrepareStatement(sql, university_id, employee_id, job_name, department_id)) {
            ExecuteUpdate(ps);
        }
    }

    public void AddSubjectForEmployee(int university_id, int employee_id, String subject_name) throws SQLException {
        RunAsTransaction(() -> {
            String sql1 = "INSERT INTO subject VALUES (?) ON CONFLICT ON CONSTRAINT subject_pkey DO NOTHING";
            try (PreparedStatement ps = PrepareStatement(sql1, subject_name)) {
                ExecuteUpdate(ps);
            }

            String sql2 = "INSERT INTO professor VALUES (?, ?, ?)";
            try (PreparedStatement ps = PrepareStatement(sql2, university_id, employee_id, subject_name)) {
                ExecuteUpdate(ps);
            }
        });
    }

    // ------------- Selections -------------

    private Map<Integer, String> ResultSetToIdNameMap(ResultSet rs) throws SQLException {
        var map = new HashMap<Integer, String>();
        while (rs.next()) {
            map.put(rs.getInt(1), rs.getString(2));
        }
        return map;
    }

    public Map<Integer, String> GetUniversities() throws SQLException {
        String sql = "SELECT university_id, name FROM university";

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            return ResultSetToIdNameMap(rs);
        }
    }

    public int GetUniversityId(String name) throws SQLException {
        String sql = "SELECT university_id FROM university WHERE name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, name)) {
            ResultSet rs = ExecuteQuery(ps);

            rs.next();
            return rs.getInt("university_id");
        }
    }

    public University GetUniversityInfo(int university_id) throws SQLException {
        String sql = "SELECT name, url, state, campus, military FROM university WHERE university_id = " + university_id;

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            rs.next();
            return new University(rs.getString("name")).
                    Url(rs.getString("url")).
                    StateFlag(rs.getBoolean("state")).
                    CampusFlag(rs.getBoolean("campus")).
                    MilitaryFlag(rs.getBoolean("military"));
        }
    }

    public Map<Integer, String> GetDepartments(int university_id) throws SQLException {
        String sql = "SELECT department_id, name FROM department WHERE university_id = " + university_id;

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            return ResultSetToIdNameMap(rs);
        }
    }

    public Map<Integer, String> GetDepartments(int university_id, String building_name) throws SQLException {
        String sql = "SELECT department_id, name" +
                "FROM department INNER JOIN location " +
                "USING (universoty_id, department_id) " +
                "WHERE university_id = " + university_id + " AND building_name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, building_name)) {
            ResultSet rs = ExecuteQuery(ps);

            return ResultSetToIdNameMap(rs);
        }
    }

    public Map<Integer, String> GetFaculties(int university_id) throws SQLException {
        String sql = "SELECT department_id, name " +
                "FROM department INNER JOIN faculty " +
                "USING (university_id, department_id) " +
                "WHERE university_id = " + university_id;

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            return ResultSetToIdNameMap(rs);
        }
    }

    public Map<Integer, String> GetCathedras(int university_id, int faculty_id) throws SQLException {
        String sql = "SELECT department_id, name " +
                "FROM department INNER JOIN cathedra " +
                "USING (university_id, department_id) " +
                "WHERE university_id = " + university_id + " AND cathedra.faculty_id = " + faculty_id;

        try (PreparedStatement s = PrepareStatement(sql, university_id)) {
            ResultSet rs = ExecuteQuery(s);

            return ResultSetToIdNameMap(rs);
        }
    }

    public int GetDepartmentId(int university_id, String department_name) throws SQLException {
        String sql = "SELECT department_id FROM department WHERE university_id = ? AND name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_name)) {
            ResultSet rs = ExecuteQuery(ps);

            rs.next();
            return rs.getInt("department_id");
        }
    }

    public Department GetDepartmentInfo(int university_id, int department_id) throws SQLException {
        String sql = "SELECT name, url, email, headmaster_id FROM department WHERE university_id = ? AND department_id = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id)) {
            ResultSet rs = ExecuteQuery(ps);

            rs.next();
            return new Department(rs.getString("name")).
                    Url(rs.getString("url")).
                    Email(rs.getString("email")).
                    HeadmasterId(rs.getInt("headmaster_id"));
        }
    }

    public List<Building> GetBuildings(int university_id) throws SQLException {
        String sql = "SELECT name, address FROM building WHERE university_id = " + university_id;

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            var buildings = new ArrayList<Building>();
            while (rs.next()) {
                buildings.add(new Building(rs.getString("name"), rs.getString("address")));
            }
            return buildings;
        }
    }

    public Map<Building, String> GetHeadOffice(int university_id, int department_id) throws SQLException {
        String sql = "SELECT name, address, head_office " +
                "FROM building INNER JOIN location " +
                "USING (university_id, building_name) " +
                "WHERE university_id = ? AND department_id = ? AND head_office != NULL";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id)) {
            ResultSet rs = ExecuteQuery(ps);

            var map = new HashMap<Building, String>();
            while (rs.next()) {
                map.put(new Building(rs.getString("name"), rs.getString("address")),
                        rs.getString("head_office"));
            }
            return map;
        }
    }

    public Map<Building, String> GetBuildings(int university_id, int department_id) throws SQLException {
        String sql = "SELECT building_name, address, head_office " +
                "FROM building INNER JOIN location " +
                "USING (university_id, building_name)" +
                "WHERE university_id = " + university_id + " AND department_id = " + department_id;

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            var buildings = new HashMap<Building, String>();
            while (rs.next()) {
                buildings.put(
                        new Building(rs.getString("building_name"),
                                rs.getString("address")),
                        rs.getString("head_office"));
            }
            return buildings;
        }
    }

    public List<Specialty> GetSpecialties() throws SQLException {
        String sql = "SELECT specialty_code, name, qualification FROM specialty";

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            var specialties = new ArrayList<Specialty>();
            while (rs.next()) {
                specialties.add(new Specialty(rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("qualification")));
            }
            return specialties;
        }
    }

    // specialty -> faculty
    public Map<Specialty, String> GetSpecialties(int university_id) throws SQLException {
        String sql = "SELECT DISTINCT specialty_code, specialty.name, qualification, d.name " +
                "FROM specialty INNER JOIN specialty_at_university as sat" +
                "USING (specialty_code)" +
                "INNER JOIN department as d" +
                "ON sat.university_id = d.university_id AND sat.faculty_id = d.department_id" +
                "WHERE d.university_id = " + university_id;

        try (Statement s = connection.createStatement()) {
            ResultSet rs = ExecuteQuery(s, sql);

            var specialties = new HashMap<Specialty, String>();
            while (rs.next()) {
                specialties.put(new Specialty(rs.getString("specialty_code"),
                        rs.getString("specialty.name"),
                        rs.getString("qualification")),
                    rs.getString("d.name"));
            }
            return specialties;
        }
    }

    public Map<Integer, String> GetFacultiesWithSpecailty(int university_id, String specialty_code) throws SQLException {
        String sql = "SELECT DISTINCT department_id, department.name " +
                "FROM department INNER JOIN faculty" +
                "USING (university_id, department_id)" +
                "INNER JOIN specialty_at_university as sat" +
                "ON department_id = sat.faculty_id" +
                "WHERE univerity_id = ? AND specialty_code = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, specialty_code)) {
            ResultSet rs = ExecuteQuery(ps);

            return ResultSetToIdNameMap(rs);
        }
    }

    public Map<Integer, String> GetUniversitiesWithSpecailty(String specialty_code) throws SQLException {
        // using GetFacultiesWithSpecailty() sql as subquery
        String sql = "WITH faculties AS ( " +
                "SELECT DISTINCT f.university_id, department_id, department.name " +
                "FROM department INNER JOIN faculty AS f " +
                "USING (university_id, department_id) " +
                "INNER JOIN specialty_at_university AS sat " +
                "ON f.university_id = sat.university_id AND department_id = sat.faculty_id " +
                "WHERE specialty_code = ? " +
                ") " +
                "SELECT university_id, name FROM university AS u " +
                "WHERE EXISTS (SELECT * FROM faculties WHERE faculties.university_id = u.university_id);";

        try (PreparedStatement ps = PrepareStatement(sql, specialty_code)) {
            ResultSet rs = ExecuteQuery(ps);

            return ResultSetToIdNameMap(rs);
        }
    }

    public Specialty GetSpeaciltyInfo(String code) throws SQLException {
        String sql = "SELECT name, qualification FROM specailty WHERE code = ?";

        try (PreparedStatement ps = PrepareStatement(sql, code)) {
            ResultSet rs = ExecuteQuery(ps);

            rs.next();
            return new Specialty(code, rs.getString("name"), rs.getString("qualification"));
        }
    }

    public List<SpecialtyAtUniversity> GetSpecialtyAtUniversity(int university_id, int faculty_id, String specialty_code)
            throws SQLException
    {
        String sql = "SELECT study_form, months_to_study, number_of_free_places, number_of_paid_places " +
                "FROM specialty INNER JOIN specialty_at_university " +
                "USING (specailty_code) " +
                "WHERE university_id = ? AND faculty_id = ? AND specialty_code = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, faculty_id, specialty_code)) {
            ResultSet rs = ExecuteQuery(ps);

            var specialties = new ArrayList<SpecialtyAtUniversity>();
            while (rs.next()) {
                specialties.add(new SpecialtyAtUniversity(
                        rs.getString("study_form"),
                        rs.getInt("months_to_study"),
                        rs.getInt("number_of_free_places"),
                        rs.getInt("number_of_paid_places")
                ));
            }
            return specialties;
        }
    }

    // Triple of subject, study_form, number_of_hours
    public List<Triple<String, String, Integer>> GetSubjects(int university_id, int faculty_id, String specialty_code)
        throws SQLException
    {
        String sql = "SELECT subject_name, study_form, number_of_hours FROM hours " +
                "WHERE university_id = ? AND faculty_id = ? AND specialty_code = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, faculty_id, specialty_code)) {
            ResultSet rs = ExecuteQuery(ps);

            var list = new ArrayList<Triple<String, String, Integer>>();
            while (rs.next()) {
                list.add(new Triple<>(
                        rs.getString("subject_name"),
                        rs.getString("study_form"),
                        rs.getInt("number_of_hours")
                ));
            }
            return list;
        }
    }

    public Map<Integer, Employee> GetProfessors(int university_id, String subject_name) throws SQLException {
        String sql = "SELECT employee_id, first_name, last_name, patronymic " +
                "FROM professor INNER JOIN employee " +
                "USING (university_id, employee_id) " +
                "WHERE subject_name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, subject_name)) {
            ResultSet rs = ExecuteQuery(ps);

            var map = new HashMap<Integer, Employee>();
            while (rs.next()) {
                map.put(rs.getInt("employee_id"),
                        new Employee(rs.getString("first_name")).
                                LastName(rs.getString("last_name")).
                                Patronymic(rs.getString("patronymic")));
            }
            return map;
        }
    }

    public Employee GetEmployee(int university_id, int employee_id) throws SQLException {
        String sql = "SELECT first_name, last_name, patronymic " +
                "FROM employee WHERE university_id = ? AND employee_id = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, employee_id)) {
            ResultSet rs = ExecuteQuery(ps);

            rs.next();
            return new Employee(rs.getString("first_name")).
                    LastName(rs.getString("last_name")).
                    Patronymic(rs.getString("patronymic"));
        }
    }

    public Map<Integer, String> GetEmployeeJobs(int university_id, int employee_id) throws SQLException {
        String sql = "SELECT department_id, job_name FROM job WHERE university_id = ? AND employee_id = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, employee_id)) {
            ResultSet rs = ExecuteQuery(ps);

            var map = new HashMap<Integer, String>();
            while (rs.next()) {
                map.put(rs.getInt("department_id"), rs.getString("job_name"));
            }
            return map;
        }
    }

    public Map<Integer, Employee> GetEmployees(int university_id, int department_id) throws SQLException {
        String sql = "SELECT employee_id, first_name, last_name, patronymic " +
                "FROM employee INNER JOIN job " +
                "USING (university_id, employee_id) " +
                "WHERE university_id = ? AND department_id = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, department_id)) {
            ResultSet rs = ExecuteQuery(ps);

            var map = new HashMap<Integer, Employee>();
            while (rs.next()) {
                map.put(rs.getInt("employee_id"),
                        new Employee(rs.getString("first_name")).
                                LastName(rs.getString("last_name")).
                                Patronymic(rs.getString("patronymic")));
            }
            return map;
        }
    }

    // ------------- Updates -------------

    private void Update(String table, String field, Object new_value, int university_id, boolean cascade)
            throws SQLException
    {
        String sql = "UPDATE " + table + " SET " + field + " = ? WHERE university_id = ?";
        if (cascade) {
            sql += " CASCADE";
        }

        try (PreparedStatement ps = PrepareStatement(sql, new_value, university_id)) {
            ExecuteUpdate(ps);
        }
    }
    private void Update(String table, String field, Object new_value, int university_id)
            throws SQLException
    {
        Update(table, field, new_value, university_id, false);
    }

    private void Update(String table, String field, Object new_value, int university_id, int department_id, boolean cascade)
            throws SQLException
    {
        String sql = "UPDATE " + table + " SET " + field + " = ? WHERE university_id = ? AND department_id = ?";
        if (cascade) {
            sql += " CASCADE";
        }

        try (PreparedStatement ps = PrepareStatement(sql, new_value, university_id, department_id)) {
            ExecuteUpdate(ps);
        }
    }
    private void Update(String table, String field, Object new_value, int university_id, int department_id)
            throws SQLException
    {
        Update(table, field, new_value, university_id, department_id, false);
    }

    private void Update(String table, String field, Object new_value,
                        int university_id, int faculty_id, String specailty_code, String study_form,
                        boolean cascade)
            throws SQLException
    {
        String sql = "UPDATE " + table + " SET " + field + " = ? " +
                "WHERE university_id = ? AND faculty_id = ? AND specailty_code = ? AND study_form = ?";
        if (cascade) {
            sql += " CASCADE";
        }

        try (PreparedStatement ps = PrepareStatement(sql,
                new_value, university_id, faculty_id, specailty_code, study_form))
        {
            ExecuteUpdate(ps);
        }
    }
    private void Update(String table, String field, Object new_value,
                        int university_id, int faculty_id, String specailty_code, String study_form)
        throws SQLException
    {
        Update(table, field, new_value, university_id, faculty_id, specailty_code, study_form, false);
    }

    public void UpdateUniversityName(int university_id, String new_name) throws SQLException {
        Update("university", "name", new_name, university_id);
    }

    public void UpdateUniversityUrl(int university_id, String new_url) throws SQLException {
        Update("university", "url", new_url, university_id);
    }

    public void UpdateUniversityStateFlag(int university_id, boolean new_state_flag) throws SQLException {
        Update("university", "state", new_state_flag, university_id);
    }

    public void UpdateUniversityCampusFlag(int university_id, boolean new_campus_flag) throws SQLException {
        Update("university", "campus", new_campus_flag, university_id);
    }

    public void UpdateUniversityMilitaryFlag(int university_id, boolean new_military_flag) throws SQLException {
        Update("university", "military", new_military_flag, university_id);
    }

    public void UpdateBuildingName(int university_id, String new_name) throws SQLException {
        Update("building", "building_name", new_name, university_id, true);
    }

    public void UpdateBuildingAddress(int university_id, String new_address) throws SQLException {
        Update("building", "address", new_address, university_id);
    }

    public void UpdateDepartmentName(int university_id, int department_id, String new_name) throws SQLException {
        Update("department", "name", new_name, university_id, department_id);
    }

    public void UpdateDepartmentUrl(int university_id, int department_id, String new_url) throws SQLException {
        Update("department", "url", new_url, university_id, department_id);
    }

    public void UpdateDepartmentEmail(int university_id, int department_id, String new_email) throws SQLException {
        Update("department", "email", new_email, university_id, department_id);
    }

    public void UpdateDepartmentHeadmasterId(int university_id, int department_id, int new_headmster_id) throws SQLException {
        Update("department", "headmaster_id", new_headmster_id, university_id, department_id);
    }

    public void UpdateDepartmentHeadOffice(int university_id, int department_id, String building, String new_head_office)
            throws SQLException
    {
        String sql = "UPDATE location SET head_office = ? WHERE university_id = ? AND department_id = ? AND building_name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, new_head_office, university_id, department_id, building)) {
            ExecuteUpdate(ps);
        }
    }

    public void UpdateSpecialtyName(String specialty_code, String new_name) throws SQLException {
        String sql = "UPDATE specialty SET name = ? WHERE specialty_code = ?";

        try (PreparedStatement ps = PrepareStatement(sql, new_name, specialty_code)) {
            ExecuteUpdate(ps);
        }
    }

    public void UpdateSpecialtyMonthsToStudy(int university_id, int faculty_id, String specailty_code, String study_form,
                                             int new_month_to_study)
            throws SQLException
    {
        Update("specialty_at_university", "month_to_study", new_month_to_study,
                university_id, faculty_id, specailty_code, study_form);
    }

    public void UpdateSpecialtyFreePlaces(int university_id, int faculty_id, String specailty_code, String study_form,
                                          int new_free_places)
            throws SQLException
    {
        Update("specialty_at_university", "number_of_free_places", new_free_places,
                university_id, faculty_id, specailty_code, study_form);
    }

    public void UpdateSpecialtyPaidPlaces(int university_id, int faculty_id, String specailty_code, String study_form,
                                          int new_paid_places)
            throws SQLException
    {
        Update("specialty_at_university", "number_of_paid_places", new_paid_places,
                university_id, faculty_id, specailty_code, study_form);
    }

    public void UpdateHours(int university_id, int faculty_id, String specailty_code, String study_form, String subject_name,
                            int new_hours)
        throws SQLException
    {
        String sql = "UPDATE hours SET number_of_hours = ? " +
                "WHERE university_id = ? AND faculty_id = ? AND specailty_code = ? AND study_form = ? AND subject_name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, new_hours,
                university_id, faculty_id, specailty_code, study_form, subject_name))
        {
            ExecuteUpdate(ps);
        }
    }

    public void UpdateEmployeeName(int university_id, int employee_id, Employee new_employee) throws SQLException {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, partonymic = ? " +
                "WHERE university_id = ? AND employee_id = ?";

        try (PreparedStatement ps = PrepareStatement(sql, new_employee, university_id, employee_id)) {
            ExecuteUpdate(ps);
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
        });
    }

    public void DeleteUniversity(int university_id) throws SQLException {
        String sql = "DELETE FROM university WHERE university_id = " + university_id + " CASCADE";

        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, sql);
        }
    }

    public void DeleteDepartment(int university_id, int department_id) throws SQLException {
        String sql = "DELETE FROM department" +
                "WHERE university_id = " + university_id + " AND department_id = " + department_id + " CASCADE";

        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, sql);
        }
    }

    public void DeleteBuilding(int university_id, String building_name) throws SQLException {
        String sql = "DELETE FROM building" +
                "WHERE university_id = " + university_id + " AND building_name = " + building_name + " CASCADE";

        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, sql);
        }
    }

    public void DeleteBuilding(int university_id, String building_name, int departmetn_id) throws SQLException {
        String sql = "DELETE FROM location " +
                "WHERE university_id = " + university_id + " AND building_name = " + building_name +
                    " AND department_id = " + departmetn_id;

        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, sql);
        }
    }

    public void DeleteEmployee(int university_id, int employee_id) throws SQLException {
        String sql = "DELETE FROM employee " +
                "WHERE university_id = " + university_id + " AND employee_id = " + employee_id + " CASCADE";

        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, sql);
        }
    }

    public void DeleteJob(int university_id, int employee_id, String job_name) throws SQLException {
        String sql = "DELETE FROM job " +
                "WHERE university_id = " + university_id + " AND employee_id = " + employee_id + " AND job_name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, job_name)) {
            ExecuteUpdate(ps);
        }
    }

    public void DeleteProfessor(int university_id, int employee_id, String subject_name) throws SQLException {
        String sql = "DELETE FROM professor " +
                "WHERE university_id = " + university_id + " AND employee_id = " + employee_id + " AND subject_name = ?";

        try (PreparedStatement ps = PrepareStatement(sql, subject_name)) {
            ExecuteUpdate(ps);
        }
    }

    public void DeleteProfessor(int university_id, int employee_id) throws SQLException {
        String sql = "DELETE FROM professor " +
                "WHERE university_id = " + university_id + " AND employee_id = " + employee_id;

        try (Statement s = connection.createStatement()) {
            ExecuteUpdate(s, sql);
        }
    }

    public void DeleteSpecialty(String specialty_code) throws SQLException {
        String sql = "DELETE FROM specialty WHERE specialty_code = ? CASCADE";

        try (PreparedStatement ps = PrepareStatement(sql, specialty_code)) {
            ExecuteUpdate(ps);
        }
    }

    public void DeleteSpecialty(String specialty_code, int university_id) throws SQLException {
        String sql = "DELETE FROM specialty_at_university WHERE university_id = ? AND specialty_code = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, specialty_code)) {
            ExecuteUpdate(ps);
        }
    }

    public void DeleteSpecialty(String specialty_code, int university_id, String study_form) throws SQLException {
        String sql = "DELETE FROM specialty_at_university WHERE university_id = ? AND specialty_code = ? AND study_form = ?";

        try (PreparedStatement ps = PrepareStatement(sql, university_id, specialty_code, study_form)) {
            ExecuteUpdate(ps);
        }
    }

    public void DeleteSubject(String subject_name, int university_id, int faculty_id, String specialty_code) throws SQLException {
        RunAsTransaction(() -> {
            String sql1 = "DELETE FROM hours WHERE university_id = ? AND faculty_id = ? AND specialty_code = ? AND subject_name = ?";
            String sql2 = "DELETE FROM professor WHERE university_id = ? AND subject_name = ? AND " +
                    "subjec_name NOT IN (SELECT subject_name FROM hours WHERE university_id = ?)";

            try (PreparedStatement ps1 = PrepareStatement(sql1, university_id, faculty_id, specialty_code, subject_name);
                 PreparedStatement ps2 = PrepareStatement(sql2, university_id, subject_name, university_id))
            {
                ExecuteUpdate(ps1);
                ExecuteUpdate(ps2);
            }
        });
    }

    public void DeleteUnusedSubject() throws SQLException {
        RunAsTransaction(() -> {
            String sql1 = "DELETE FROM subject WHERE subject_name NOT IN (SELECT subject_name FROM hours)";
            String sql2 = "DELETE FROM professor WHERE subject_name NOT IN (SELECT subject_name FROM hours)";

            try (Statement s = connection.createStatement())
            {
                ExecuteUpdate(s, sql1);
                ExecuteUpdate(s, sql2);
            }
        });
    }
}
