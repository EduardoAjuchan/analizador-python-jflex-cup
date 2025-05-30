package analizador;
public class SyntaxError {
    public int line;
    public int column;
    public String message;
    public int length;

    public SyntaxError(int line, int column, String message, int length) {
        this.line = line;
        this.column = column;
        this.message = message;
        this.length = length;
    }

    @Override
    public String toString() {
        return String.format("Error en línea %d, columna %d: %s", line, column, message);
    }
}