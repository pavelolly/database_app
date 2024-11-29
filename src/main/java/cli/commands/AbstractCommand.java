package cli.commands;

import app.DataBase;

public abstract class AbstractCommand {
    static protected DataBase db;

    public AbstractCommand(DataBase db) {
        AbstractCommand.db = db;
    }
}
