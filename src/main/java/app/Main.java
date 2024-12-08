package app;

import cli.Parser;
import cli.commands.*;
import cli.exceptions.CLIException;
import cli.exceptions.InvalidCommandException;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
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
                        case "reload" -> {
                            db.TruncateEverything();
                            Scripts.AddTverSU(db);
                        }
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