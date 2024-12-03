package cli.commands;

import app.DataBase;
import picocli.CommandLine.*;
import tuples.Triple;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "show",
         subcommands = { Show.University.class,
                         Show.Department.class,
                         Show.Building.class,
                         Show.Specialty.class,
                         Show.Employee.class })
public class Show extends AbstractCommand {
    public Show(DataBase db) {
        super(db);
    }

    @Command(name = "university")
    public static class University implements Callable<Integer> {
        @ArgGroup
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = { "-i", "--id" }, required = true)
            private Integer id;

            @Option(names = { "-s", "--specialty" }, required = true)
            private String specialty_code;
        }

        @Override
        public Integer call() throws SQLException {
            if (exclusive == null) {
                Map<Integer, String> universities = db.GetUniversities();
                DataBase.PrintIdNameMap(universities);
                return 0;
            }

            if (exclusive.id != null) {
                DataBase.University university = db.GetUniversityInfo(exclusive.id);
                System.out.println(university);
                return 0;
            }

            if (exclusive.specialty_code != null) {
                Map<Integer, String> universities = db.GetUniversitiesWithSpecailty(exclusive.specialty_code);
                DataBase.PrintIdNameMap(universities);
                return 0;
            }

            return 0;
        }
    }

    @Command(name = "department")
    public static class Department implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @ArgGroup
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = { "-i", "--id" }, required = true)
            public Integer department_id;

            @Option(names = { "-f", "--faculty" }, required = true)
            public Boolean faculty;

            @Option(names = { "-c", "--cathedra" }, required = true)
            public Integer cathedra_id;

            @Option(names = { "-b", "--building" }, required = true)
            public String building_name;

            @Option(names = { "-h", "--head-office" }, required = true)
            public Integer department_id_for_head_office;

            @Option(names = { "-s", "--specialty" }, required = true)
            public String specialty_code;
        }

        @Override
        public Integer call() throws SQLException {
            if (exclusive == null) {
                Map<Integer, String> departments = db.GetDepartments(university_id);
                DataBase.PrintIdNameMap(departments);
                return 0;
            }

            if (exclusive.department_id != null) {
                DataBase.Department department = db.GetDepartmentInfo(university_id, exclusive.department_id);
                System.out.println(department);

                return 0;
            }
            if (exclusive.faculty != null) {
                Map<Integer, String> faculties = db.GetFaculties(university_id);
                DataBase.PrintIdNameMap(faculties);

                return 0;
            }
            if (exclusive.cathedra_id != null) {
                Map<Integer, String> cathedras = db.GetCathedras(university_id, exclusive.cathedra_id);
                DataBase.PrintIdNameMap(cathedras);

                return 0;
            }
            if (exclusive.building_name != null) {
                Map<Integer, String> departments = db.GetDepartments(university_id, exclusive.building_name);
                DataBase.PrintIdNameMap(departments);

                return 0;
            }
            if (exclusive.department_id_for_head_office != null) {
                Map<DataBase.Building, String> head_offices = db.GetHeadOffice(university_id,exclusive.department_id_for_head_office);
                for (var entry : head_offices.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
            if (exclusive.specialty_code != null) {
                Map<Integer, String> faculties = db.GetFacultiesWithSpecailty(university_id, exclusive.specialty_code);
                DataBase.PrintIdNameMap(faculties);
            }

            return 0;
        }
    }

    @Command(name = "building")
    public static class Building implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-d", "--department"})
        private Integer department_id;

        @Override
        public Integer call() throws SQLException {
            List<DataBase.Building> buildings;
            if (department_id != null) {
                buildings = db.GetBuildings(university_id, department_id);
            } else {
                buildings = db.GetBuildings(university_id);
            }

            for (var building : buildings) {
                System.out.println(building);
            }

            return 0;
        }
    }

    @Command(name = "specialty", subcommands = Specialty.At.class)
    public static class Specialty implements Callable<Integer> {
        @ArgGroup
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = { "-u", "--university" }, required = true)
            private Integer university_id;

            @Option(names = { "-c", "--code" }, required = true)
            private String specialty_code;
        }

        @Override
        public Integer call() throws SQLException {
            if (exclusive == null) {
                List<DataBase.Specialty> specailties = db.GetSpecialties();
                for (var specailty : specailties) {
                    System.out.println(specailty);
                }
                return 0;
            }

            if (exclusive.university_id != null) {
                Map<DataBase.Specialty, String> specailties = db.GetSpecialties(exclusive.university_id);
                for (var entry : specailties.entrySet()) {
                    System.out.println(entry.getKey() + " at faculty '" + entry.getValue() + "'");
                }

                return 0;
            }

            if (exclusive.specialty_code != null) {
                DataBase.Specialty specialty = db.GetSpecialtyInfo(exclusive.specialty_code);
                System.out.println(specialty);

                return 0;
            }

            return 1;
        }

        @Command(name = "at")
        public static class At implements Callable<Integer> {

            @Option(names = { "-u", "--university" }, required = true)
            private Integer university_id;

            @Option(names = { "-f", "--faculty" }, required = true)
            private Integer faculty_id;

            @Option(names = { "-c", "--code" }, required = true)
            private String specialty_code;

            @Option(names = { "-s", "--subjects" })
            private Boolean subjects;

            @Override
            public Integer call() throws SQLException {
                if (subjects) {
                    List<DataBase.SpecialtyAtUniversity> specailties = db.GetSpecialtyAtUniversity(university_id, faculty_id, specialty_code);
                    for (var specailty : specailties) {
                        System.out.println(specailty);
                    }
                    return 0;
                }

                List<Triple<String, String, Integer>> subjects = db.GetSubjects(university_id, faculty_id, specialty_code);
                for (var subject : subjects) {
                    System.out.printf("Subject: '%s', Form: '%s', Hours: %d\n",
                            subject.getFirst(), subject.getSecond(), subject.getThird());
                }

                return 0;
            }
        }
    }

    @Command(name = "employee")
    public static class Employee implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @ArgGroup(multiplicity = "1")
        ExclusiveGroup exclusive;

        private static class ExclusiveGroup {
            @Option(names = { "-n", "--name" }, required = true)
            private Integer employee_id_name;

            @Option(names = { "-j", "--jobs" }, required = true)
            private Integer employee_id_jobs;

            @Option(names = { "-d", "--department" }, required = true)
            private Integer department_id;

            @Option(names = { "-s", "--subject" }, required = true)
            private String subject;
        }

        @Override
        public Integer call() throws SQLException {
            if (exclusive.employee_id_name != null) {
                DataBase.Employee employee = db.GetEmployeeInfo(university_id, exclusive.employee_id_name);
                System.out.println(employee);
                return 0;
            }
            if (exclusive.employee_id_jobs != null) {
                Map<Integer, String> jobs = db.GetEmployeeJobs(university_id, exclusive.employee_id_jobs);
                for (var job : jobs.entrySet()) {
                    System.out.println(job.getValue() + " at department with id: " + job.getKey());
                }
                return 0;
            }
            if (exclusive.department_id != null) {
                Map<Integer, DataBase.Employee> employees = db.GetEmployees(university_id, exclusive.department_id);
                for (var employee : employees.values()) {
                    System.out.println(employee);
                }
                return 0;
            }
            if (exclusive.subject != null) {
                Map<Integer, DataBase.Employee> professors = db.GetProfessors(university_id, exclusive.subject);
                for (var professor : professors.values()) {
                    System.out.println(professor);
                }
                return 0;
            }

            return 0;
        }
    }
}
