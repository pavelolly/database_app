package cli.commands;

import app.DataBase;
import picocli.CommandLine.*;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Command(name = "update",
        subcommands = { Update.University.class,
                        Update.Building.class,
                        Update.Department.class,
                        Update.HeadOffice.class,
                        Update.Specialty.class,
                        Update.Employee.class })
public class Update extends AbstractCommand {
    public Update(DataBase db) {
        super(db);
    }

    @Command(name = "university")
    public static class University implements Callable<Integer> {
        @Spec Model.CommandSpec spec;

        @Option(names = { "-i","--id" }, required = true)
        private Integer id;

        @Option(names = { "-n", "--name" })
        private String name;

        @Option(names = { "-u", "--url" })
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
            if (name != null) {
                db.UpdateUniversityName(id, name);
            }
            if (url != null) {
                db.UpdateUniversityUrl(id, url);
            }
            if (state_flag != null) {
                db.UpdateUniversityStateFlag(id, ValidateFlag("--state", state_flag));
            }
            if (campus_flag != null) {
                db.UpdateUniversityCampusFlag(id, ValidateFlag("--campus", campus_flag));
            }
            if (military_flag != null) {
                db.UpdateUniversityMilitaryFlag(id, ValidateFlag("--military", military_flag));
            }

            return 0;
        }
    }

    @Command(name = "building")
    public static class Building implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-n", "--name" })
        private String name;

        @Option(names = { "-a", "--address" })
        private String address;

        @Override
        public Integer call() throws SQLException {
            if (name != null) {
                db.UpdateBuildingName(university_id, name);
            }
            if (address != null) {
                db.UpdateBuildingAddress(university_id, address);
            }
            return 0;
        }
    }

    @Command(name = "department")
    public static class Department implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-i", "--id" }, required = true)
        private Integer department_id;

        @Option(names = { "-n", "--name" })
        private String name;

        @Option(names = { "--url" })
        private String url;

        @Option(names = { "--email" })
        private String email;

        @Option(names = { "-h", "--headmaster" })
        private Integer headmster_id;

        @Override
        public Integer call() throws SQLException {
            if (name != null) {
                db.UpdateDepartmentName(university_id, department_id, name);
            }
            if (url != null) {
                db.UpdateDepartmentUrl(university_id, department_id, url);
            }
            if (email != null) {
                db.UpdateDepartmentEmail(university_id, department_id, email);
            }
            if (headmster_id != null) {
                db.UpdateDepartmentHeadmasterId(university_id, department_id, headmster_id);
            }

            return 0;
        }
    }

    @Command(name = "head-office")
    public static class HeadOffice implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-d", "--department" }, required = true)
        private Integer department_id;

        @Option(names = { "-b", "--building" }, required = true)
        private String building_name;

        @Parameters
        private String head_office_name;

        @Override
        public Integer call() throws SQLException {
            db.UpdateDepartmentHeadOffice(university_id, department_id, building_name, head_office_name);
            return 0;
        }
    }

    @Command(name = "specialty", subcommands = Specialty.At.class)
    public static class Specialty implements Callable<Integer> {
        @Option(names = { "-c", "--code" }, required = true)
        private String specailty_code;

        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Override
        public Integer call() throws SQLException {
            db.UpdateSpecialtyName(specailty_code, name);
            return 0;
        }

        @Command(name = "at")
        public static class At implements Callable<Integer> {

            @Option(names = { "-u", "--university" }, required = true)
            private Integer university_id;

            @Option(names = { "-f", "--faculty" }, required = true)
            private Integer faculty_id;

            @Option(names = { "-c", "--code" }, required = true)
            private String specailty_code;

            @Option(names = { "--study-form" }, required = true)
            private String study_form;

            @Option(names = "--month-to-study")
            private Integer month_to_study;

            @Option(names = "--free-places")
            private Integer free_places;

            @Option(names = "--paid-places")
            private Integer paid_places;

            @ArgGroup(exclusive = false)
            HoursGroup hours_group;

            private static class HoursGroup {
                @Option(names = { "-s", "--subject" }, required = true)
                private String subject_name;

                @Option(names = { "-h", "--hours" }, required = true)
                private Integer hours;
            }

            @Override
            public Integer call() throws SQLException {
                if (month_to_study != null) {
                    db.UpdateSpecialtyMonthsToStudy(university_id, faculty_id, specailty_code, study_form, month_to_study);
                }
                if (free_places != null) {
                    db.UpdateSpecialtyFreePlaces(university_id, faculty_id, specailty_code, study_form, free_places);
                }
                if (paid_places != null) {
                    db.UpdateSpecialtyPaidPlaces(university_id, faculty_id, specailty_code, study_form, paid_places);
                }
                if (hours_group != null) {
                    db.UpdateHours(university_id, faculty_id, specailty_code, study_form, hours_group.subject_name, hours_group.hours);
                }
                return 0;
            }
        }
    }

    @Command(name = "employee")
    public static class Employee implements Callable<Integer> {

        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-e", "--employee" }, required = true)
        private Integer employee_id;

        @Option(names = { "-f", "--first-name" }, required = true)
        private String first_name;

        @Option(names = { "-l", "--last-name" })
        private String last_name;

        @Option(names = { "-p", "--patronymic" })
        private String patronymic;

        @Override
        public Integer call() throws SQLException {
            db.UpdateEmployeeName(university_id, employee_id,
                    new DataBase.Employee(first_name).
                            LastName(last_name).
                            Patronymic(patronymic));
            return 0;
        }
    }
}
