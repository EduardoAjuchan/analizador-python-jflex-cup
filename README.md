# Analizador de Python - Proyecto

## Manual Técnico

Este proyecto es un analizador léxico y sintáctico para un subconjunto del lenguaje Python, desarrollado en Java. Utiliza **JFlex** para la generación del analizador léxico (lexer) y **CUP** para la generación del analizador sintáctico (parser).

### Estructura del Proyecto

```
src/analizador/
  ├── Main.java
  ├── PythonLexer.flex      // Especificación del lexer (JFlex)
  ├── PythonLexer.java      // Lexer generado por JFlex
  ├── PythonParser.cup      // Especificación del parser (CUP)
  ├── PythonParser.java     // Parser generado por CUP
  ├── sym.java              // Símbolos generados por CUP
  ├── SyntaxError.java      // Manejo de errores de sintaxis
  ├── Token.java            // Representación de tokens
```

### Descripción de Componentes

- **Lexer (JFlex):**  
  El archivo `PythonLexer.flex` define las expresiones regulares y reglas para identificar los diferentes tokens del lenguaje Python (palabras reservadas, identificadores, operadores, literales, etc.). JFlex procesa este archivo y genera la clase `PythonLexer.java`, que se encarga de analizar el texto fuente y producir una secuencia de tokens.

- **Parser (CUP):**  
  El archivo `PythonParser.cup` contiene la gramática del subconjunto de Python que se desea analizar, así como las acciones semánticas asociadas. CUP utiliza este archivo para generar las clases `PythonParser.java` y `sym.java`, que implementan el análisis sintáctico y la tabla de símbolos, respectivamente.

- **Main.java:**  
  Es el punto de entrada del programa. Se encarga de inicializar el lexer y el parser, y de procesar el archivo fuente de Python que se desea analizar.

- **SyntaxError.java y Token.java:**  
  Clases auxiliares para el manejo de errores y la representación de tokens.

### Proceso de Generación y Ejecución

1. **Generar el lexer con JFlex:**
   jflex src/analizador/PythonLexer.flex
   Esto creará el archivo `PythonLexer.java`.

2. **Generar el parser con CUP:**
   java -jar java-cup-11b.jar -parser PythonParser -symbols sym src/analizador/PythonParser.cup
   Esto generará los archivos `PythonParser.java` y `sym.java`.

3. **Compilar el proyecto:**
   javac src/analizador/*.java

4. **Ejecutar el analizador:**
   java -cp src analizador.Main archivo.py

### Notas Técnicas

- Asegúrate de tener JFlex y CUP instalados y configurados en tu sistema.
- El analizador está diseñado para un subconjunto de Python, por lo que no soporta todas las características del lenguaje.

---

## Manual de Usuario

Para analizar un archivo Python, simplemente ejecuta el programa desde el archivo principal (Main). El sistema te pedirá que indiques el archivo `.py` que deseas analizar.

El programa mostrará en pantalla si el archivo es válido o si contiene errores léxicos o sintácticos, indicando la línea, columna y una breve descripción del error si lo hubiera.

No necesitas realizar configuraciones adicionales ni conocer comandos específicos, solo asegúrate de tener el archivo Python que deseas analizar disponible.
