package cli.exceptions;

public class ParsingException extends CLIException {
    public ParsingException(String message, String source, int pos) {
        super(message, source, pos);
    }

    @Override
    public void Print() {
        System.err.println("[PARSER ERROR] " + this.getMessage());
        System.err.println(source);
        System.err.println(" ".repeat(pos) + "^");
        System.err.println();
    }
}
