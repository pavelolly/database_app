package cli.exceptions;

public abstract class CLIException extends Exception {
    public String source;
    public int pos;

    public CLIException(String message, String source, int pos) {
        super(message);
        this.source = source;
        this.pos = pos;
    }

    public abstract void Print();
}

