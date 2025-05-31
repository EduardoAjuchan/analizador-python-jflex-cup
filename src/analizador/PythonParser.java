package analizador;

import java.util.*;
import static analizador.sym.*;

public class PythonParser {
    public static List<SyntaxError> checkSyntax(List<Token> tokens) {
        List<SyntaxError> errors = new ArrayList<>();
        Stack<Token> bracketStack = new Stack<>();
        Stack<Integer> indentLevels = new Stack<>();

        Set<String> definedVariables = new HashSet<>();
        Set<String> definedFunctions = new HashSet<>();
        Set<String> calledFunctions = new HashSet<>();
        Set<String> reservedWords = new HashSet<>(Arrays.asList(
                "def", "return", "if", "else", "for", "while", "None", "True", "False",
                "print", "and", "or", "not", "input", "float", "int", "str", "len", "lower"
        ));

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            // Errores léxicos
            if (token.type == ERROR) {
                // Ignorar retorno de carro CR (\r)
                if (token.value.equals("\r")) continue;
                errors.add(new SyntaxError(token.line, token.column,
                        "Carácter no reconocido: '" + token.value + "'", token.value.length()));
                continue;
            }

            // Balance de paréntesis y símbolos
            if (token.type == SYMBOL) {
                if (token.value.equals("(") || token.value.equals("{") || token.value.equals("[")) {
                    bracketStack.push(token);
                } else if (token.value.equals(")") || token.value.equals("}") || token.value.equals("]")) {
                    if (bracketStack.isEmpty()) {
                        errors.add(new SyntaxError(token.line, token.column,
                                "Cierre de símbolo inesperado: '" + token.value + "'", token.value.length()));
                    } else {
                        Token open = bracketStack.pop();
                        if (!esPareja(open.value, token.value)) {
                            errors.add(new SyntaxError(open.line, open.column,
                                    "Se esperaba cerrar '" + parejaEsperada(open.value) + "' pero se encontró '" + token.value + "'", open.value.length()));
                            errors.add(new SyntaxError(token.line, token.column,
                                    "Símbolo de cierre incorrecto: '" + token.value + "'", token.value.length()));
                        }
                    }
                }
            }

            // Validación de definición de funciones
            if (token.type == KEYWORD && token.value.equals("def")) {
                validarFuncion(tokens, i, errors);
                if (i + 1 < tokens.size() && tokens.get(i + 1).type == IDENTIFIER) {
                    definedFunctions.add(tokens.get(i + 1).value);
                }
                // Guardar los parámetros como variables válidas
                int j = i + 2;
                while (j < tokens.size()) {
                    Token t = tokens.get(j);
                    if (t.type == IDENTIFIER) {
                        definedVariables.add(t.value);
                    } else if (t.type == SYMBOL && t.value.equals(")")) {
                        break;
                    }
                    j++;
                }
            }

            // Indentación
            if (token.type == INDENT) {
                int spaces = token.value.length();
                if (indentLevels.isEmpty() || spaces > indentLevels.peek()) {
                    indentLevels.push(spaces);
                } else {
                    while (!indentLevels.isEmpty() && indentLevels.peek() > spaces) {
                        indentLevels.pop();
                    }
                    if (indentLevels.isEmpty() || indentLevels.peek() != spaces) {
                        errors.add(new SyntaxError(token.line, token.column,
                                "Nivel de indentación inválido", token.value.length()));
                    }
                }
            }

            // Asignación de variables
            if (token.type == IDENTIFIER) {
                if (reservedWords.contains(token.value)) {
                    continue; // no marcar como error si es reservada
                }
                if (i + 1 < tokens.size() && tokens.get(i + 1).type == SYMBOL && tokens.get(i + 1).value.equals("=")) {
                    definedVariables.add(token.value);
                }
            }

            // División por cero
            if (token.type == SYMBOL && token.value.equals("/")) {
                if (i + 1 < tokens.size()) {
                    Token next = tokens.get(i + 1);
                    if (next.type == NUMBER && next.value.equals("0")) {
                        errors.add(new SyntaxError(next.line, next.column, "División por cero", next.value.length()));
                    }
                }
            }
        }

        for (String llamada : calledFunctions) {
            if (!definedFunctions.contains(llamada) && !reservedWords.contains(llamada)) {
                errors.add(new SyntaxError(0, 0, "Función llamada pero no definida: '" + llamada + "'", llamada.length()));
            }
        }

        while (!bracketStack.isEmpty()) {
            Token open = bracketStack.pop();
            errors.add(new SyntaxError(open.line, open.column,
                    "Símbolo sin cerrar: '" + open.value + "'", open.value.length()));
        }

        return errors;
    }

    private static boolean esPareja(String abre, String cierra) {
        return (abre.equals("(") && cierra.equals(")")) ||
                (abre.equals("{") && cierra.equals("}")) ||
                (abre.equals("[") && cierra.equals("]"));
    }

    private static String parejaEsperada(String abre) {
        return switch (abre) {
            case "(" -> ")";
            case "[" -> "]";
            case "{" -> "}";
            default -> "?";
        };
    }

    private static void validarFuncion(List<Token> tokens, int i, List<SyntaxError> errors) {
        int idx = i + 1;
        Token nombre = null, parA = null, parC = null, dosPuntos = null;

        while (idx < tokens.size()) {
            Token t = tokens.get(idx);
            if (t.type == IDENTIFIER) {
                nombre = t;
                idx++;
                break;
            } else if (t.type != WHITESPACE && t.type != NEWLINE) break;
            idx++;
        }

        int abiertos = 0;
        while (idx < tokens.size()) {
            Token t = tokens.get(idx);
            if (t.type == SYMBOL && t.value.equals("(")) {
                if (parA == null) parA = t;
                abiertos++;
            } else if (t.type == SYMBOL && t.value.equals(")")) {
                abiertos--;
                if (abiertos == 0) {
                    parC = t;
                    idx++;
                    break;
                }
            } else if (t.type == NEWLINE) break;
            idx++;
        }

        while (idx < tokens.size()) {
            Token t = tokens.get(idx);
            if (t.type == SYMBOL && t.value.equals(":")) {
                dosPuntos = t;
                break;
            } else if (t.type == NEWLINE) break;
            idx++;
        }

        if (nombre == null) {
            Token defToken = tokens.get(i);
            errors.add(new SyntaxError(defToken.line, defToken.column,
                    "Se esperaba un nombre de función después de 'def'", defToken.value.length()));
        }
        if (parA == null || parC == null || abiertos != 0) {
            Token base = nombre != null ? nombre : tokens.get(i);
            errors.add(new SyntaxError(base.line, base.column,
                    "Paréntesis mal definidos en la definición de función", base.value.length()));
        }
        if (dosPuntos == null) {
            Token base = parC != null ? parC : tokens.get(i);
            errors.add(new SyntaxError(base.line, base.column,
                    "Se esperaba ':' al final de la definición de la función", base.value.length()));
        }
    }
}