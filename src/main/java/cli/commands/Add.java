package cli.commands;

import app.DataBase;
import picocli.CommandLine.*;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Command(name = "add", mixinStandardHelpOptions = true,
         subcommands = { Add.University.class,
                         Add.Department.class,
                         Add.Building.class,
                         Add.Locate.class,
                         Add.Specialty.class,
                         Add.Subject.class,
                         Add.Employee.class,
                         Add.Job.class,
                         Add.Professor.class })
public class Add extends AbstractCommand {
    public Add(DataBase db) {
        super(db);
    }

    private static void PrintNewId(String message, int id) {
        System.out.println("[NEW ID] " + id + ": " + message);
    }


    @Command(name = "university", mixinStandardHelpOptions = true)
    public static class University implements Callable<Integer> {
        @Spec Model.CommandSpec spec;

        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Option(names = { "--url" })
        private String url;

        @Option(names = { "-s", "--state" })
        private String state_flag;

        @Option(names = { "-c", "--campus" })
        private String campus_flag;

        @Option(names = { "-m", "--military" })
        private String military_flag;

        private boolean ValidateFlag(String flag_name, String flag_value) {
            if (!flag_value.equalsIgnoreCase("true") && !flag_value.equalsIgnoreCase("false")) {
                throw new ParameterException(spec.commandLine(),
                        String.format("Invalid value for flag %s: %s", flag_name, flag_value));
            }
            return flag_value.equalsIgnoreCase("true");
        }

        @Override
        public Integer call() throws SQLException, ParameterException {
            int new_id = db.AddUniversity(
                    new DataBase.University(name).
                        Url(url).
                        StateFlag(ValidateFlag("--state", state_flag)).
                        CampusFlag(ValidateFlag("--campus", campus_flag)).
                        MilitaryFlag(ValidateFlag("--military", military_flag)));

            PrintNewId("Added new university", new_id);

            return 0;
        }
    }

    @Command(name = "department", mixinStandardHelpOptions = true)
    public static class Department implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Option(names = { "--url" })
        private String url;

        @Option(names = { "--email" })
        private String email;

        @Option(names = { "-h", "--headmaster" })
        private Integer headmster_id;

        @ArgGroup
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = { "-f", "--faculty" }, required = true)
            private Boolean faculty;

            @Option(names = { "-c", "--cathedra-for" }, required = true)
            private Integer faculty_id;
        }

        @Override
        public Integer call() throws Exception {
            var department = new DataBase.Department(name).Url(url).Email(email).HeadmasterId(headmster_id);

            if (exclusive == null) {
                int new_id = db.AddDepartment(university_id, department);
                PrintNewId("Added new department", new_id);
                return 0;
            }

            if (exclusive.faculty != null) {
                int new_id = db.AddFaculty(university_id, department);
                PrintNewId("Added new faculty", new_id);
                return 0;
            }

            if (exclusive.faculty_id != null) {
                int new_id = db.AddCathedra(university_id, department, exclusive.faculty_id);
                PrintNewId("Added new cathedra for faculty " + exclusive.faculty_id, new_id);
                return 0;
            }

            return 0;
        }
    }

    @Command(name = "building", mixinStandardHelpOptions = true)
    public static class Building implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Option(names = { "-a", "--address" }, required = true)
        private String address;

        @Override
        public Integer call() throws SQLException {
            db.AddBuilding(university_id, new DataBase.Building(name, address));
            return 0;
        }
    }

    @Command(name = "locate", mixinStandardHelpOptions = true)
    public static class Locate implements Callable<Integer> {

        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-d", "--department" }, required = true)
        private Integer department_id;

        @Option(names = { "-b", "--building" }, required = true)
        private String bulding_name;

        @Option(names = { "--head-office" })
        private String head_office;

        @Override
        public Integer call() throws SQLException {
            db.LocateDepartmentAtBuilding(university_id, department_id, bulding_name, head_office);
            return 0;
        }
    }

    // !!--------<<< THIS IS SO BAD >>>--------!!
    @Command(name = "specialty", mixinStandardHelpOptions = true)
    public static class Specialty implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-f", "--faculty" }, required = true)
        private Integer faculty_id;

        @Option(names = { "-c", "--code" }, required = true)
        private String specialty_code;

        @Option(names = { "-n", "--name" }, required = true)
        private String specialty_name;

        @Option(names = { "-q", "--quialification" }, required = true)
        private String specialty_qualification;

        @Option(names = { "-s", "--study-form" }, required = true)
        private String specialty_study_form;

        @Option(names = { "-m", "--months-to-study" }, required = true)
        private Integer specialty_month_to_study;

        @Option(names = { "--free-places" }, required = true)
        private Integer specialty_free_places;

        @Option(names = { "--paid-places" }, required = true)
        private Integer specialty_paid_places;

        @Override
        public Integer call() throws SQLException {
            db.AddSpecialtyForFaculty(university_id, faculty_id,
                    new DataBase.Specialty(specialty_code, specialty_name, specialty_qualification),
                    new DataBase.SpecialtyAtUniversity(specialty_study_form, specialty_month_to_study, specialty_free_places, specialty_paid_places));

            return 0;
        }
    }

    @Command(name = "subject", mixinStandardHelpOptions = true)
    public static class Subject implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-f", "--faculty" }, required = true)
        private Integer faculty_id;

        @Option(names = { "-c", "--code" }, required = true)
        private String specialty_code;

        @Option(names = { "--study-form" }, required = true)
        private String study_form;

        @Option(names = { "-s", "--subject" }, required = true)
        private String subject_name;

        @Option(names = { "--hours" }, required = true)
        private Integer hours;

        @Override
        public Integer call() throws Exception {
            db.AddSubjectForSpecialty(university_id, faculty_id, specialty_code, study_form, subject_name, hours);
            return 0;
        }
    }

    @Command(name = "employee", mixinStandardHelpOptions = true)
    public static class Employee implements Callable<Integer> {

        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-f", "--first-name" }, required = true)
        private String first_name;

        @Option(names = { "-l", "--last-name" })
        private String last_name;

        @Option(names = { "-p", "--patronymic" })
        private String patronymic;

        @Override
        public Integer call() throws SQLException {
            int new_id = db.AddEmployee(university_id, new DataBase.Employee(first_name).
                            LastName(last_name).
                            Patronymic(patronymic));

            PrintNewId("Added new employee", new_id);

            return 0;
        }
    }

    @Command(name = "job", mixinStandardHelpOptions = true)
    public static class Job implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-d", "--department" }, required = true)
        private Integer department_id;

        @Option(names = { "-e", "--employee" }, required = true)
        private Integer employee_id;

        @Option(names = { "-j", "--job" }, required = true)
        private String job_name;

        @Override
        public Integer call() throws SQLException {
            db.AddJob(university_id, department_id, employee_id, job_name);
            return 0;
        }
    }

    @Command(name = "professor", mixinStandardHelpOptions = true)
    public static class Professor implements Callable<Integer> {

        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-e", "--employee" }, required = true)
        private Integer employee_id;

        @Option(names = { "-s", "--subject" }, required = true)
        private String subject_name;

        @Override
        public Integer call() throws SQLException {
            db.AddProfessor(university_id, employee_id, subject_name);
            return 0;
        }
    }
}
