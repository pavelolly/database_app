package cli.commands;

import app.DataBase;
import picocli.CommandLine.Command;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "show", subcommands = { Show.Universities.class })
public class Show extends AbstractCommand {
    public Show(DataBase db) {
        super(db);
    }

    @Command(name = "universities")
    public static class Universities implements Callable<Integer> {
        @Override
        public Integer call() throws SQLException {
            Map<Integer, String> universities = db.GetUniversities();
            DataBase.PrintIdNameMap(universities);
            return 0;
        }
    }
}
