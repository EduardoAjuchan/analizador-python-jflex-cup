package analizador;
public class sym {
    public static final int EOF = 0;
    public static final int NEWLINE = 1;
    public static final int INDENT = 2;
    public static final int DEDENT = 3;

    public static final int IDENTIFIER = 4;
    public static final int KEYWORD = 5;
    public static final int NUMBER = 6;
    public static final int STRING = 7;
    public static final int COMMENT = 8;
    public static final int SYMBOL = 9;

    public static final int ERROR = 10;
    public static final int WHITESPACE = 11;
    public static final int BUILTIN = 12;
}