import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 *  Tables from database
 */

public abstract class DBObject {
    @ToString
    public static class University extends DBObject {
        public String name;
        public String url;
        public Boolean state = false;
        public Boolean campus = false;
        public Boolean military = false;

        public University(String name) {
            this.name = name;
        }

        public University setUrl(String url) {
            this.url = url;
            return this;
        }

        public University setState(boolean state) {
            this.state = state;
            return this;
        }

        public University setCampus(boolean campus) {
            this.campus = campus;
            return this;
        }

        public University setMilitary(boolean military) {
            this.military = military;
            return this;
        }
    }

    @ToString
    public static class Building extends DBObject {
        public String name;
        public String address;

        public Building(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }

    @ToString
    public static class Department extends DBObject {
        public String name;
        public Integer headmaster_id;
        public String url;
        public String email;

        public Department(String name) {
            this.name = name;
        }

        public Department setHeadmasterId(int headmaster_id) {
            this.headmaster_id = headmaster_id;
            return this;
        }

        public Department setUrl(String url) {
            this.url = url;
            return this;
        }

        public Department setEmail(String email) {
            this.email = email;
            return this;
        }
    }

    @ToString
    public static class Employee extends DBObject {
        public String first_name;
        public String last_name;
        public String patronymic;

        public Employee(String first_name) {
            this.first_name = first_name;
        }

        public Employee setLastName(String last_name) {
            this.last_name = last_name;
            return this;
        }

        public Employee setPatronymic(String patronymic) {
            this.patronymic = patronymic;
            return this;
        }
    }

    @ToString
    public static class Specialty extends DBObject {
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
    public static class SpecailtyAtUniversity extends DBObject {
        public String study_form;
        public Integer month_to_study;
        public Integer number_of_free_places = 0;
        public Integer number_of_paid_places = 0;

        public SpecailtyAtUniversity(String study_form, Integer month_to_study) {
            this.study_form = study_form;
            this.month_to_study = month_to_study;
        }

        public SpecailtyAtUniversity setNumberOfFreePlaces(int number_of_free_places) {
            this.number_of_free_places = number_of_free_places;
            return this;
        }

        public SpecailtyAtUniversity setNumberOfPaidPlaces(int number_of_paid_places) {
            this.number_of_paid_places = number_of_paid_places;
            return this;
        }
    }
}
