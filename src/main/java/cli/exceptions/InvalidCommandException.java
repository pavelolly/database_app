package cli.exceptions;

public class InvalidCommandException extends CLIException {
    public InvalidCommandException(String message, String source, int pos) {
        super(message, source, pos);
    }

    @Override
    public void Print() {
        System.err.println("[INVALID COMMAND] " + this.getMessage());
        System.err.println(source);
        System.err.println(" ".repeat(pos) + "^");
        System.err.println();
    }
}
