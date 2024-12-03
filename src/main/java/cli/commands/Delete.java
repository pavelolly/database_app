package cli.commands;

import app.DataBase;
import picocli.CommandLine.*;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Command(name = "delete",
         subcommands = { Delete.University.class,
                         Delete.Department.class,
                         Delete.Building.class,
                         Delete.Employee.class,
                         Delete.Specialty.class,
                         Delete.Subject.class })
public class Delete extends AbstractCommand implements Callable<Integer> {
    public Delete(DataBase db) {
        super(db);
    }

    @Override
    public Integer call() throws SQLException {
        db.TruncateEverything();
        return 0;
    }

    @Command(name = "university")
    public static class University implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Override
        public Integer call() throws SQLException {
            db.DeleteUniversity(university_id);
            return 0;
        }
    }

    @Command(name = "department")
    public static class Department implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-d", "--department" }, required = true)
        private Integer department_id;

        @Override
        public Integer call() throws SQLException {
            db.DeleteDepartment(university_id, department_id);
            return 0;
        }
    }

    @Command(name = "building")
    public static class Building implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-b", "--building"}, required = true)
        private String building_name;

        @Option(names = { "-d", "--department" })
        private Integer department_id;

        @Override
        public Integer call() throws SQLException {
            if (department_id == null) {
                db.DeleteBuilding(university_id, building_name);
                return 0;
            }

            db.DeleteBuilding(university_id, building_name, department_id);
            return 0;
        }
    }

    @Command(name = "employee")
    public static class Employee implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-e", "--employee" }, required = true)
        private Integer employee_id;

        @ArgGroup
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = { "-j", "--job" }, required = true)
            private String job_name;

            @Option(names = { "-", "--subject" }, required = true)
            private String subject;

            @Option(names = { "-", "--professor" }, required = true)
            private Boolean professor;
        }

        @Override
        public Integer call() throws SQLException {
            if (exclusive == null) {
                db.DeleteEmployee(university_id, employee_id);
                return 0;
            }

            if (exclusive.job_name != null) {
                db.DeleteJob(university_id, employee_id, exclusive.job_name);
                return 0;
            }

            if (exclusive.subject != null) {
                db.DeleteProfessor(university_id, employee_id, exclusive.subject);
                return 0;
            }

            if (exclusive.professor) {
                db.DeleteProfessor(university_id, employee_id);
                return 0;
            }

            return 0;
        }
    }

    @Command(name = "specailty", subcommands = Specialty.At.class)
    public static class Specialty implements Callable<Integer> {
        @Option(names = { "-c", "--code" }, required = true)
        private String code;

        @Override
        public Integer call() throws Exception {
            db.DeleteSpecialty(code);
            return 0;
        }

        @Command(name = "at")
        public static class At implements Callable<Integer> {
            @Option(names = { "-c", "--code" }, required = true)
            private String code;

            @Option(names = { "-u", "--university" }, required = true)
            private Integer university_id;

            @Option(names = { "-s", "--study-form" })
            private String study_form;

            @Override
            public Integer call() throws SQLException {
                if (study_form == null) {
                    db.DeleteSpecialty(code, university_id);
                    return 0;
                }

                db.DeleteSpecialty(code, university_id, study_form);
                return 0;
            }
        }
    }

    @Command(name = "subject")
    public static class Subject implements Callable<Integer> {
        @ArgGroup(multiplicity = "1")
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = "--unused", required = true)
            private Boolean unused;

            @ArgGroup(exclusive = false, multiplicity = "1")
            DependentGroup dependent;
        }

        private static class DependentGroup {
            @Option(names = { "-s", "--subject" }, required = true)
            private String subject_name;

            @Option(names = { "-u", "--university" }, required = true)
            private Integer university_id;

            @Option(names = { "-f", "--faculty" }, required = true)
            private Integer faculty_id;

            @Option(names = { "-c", "--code" }, required = true)
            private String specialty_code;
        }

        @Override
        public Integer call() throws Exception {
            if (exclusive.unused) {
                db.DeleteUnusedSubject();
                return 0;
            }

            var d = exclusive.dependent;
            db.DeleteSubject(d.subject_name, d.university_id, d.faculty_id, d.specialty_code);

            return 0;
        }
    }
}
