package analizador;
import java.util.*;
import java.util.regex.*;

public class PythonLexer {
    private final String input;
    private int currentIndex = 0;
    private int line = 1;
    private int column = 1;

    private final List<Token> tokens = new ArrayList<>();

    private static final Set<String> KEYWORDS = Set.of(
            "False", "None", "True", "and", "as", "assert", "async", "await", "break",
            "class", "continue", "def", "del", "elif", "else", "except", "finally",
            "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
            "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
    );

    private static final Set<String> BUILTINS = Set.of(
            "print", "range", "len", "input", "str", "int", "float", "bool", "list",
            "dict", "set", "type", "abs", "sum", "min", "max", "sorted", "reversed",
            "enumerate", "zip", "map", "filter"
    );

    private static final List<PatternToken> PATTERNS = List.of(
            new PatternToken("comment", Pattern.compile("^#.*")),
            new PatternToken("string", Pattern.compile("^(\"\"\".*?\"\"\"|'''.*?'''|\"(\\\\.|[^\"\\\\\\n])*\"|'(\\\\.|[^'\\\\\\n])*')", Pattern.DOTALL)),
            new PatternToken("number", Pattern.compile("^\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b")),
            new PatternToken("identifier", Pattern.compile("^\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")),
            new PatternToken("symbol", Pattern.compile("^(==|!=|<=|>=|->|:=|\\+|-|\\*|\\/|%|=|<|>|\\(|\\)|\\[|\\]|\\{|\\}|\\.|,|:|;|@|\\||&|\\^|~)")),
            new PatternToken("whitespace", Pattern.compile("^[ \\t]+")),
            new PatternToken("newline", Pattern.compile("^\n"))
    );

    public PythonLexer(String input) {
        this.input = input;
    }

    public static List<Token> tokenize(String code) {
        PythonLexer lexer = new PythonLexer(code);
        lexer.scan();
        return lexer.tokens;
    }

    private void scan() {
        while (currentIndex < input.length()) {
            String remaining = input.substring(currentIndex);

            boolean matched = false;

            for (PatternToken rule : PATTERNS) {
                Matcher matcher = rule.pattern.matcher(remaining);
                if (matcher.find()) {
                    String value = matcher.group();
                    String type = rule.type;

                    int tokenSymId = switch (type) {
                        case "comment" -> sym.COMMENT;
                        case "string" -> sym.STRING;
                        case "number" -> sym.NUMBER;
                        case "identifier" -> {
                            if (KEYWORDS.contains(value)) yield sym.KEYWORD;
                            else if (BUILTINS.contains(value)) yield sym.BUILTIN;
                            else yield sym.IDENTIFIER;
                        }
                        case "symbol" -> sym.SYMBOL;
                        case "whitespace" -> sym.WHITESPACE;
                        case "newline" -> sym.NEWLINE;
                        default -> sym.ERROR;
                    };

                    tokens.add(new Token(tokenSymId, value, line, column));

                    advance(value);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                char unknown = input.charAt(currentIndex);
                tokens.add(new Token(sym.ERROR, String.valueOf(unknown), line, column));
                advance(String.valueOf(unknown));
            }
        }

        tokens.add(new Token(sym.EOF, "", line, column));
    }

    private void advance(String text) {
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        currentIndex += text.length();
    }

    // Clase auxiliar para emparejar tipo + patrón
    private static class PatternToken {
        String type;
        Pattern pattern;

        public PatternToken(String type, Pattern pattern) {
            this.type = type;
            this.pattern = pattern;
        }
    }
}