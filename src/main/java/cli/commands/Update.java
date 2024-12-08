package cli.commands;

import app.DataBase;
import picocli.CommandLine.*;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Command(name = "update", mixinStandardHelpOptions = true,
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

    @Command(name = "university", mixinStandardHelpOptions = true)
    public static class University implements Callable<Integer> {
        @Spec Model.CommandSpec spec;

        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-n", "--name" })
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
            if (name != null) {
                db.UpdateUniversityName(university_id, name);
            }
            if (url != null) {
                db.UpdateUniversityUrl(university_id, url.isEmpty() ? null : url);
            }
            if (state_flag != null) {
                db.UpdateUniversityStateFlag(university_id, ValidateFlag("--state", state_flag));
            }
            if (campus_flag != null) {
                db.UpdateUniversityCampusFlag(university_id, ValidateFlag("--campus", campus_flag));
            }
            if (military_flag != null) {
                db.UpdateUniversityMilitaryFlag(university_id, ValidateFlag("--military", military_flag));
            }

            return 0;
        }
    }

    @Command(name = "building", mixinStandardHelpOptions = true)
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

    @Command(name = "department", mixinStandardHelpOptions = true)
    public static class Department implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-d", "--department" }, required = true)
        private Integer department_id;

        @Option(names = { "-n", "--name" })
        private String name;

        @Option(names = { "--url" })
        private String url;

        @Option(names = { "--email" })
        private String email;

        @Option(names = { "--headmaster" })
        private Integer headmaster_id;

        @Override
        public Integer call() throws SQLException {
            if (name != null) {
                db.UpdateDepartmentName(university_id, department_id, name);
            }
            if (url != null) {
                db.UpdateDepartmentUrl(university_id, department_id, url.isEmpty() ? null : url);
            }
            if (email != null) {
                db.UpdateDepartmentEmail(university_id, department_id, email.isEmpty() ? null : email);
            }
            if (headmaster_id != null) {
                db.UpdateDepartmentHeadmasterId(university_id, department_id, headmaster_id);
            }

            return 0;
        }
    }

    @Command(name = "head-office", mixinStandardHelpOptions = true)
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
            db.UpdateDepartmentHeadOffice(university_id, department_id, building_name, head_office_name.isEmpty() ? null : head_office_name);
            return 0;
        }
    }

    @Command(name = "specialty", mixinStandardHelpOptions = true, subcommands = Specialty.At.class)
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

    @Command(name = "employee", mixinStandardHelpOptions = true)
    public static class Employee implements Callable<Integer> {

        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-e", "--employee" }, required = true)
        private Integer employee_id;

        @Option(names = { "-f", "--first-name" })
        private String first_name;

        @Option(names = { "-l", "--last-name" })
        private String last_name;

        @Option(names = { "-p", "--patronymic" })
        private String patronymic;

        @Override
        public Integer call() throws SQLException {
            if (first_name != null) {
                db.UpdateEmployeeFirstName(university_id, employee_id, first_name.isEmpty() ? null : first_name);
            }

            if (last_name != null) {
                db.UpdateEmployeeLastName(university_id, employee_id, last_name.isEmpty() ? null : last_name);
            }

            if (patronymic != null) {
                db.UpdateEmployeePatronymic(university_id, employee_id, patronymic.isEmpty() ? null : patronymic);
            }

            return 0;
        }
    }
}
