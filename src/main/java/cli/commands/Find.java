package cli.commands;

import app.DataBase;
import picocli.CommandLine.*;

import java.sql.SQLException;
import java.util.concurrent.Callable;

@Command(name = "find",
        subcommands = { Find.University.class,
                        Find.Department.class,
                        Find.Building.class })
public class Find extends AbstractCommand {
    public Find(DataBase db) {
        super(db);
    }

    @Command(name = "university")
    public static class University implements Callable<Integer> {
        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Override
        public Integer call() throws SQLException {
            int id = db.GetUniversityId(name);
            System.out.println("Id of '" + name + "' is: " + id);

            return 0;
        }
    }

    @Command(name = "department")
    public static class Department implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Override
        public Integer call() throws SQLException {
            int id = db.GetDepartmentId(university_id, name);
            System.out.println("Id of '" + name + "' is: " + id);
            return 0;
        }
    }

    @Command(name = "building")
    public static class Building implements Callable<Integer> {
        @Option(names = { "-u", "--university" }, required = true)
        private Integer university_id;

        @Option(names = { "-n", "--name" }, required = true)
        private String name;

        @Override
        public Integer call() throws Exception {
            DataBase.Building building = db.GetBuildingInfo(university_id, name);
            System.out.println("Found building: " + building);
            return 0;
        }
    }
}
