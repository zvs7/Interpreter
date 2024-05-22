import java.util.*;
import java.util.regex.*;

public class Interpreter {
    // Pattern to match tokens in the input
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "\\s*(=>|[-+*/=();]|\\d+|\\w+|.)");

    // Enum to represent the different types of tokens
    private enum TokenType {
        EOF, IDENTIFIER, LITERAL, OPERATOR, PAREN, SEMICOLON
    }

    // Class to represent a token
    private static class Token {
        TokenType type;
        String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    // Method to tokenize the input string
    private List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.isEmpty()) 
                continue;

            // Check if the token is a literal
            if (token.matches("\\d+")) {
                // Check for invalid literals with leading zeros
                if (token.length() > 1 && token.startsWith("0")) {
                    throw new RuntimeException("error");
                }
                tokens.add(new Token(TokenType.LITERAL, token));
            } else if (token.matches("\\w+")) {
                // Check if the token is an identifier
                tokens.add(new Token(TokenType.IDENTIFIER, token));
            } else if (token.matches("[-+*/=]")) {
                // Check if the token is an operator
                tokens.add(new Token(TokenType.OPERATOR, token));
            } else if (token.equals(";") || token.equals("(") || token.equals(")")) {
                // Check if the token is a parenthesis or semicolon
                tokens.add(new Token(TokenType.PAREN, token));
            } else {
                // Unexpected token
                throw new RuntimeException("error: Unexpected token: " + token);
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    // Parser class to parse the tokens
    private static class Parser {
        private List<Token> tokens;
        private int pos;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        // Method to peek the current token
        private Token peek() {
            return tokens.get(pos);
        }

        // Method to advance to the next token
        private Token advance() {
            return tokens.get(pos++);
        }

        // Method to expect a specific type of token
        private void expect(TokenType type) {
            if (peek().type != type) {
                throw new RuntimeException("error: Syntax error: expected " + type + " but found " + peek().type);
            }
            advance();
        }

        // Method to parse the entire program
        private void parseProgram(Map<String, Integer> variables) {
            while (peek().type != TokenType.EOF) {
                parseAssignment(variables);
                expect(TokenType.PAREN); // Expecting ';'
            }
        }

        // Method to parse an assignment statement
        private void parseAssignment(Map<String, Integer> variables) {
            Token id = advance();
            if (id.type != TokenType.IDENTIFIER) {
                throw new RuntimeException("error: Syntax error: expected identifier but found " + id.type);
            }
            expect(TokenType.OPERATOR); // '='
            int value = parseExp(variables);
            variables.put(id.value, value);
        }

        // Method to parse an expression
        private int parseExp(Map<String, Integer> variables) {
            int value = parseTerm(variables);
            while (peek().type == TokenType.OPERATOR && (peek().value.equals("+") || peek().value.equals("-"))) {
                String op = advance().value;
                int termValue = parseTerm(variables);
                if (op.equals("+")) {
                    value += termValue;
                } else {
                    value -= termValue;
                }
            }
            return value;
        }

        // Method to parse a term
        private int parseTerm(Map<String, Integer> variables) {
            int value = parseFactor(variables);
            while (peek().type == TokenType.OPERATOR && peek().value.equals("*")) {
                advance(); // '*'
                int factorValue = parseFactor(variables);
                value *= factorValue;
            }
            return value;
        }

        // Method to parse a factor
        private int parseFactor(Map<String, Integer> variables) {
            Token token = advance();
            if (token.type == TokenType.PAREN && token.value.equals("(")) {
                int value = parseExp(variables);
                expect(TokenType.PAREN); // ')'
                return value;
            } else if (token.type == TokenType.OPERATOR && (token.value.equals("+") || token.value.equals("-"))) {
                int value = parseFactor(variables);
                return token.value.equals("-") ? -value : value;
            } else if (token.type == TokenType.LITERAL) {
                return Integer.parseInt(token.value);
            } else if (token.type == TokenType.IDENTIFIER) {
                if (!variables.containsKey(token.value)) {
                    throw new RuntimeException("error: Semantic error: uninitialized variable " + token.value);
                }
                return variables.get(token.value);
            } else {
                throw new RuntimeException("error: Syntax error: unexpected token " + token.value);
            }
        }
    }

    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();

        String[] inputs = {
            "x = 001;",    // error
            "x_2 = 0;",    // x_2 = 0
            "x = 0\n" + "y = x;\n" + "z = ---(x+y);", // error
            "x = 1;\n" + "y = 2;\n" + "z = ---(x+y)*(x+-y);" // x = 1, y = 2, z = 3
        };

        for (String input : inputs) {
            System.out.println("Input:");
            System.out.println(input);
            try {
                // Tokenize the input
                List<Token> tokens = interpreter.tokenize(input);
                // Parse the tokens
                Parser parser = new Parser(tokens);
                Map<String, Integer> variables = new HashMap<>();
                parser.parseProgram(variables);
                // Output the results
                System.out.println("Output:");
                variables.forEach((k, v) -> System.out.println(k + " = " + v));
            } catch (RuntimeException e) {
                // Output the error
                System.out.println("Output:");
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }
}