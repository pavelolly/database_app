CREATE SEQUENCE IF NOT EXISTS university_uid;

CREATE TABLE IF NOT EXISTS university
(
    university_id    INTEGER      NOT NULL DEFAULT nextval('university_uid'::regclass),
    name             VARCHAR(255) NOT NULL UNIQUE,
    url              VARCHAR(255),
    state            BOOLEAN      NOT NULL DEFAULT false,
    campus           BOOLEAN      NOT NULL DEFAULT false,
    military         BOOLEAN      NOT NULL DEFAULT false,
    next_employee_id INTEGER      NOT NULL DEFAULT 1,

    PRIMARY KEY (university_id)
);

CREATE TABLE IF NOT EXISTS building
(
    university_id INTEGER      NOT NULL REFERENCES university ON DELETE CASCADE,
    building_name VARCHAR(255) NOT NULL,
    address       VARCHAR(255) NOT NULL,

    PRIMARY KEY (university_id, building_name)
);

CREATE TABLE IF NOT EXISTS employee
(
    university_id INTEGER     NOT NULL REFERENCES university ON DELETE CASCADE,
    employee_id   INTEGER     NOT NULL,
    first_name    VARCHAR(50) NOT NULL,
    last_name     VARCHAR(50),
    patronymic    VARCHAR(50),

    PRIMARY KEY (university_id, employee_id)
);

CREATE TABLE IF NOT EXISTS department
(
    university_id INTEGER      NOT NULL REFERENCES university ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    headmaster_id INTEGER,
    url           VARCHAR(255),
    email         VARCHAR(255),
    department_id INTEGER      NOT NULL DEFAULT nextval('university_uid'::regclass),

    PRIMARY KEY (university_id, department_id),
    UNIQUE (university_id, name),

    FOREIGN KEY (university_id, headmaster_id) REFERENCES employee ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS job
(
    university_id INTEGER      NOT NULL,
    employee_id   INTEGER      NOT NULL,
    job_name      VARCHAR(255) NOT NULL,
    department_id INTEGER      NOT NULL,

    PRIMARY KEY (university_id, employee_id, department_id, job_name),
    FOREIGN KEY (university_id, department_id) REFERENCES department ON DELETE CASCADE,
    FOREIGN KEY (university_id, employee_id) REFERENCES employee ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS location
(
    university_id INTEGER      NOT NULL,
    building_name VARCHAR(255) NOT NULL,
    head_office   VARCHAR(20),
    department_id INTEGER      NOT NULL,

    PRIMARY KEY (university_id, department_id, building_name),
    FOREIGN KEY (university_id, building_name) REFERENCES building ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (university_id, department_id) REFERENCES department ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS faculty
(
    university_id INTEGER NOT NULL,
    department_id INTEGER NOT NULL,

    PRIMARY KEY (university_id, department_id),
    FOREIGN KEY (university_id, department_id) REFERENCES department ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cathedra
(
    university_id INTEGER NOT NULL,
    department_id INTEGER NOT NULL,
    faculty_id    INTEGER NOT NULL,

    PRIMARY KEY (university_id, department_id, faculty_id),
    FOREIGN KEY (university_id, department_id) REFERENCES department ON DELETE CASCADE,
    FOREIGN KEY (university_id, faculty_id) REFERENCES faculty (university_id, department_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS specialty
(
    specialty_code VARCHAR(10) PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    qualification  TEXT         NOT NULL
);

CREATE TABLE IF NOT EXISTS specialty_at_university
(
    university_id         INTEGER     NOT NULL,
    specialty_code        VARCHAR(10) NOT NULL REFERENCES specialty ON UPDATE CASCADE ON DELETE CASCADE,
    study_form            VARCHAR(10) NOT NULL,
    months_to_study       INTEGER     NOT NULL,
    number_of_free_places INTEGER     NOT NULL,
    number_of_paid_places INTEGER     NOT NULL,
    faculty_id            INTEGER     NOT NULL,

    PRIMARY KEY (university_id, specialty_code, study_form, faculty_id),
    FOREIGN KEY (university_id, faculty_id) REFERENCES faculty (university_id, department_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subject
(
    subject_name VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS hours
(
    university_id   INTEGER      NOT NULL,
    specialty_code  VARCHAR(10)  NOT NULL,
    study_form      VARCHAR(10)  NOT NULL,
    subject_name    VARCHAR(255) NOT NULL REFERENCES subject ON UPDATE CASCADE ON DELETE CASCADE,
    number_of_hours INTEGER      NOT NULL,
    faculty_id      INTEGER      NOT NULL,

    PRIMARY KEY (university_id, specialty_code, study_form, subject_name, faculty_id),
    FOREIGN KEY (university_id, specialty_code, study_form, faculty_id) REFERENCES specialty_at_university ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.professor
(
    university_id INTEGER      NOT NULL,
    employee_id   INTEGER      NOT NULL,
    subject_name  VARCHAR(255) NOT NULL REFERENCES subject ON DELETE CASCADE,

    PRIMARY KEY (university_id, employee_id, subject_name),
    FOREIGN KEY (university_id, employee_id) REFERENCES employee ON DELETE CASCADE
);

