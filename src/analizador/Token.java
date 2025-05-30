package analizador;
public class Token {
    public int type;          // en lugar de 'sym'
    public String value;
    public int line;
    public int column;

    public Token(int type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("[%s: \"%s\" (%d,%d)]", getSymbolName(), value, line, column);
    }

    private String getSymbolName() {
        return switch (type) {
            case sym.EOF -> "EOF";
            case sym.NEWLINE -> "NEWLINE";
            case sym.INDENT -> "INDENT";
            case sym.DEDENT -> "DEDENT";
            case sym.IDENTIFIER -> "IDENTIFIER";
            case sym.KEYWORD -> "KEYWORD";
            case sym.NUMBER -> "NUMBER";
            case sym.STRING -> "STRING";
            case sym.COMMENT -> "COMMENT";
            case sym.SYMBOL -> "SYMBOL";
            case sym.ERROR -> "ERROR";
            case sym.BUILTIN -> "BUILTIN";
            default -> "UNKNOWN";
        };
    }
}