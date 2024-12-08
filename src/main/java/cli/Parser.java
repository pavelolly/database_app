package cli;

import cli.exceptions.CLIException;
import cli.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private String command;
    private int pos;

    private static class Token {
        public String value;
        public int pos;

        public Token(String value, int pos) {
            this.value = value;
            this.pos = pos;
        }

        public String GetValue() {
            return value;
        }
    }

    public static class ParsedCommand {
        private final String raw_command;
        private final List<Token> tokens;

        private ParsedCommand(String raw_command, List<Token> tokens) {
            this.raw_command = raw_command;
            this.tokens = tokens;
        }

        public String GetRaw() {
            return raw_command;
        }

        public List<String> GetCommandParams() {
            return tokens.subList(1, tokens.size()).
                    stream().map(Token::GetValue).
                    toList();
        }

        public String GetCommand() {
            if (!tokens.isEmpty()) {
                return tokens.getFirst().value;
            }
            return null;
        }

        public int GetCommandPosition() throws IndexOutOfBoundsException {
            return tokens.getFirst().pos;
        }

        public boolean Empty() {
            return tokens.isEmpty();
        }
    }

    public ParsedCommand Parse(String command) throws CLIException {
        this.command = command;
        this.pos = 0;

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
                    throw new ParsingException("String literal is not closed", command, start - 1);
                }

                var token = new Token(command.substring(start, end), start);
                tokens.add(token);
                pos = end + 1; // ignore closing quote

                if (pos < command.length() && !Character.isWhitespace(command.charAt(pos))) {
                    throw new ParsingException("Non-space character immediately after string literal", command, pos);
                }
            } else {
                int start = pos;
                int end = ParseWord();
                var token = new Token(command.substring(start, end), start);
                tokens.add(token);
                pos = end;
            }
        }
        return new ParsedCommand(command, tokens);
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

    private int ParseWord() throws CLIException {
        int end = pos + 1;
        while (end < command.length() && !Character.isWhitespace(command.charAt(end))) {
            if (command.charAt(end) == '"' || command.charAt(end) == '\'') {
                throw new ParsingException("Quote with no string literal", command, end);
            }
            end++;
        }
        return end;
    }
}