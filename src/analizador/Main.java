package analizador;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::crearVentana);
    }

    public static void crearVentana() {
        JFrame frame = new JFrame("Analizador de Python - Léxico y Sintáctico");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        JButton btnSeleccionar = new JButton("📂 Seleccionar archivo .py");
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        textPane.setEditable(false);
        JScrollPane scrollTexto = new JScrollPane(textPane);

        JTextArea areaErrores = new JTextArea(6, 20);
        areaErrores.setEditable(false);
        areaErrores.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollErrores = new JScrollPane(areaErrores);
        scrollErrores.setBorder(BorderFactory.createTitledBorder("Errores Sintácticos y Semánticos"));

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.add(btnSeleccionar, BorderLayout.WEST);

        frame.setLayout(new BorderLayout());
        frame.add(panelTop, BorderLayout.NORTH);
        frame.add(scrollTexto, BorderLayout.CENTER);
        frame.add(scrollErrores, BorderLayout.SOUTH);

        btnSeleccionar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int resultado = fileChooser.showOpenDialog(frame);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                if (!archivo.getName().endsWith(".py")) {
                    JOptionPane.showMessageDialog(frame, "Seleccione un archivo .py válido.", "Archivo inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    String codigo = Files.readString(archivo.toPath());
                    List<Token> tokens = PythonLexer.tokenize(codigo);
                    List<SyntaxError> errores = PythonParser.checkSyntax(tokens);

                    mostrarCodigoConEstilo(textPane, tokens, errores);

                    if (errores.isEmpty()) {
                        areaErrores.setText("✅ No se encontraron errores sintácticos ni semánticos.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (SyntaxError err : errores) {
                            sb.append("• Línea ").append(err.line)
                                    .append(", columna ").append(err.column)
                                    .append(": ").append(err.message).append("\n");
                        }
                        areaErrores.setText(sb.toString());
                    }

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error al leer el archivo: " + ex.getMessage(), "Error de lectura", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void mostrarCodigoConEstilo(JTextPane textPane, List<Token> tokens, List<SyntaxError> errores) {
        StyledDocument doc = textPane.getStyledDocument();
        textPane.setText("");

        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style keyword = doc.addStyle("keyword", def);
        StyleConstants.setForeground(keyword, new Color(0, 0, 200));
        StyleConstants.setBold(keyword, true);

        Style builtin = doc.addStyle("builtin", def);
        StyleConstants.setForeground(builtin, new Color(0, 102, 204));
        StyleConstants.setBold(builtin, true);

        Style variable = doc.addStyle("variable", def);
        StyleConstants.setForeground(variable, new Color(30, 30, 120));

        Style number = doc.addStyle("number", def);
        StyleConstants.setForeground(number, new Color(153, 0, 0));

        Style string = doc.addStyle("string", def);
        StyleConstants.setForeground(string, new Color(0, 128, 0));

        Style comment = doc.addStyle("comment", def);
        StyleConstants.setForeground(comment, new Color(150, 150, 150));
        StyleConstants.setItalic(comment, true);

        Style symbol = doc.addStyle("symbol", def);
        StyleConstants.setForeground(symbol, new Color(128, 0, 128));

        Style whitespace = doc.addStyle("whitespace", def);
        StyleConstants.setForeground(whitespace, Color.GRAY);

        Style error = doc.addStyle("error", def);
        StyleConstants.setForeground(error, Color.RED);
        StyleConstants.setUnderline(error, true);

        Map<Integer, String> erroresPorOffset = new HashMap<>();
        int offset = 0;

        for (Token token : tokens) {
            Style estilo = switch (token.type) {
                case sym.KEYWORD -> keyword;
                case sym.BUILTIN -> builtin;
                case sym.STRING -> string;
                case sym.NUMBER -> number;
                case sym.COMMENT -> comment;
                case sym.SYMBOL -> symbol;
                case sym.ERROR -> error;
                case sym.WHITESPACE, sym.INDENT, sym.DEDENT -> whitespace;
                default -> variable;
            };

            for (SyntaxError err : errores) {
                if (token.line == err.line && token.column == err.column) {
                    estilo = error;
                    erroresPorOffset.put(offset, err.message);
                    break;
                }
            }

            try {
                doc.insertString(doc.getLength(), token.value, estilo);
                offset += token.value.length();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        textPane.setToolTipText(null);
        textPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pos = textPane.viewToModel2D(e.getPoint());
                String tooltip = erroresPorOffset.get(pos);
                textPane.setToolTipText(tooltip);
            }
        });

        textPane.setCaretPosition(0);
    }
}