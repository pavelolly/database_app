package app;

import cli.Parser;
import cli.commands.*;
import cli.exceptions.CLIException;
import cli.exceptions.InvalidCommandException;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void PrintInfo(String msg) {
        System.out.println("[INFO] " + msg);
    }

    private static void AddTverSU(DataBase db) throws SQLException {
        int uni_id = db.AddUniversity(new DataBase.University("Тверской Государственный Университет").
                Url("https://tversu.ru").
                CampusFlag(true).StateFlag(true).MilitaryFlag(false));

        PrintInfo("New added university has id: " + uni_id);

        db.AddBuilding(uni_id, new DataBase.Building("Центральный корпус",
                "г. Тверь, ул. Желябова, д.33"));
        db.AddBuilding(uni_id, new DataBase.Building("Корпус Б",
                "г. Тверь, Студенеческий переулок, д.12"));
        db.AddBuilding(uni_id, new DataBase.Building("Корпус 3",
                "г. Тверь, Садовый переулок, д.35"));

        int admin_id    = db.AddDepartment(uni_id, new DataBase.Department("Ректорат"));
        int commitee_id = db.AddDepartment(uni_id, new DataBase.Department("Приёмная комиссия"));
        int pmk_id      = db.AddFaculty(uni_id, new DataBase.Department("Факультет прикладной математики и кибернетики"));

        db.LocateDepartmentAtBuilding(uni_id, admin_id, "Центральный корпус");
        db.LocateDepartmentAtBuilding(uni_id, commitee_id, "Корпус Б");
        db.LocateDepartmentAtBuilding(uni_id, pmk_id, "Корпус 3");

        int system_alalysis_id = db.AddCathedra(uni_id,
                new DataBase.Department("Кафедра математической статистики и системного анализа"),
                pmk_id);

        int math_modelling_id = db.AddCathedra(uni_id,
                new DataBase.Department("Кафедра математического моделирования и вычислительной математики"),
                pmk_id);

        db.LocateDepartmentAtBuilding(uni_id, system_alalysis_id, "Корпус 3");
        db.LocateDepartmentAtBuilding(uni_id, math_modelling_id, "Корпус 3");

        var specialty = new DataBase.Specialty("01.03.02",
                "Прикладная математика и информатика",
                "Бакалавриат");

        db.AddSpecialtyForFaculty(uni_id, pmk_id, specialty,
                new DataBase.SpecialtyAtUniversity("Очная", 36, 140, 20));
        db.AddSpecialtyForFaculty(uni_id, pmk_id, specialty,
                new DataBase.SpecialtyAtUniversity("Заочная", 42, 30, 10));

        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Очная", "Алгебра", 72);
        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Заочная", "Алгебра", 64);

        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Очная", "Математический анализ", 72);
        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Заочная", "Математический анализ", 64);

        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Очная", "Численные методы", 72);
        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Заочная", "Численные методы", 64);

        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Очная", "Математическая статистика", 72);
        db.AddSubjectForSpecialty(uni_id, pmk_id, "01.03.02", "Заочная", "Математическая статистика", 64);

        int employee_id1 = db.AddEmployee(uni_id, new DataBase.Employee("Оксана").LastName("Сидорова").Patronymic("Игоревна"));
        int employee_id2 = db.AddEmployee(uni_id, new DataBase.Employee("Алексей").LastName("Васильев").Patronymic("Анатольевич"));
        int employee_id3 = db.AddEmployee(uni_id, new DataBase.Employee("Иван").LastName("Соловьёв"));
        int employee_id4 = db.AddEmployee(uni_id, new DataBase.Employee("Сергей").LastName("Дудаков").Patronymic("Михайлович"));
        int employee_id5 = db.AddEmployee(uni_id, new DataBase.Employee("Ректор"));

        db.AddJob(uni_id, system_alalysis_id, employee_id1, "Доцент каферды математической статистики и систнмного анализа");
        db.AddJob(uni_id, math_modelling_id, employee_id2, "Доцент каферды математического моделирования и вычислительной математики");
        db.AddJob(uni_id, pmk_id, employee_id3, "Преподаватель");
        db.AddJob(uni_id, pmk_id, employee_id4, "Декан факультета прикладной матетматики и кибернетики");
        // db.UpdateHeadmaster(uni_id, pmk_id, employee_id4);
        db.AddJob(uni_id, admin_id, employee_id5, "Ректор Тверского Госудасртвенного университета");
        // db.UpdateHeadmaster(uni_id, admin_id, employee_id5);

        db.AddProfessor(uni_id, employee_id1, "Математическая статистика");
        db.AddProfessor(uni_id, employee_id2, "Алгебра");
        db.AddProfessor(uni_id, employee_id2, "Математический анализ");
        db.AddProfessor(uni_id, employee_id3, "Численные методы");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Parser parser   = new Parser();
        var sql_handler = new IExecutionExceptionHandler() {
            @Override
            public int handleExecutionException(Exception e,
                                                CommandLine cmd,
                                                ParseResult parseResult)
                    throws Exception
            {
                if (e instanceof SQLException ex) {
                    DataBase.PrintSQLExecption(ex);

                    // propagating exit code (is that what it is?)
                    // stolen from https://picocli.info/#_handling_errors
                    return cmd.getExitCodeExceptionMapper() != null
                            ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                            : cmd.getCommandSpec().exitCodeOnExecutionException();
                }
                throw e;
            }
        };

        try (var db = new DataBase()) {
            var show_cmd   = new CommandLine(new Show(db)  ).setExecutionExceptionHandler(sql_handler);
            var update_cmd = new CommandLine(new Update(db)).setExecutionExceptionHandler(sql_handler);
            var find_cmd   = new CommandLine(new Find(db)  ).setExecutionExceptionHandler(sql_handler);
            var delete_cmd = new CommandLine(new Delete(db)).setExecutionExceptionHandler(sql_handler);
            var add_cmd    = new CommandLine(new Add(db)   ).setExecutionExceptionHandler(sql_handler);

            main_loop: while (true) {
                System.out.print("\n>>> ");
                String input = scanner.nextLine();
                try {
                    Parser.ParsedCommand command = parser.Parse(input);
                    if (command.Empty()) {
                        continue;
                    }

                    String[] params = command.GetCommandParams().toArray(new String[0]);

                    switch (command.GetCommand()) {
                        case "quit", "q", "exit", "ex" -> {
                            break main_loop;
                        }
                        case "show"   -> show_cmd.execute(params);
                        case "update" -> update_cmd.execute(params);
                        case "find"   -> find_cmd.execute(params);
                        case "delete" -> delete_cmd.execute(params);
                        case "add"    -> add_cmd.execute(params);
                        case null, default -> throw new InvalidCommandException("Unknown command",
                                command.GetRaw(),
                                command.GetCommandPosition());
                    }
                } catch (CLIException e) {
                    e.Print();
                }
            }
        } catch (SQLException e) {
            DataBase.PrintSQLExecption(e);
        }
    }
}