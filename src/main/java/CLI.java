import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private final Scanner scanner = new Scanner(System.in);

    public static class Command {
        private final Token command;
        private final Token subcommand;
        private final List<Token> args;
        private final String raw;

        Command(Token command, Token subcommand, List<Token> args, String raw) {
            this.command = command;
            this.subcommand = subcommand;
            this.args = args;
            this.raw = raw;
        }

        public Token GetCommandToken() {
            return command;
        }
        public Token GetSubcommandToken() {
            return subcommand;
        }
        public Token GetArgToken(int index) {
           try {
               return args.get(index);
           } catch (IndexOutOfBoundsException e) {
               return null;
           }
        }

        public String GetCommand() {
            return command.value;
        }
        public String GetSubcommand() {
            return subcommand.value;
        }
        public List<String> GetArgs() {
            var list = new ArrayList<String>();
            for (var arg : args) {
                list.add(arg.value);
            }
            return list;
        }
        public String GetRaw() {
            return raw;
        }
    }

    public Command GetCommand() {
        List<Token> tokens;
        String raw_command;
        do {
            System.out.print(">>> ");
            raw_command = scanner.nextLine();
            tokens = SplitCommand(raw_command);
        } while (tokens.isEmpty());

        return new Command(tokens.get(0),
                tokens.size() > 1 ? tokens.get(1) : null,
                tokens.size() > 2 ? tokens.subList(2, tokens.size()) : null,
                raw_command);
    }

    private List<Token> SplitCommand(String command) {
        try {
            return new Parser(command).Parse();
        } catch (CLIError e) {
            e.PrintForCommand(command);
        }

        return new ArrayList<>();
    }

    public static class Token {
        public String value;
        public int pos;

        public Token(String value, int pos) {
            this.value = value;
            this.pos = pos;
        }
    }

    public abstract static class CLIError extends Exception {
        public int pos;

        public CLIError(String message, int pos) {
            super(message);
            this.pos = pos;
        }

        public abstract void PrintForCommand(String command);
    }

    public static class ParsingError extends CLIError {
        public ParsingError(String message, int pos) {
            super(message, pos);
        }

        @Override
        public void PrintForCommand(String command) {
            System.err.println("[PARSER ERROR] " + this.getMessage());
            System.err.println(command);
            System.err.println(" ".repeat(pos) + "^");
            System.err.println();
        }
    }

    public static class InvalidCommandError extends CLIError {
        public InvalidCommandError(String message, Token token) {
            super(message, token.pos);
        }

        @Override
        public void PrintForCommand(String command) {
            System.err.println("[INVALID COMMAND] " + this.getMessage());
            System.err.println(command);
            System.err.println(" ".repeat(pos) + "^");
            System.err.println();
        }
    }

    private static class Parser {
        private final String command;
        private int pos;

        public Parser(String command) {
            this.command = command;
            this.pos = 0;
        }

        public List<Token> Parse() throws CLIError {
            var tokens = new ArrayList<Token>();
            while (pos < command.length()) {
                if (Character.isWhitespace(command.charAt(pos))) {
                    pos++;
                    continue;
                }

                if (command.charAt(pos) == '"' || command.charAt(pos) == '\'') {
                    int start = pos + 1; // ignore open quote
                    int end = ParseStringLiteral(command.charAt(pos));
                    if (end == -1) {
                        throw new ParsingError("String literal is not closed", start - 1);
                    }

                    var token = new Token(command.substring(start, end), start);
                    tokens.add(token);
                    pos = end + 1; // ignore closing quote

                    if (pos < command.length() && !Character.isWhitespace(command.charAt(pos))) {
                        throw new ParsingError("Non-space character immediately after string literal", pos);
                    }
                } else {
                    int start = pos;
                    int end = ParseWord();
                    var token = new Token(command.substring(start, end), start);
                    tokens.add(token);
                    pos = end;
                }
            }
            return tokens;
        }

        private int ParseStringLiteral(char quote) {
            int end = pos + 1;
            while (end < command.length() && command.charAt(end) != quote) {
                end++;
            }

            if (end == command.length()) {
                return -1;
            }

            return end;
        }

        private int ParseWord() throws CLIError {
            int end = pos + 1;
            while (end < command.length() && !Character.isWhitespace(command.charAt(end))) {
                if (command.charAt(end) == '"' || command.charAt(end) == '\'') {
                    throw new ParsingError("Quote with no string literal", end);
                }
                end++;
            }
            return end;
        }
    }
}
