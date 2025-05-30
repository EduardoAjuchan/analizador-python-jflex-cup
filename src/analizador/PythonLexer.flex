/* Archivo: PythonLexer.flex */
package analizador;

import java_cup.runtime.Symbol;
import static analizador.sym.*;

%%

%public
%class PythonLexer
%unicode
%cup
%line
%column

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

/* Expresiones regulares */
NEWLINE       = \r?\n
WHITESPACE    = [ \t]+
ID            = [a-zA-Z_][a-zA-Z0-9_]*
INT           = [0-9]+
FLOAT         = [0-9]+\.[0-9]+
STRING        = \"([^\"\\]|\\.)*\"|\'([^\'\\]|\\.)*\'
COMMENT       = \#.*

%%

<YYINITIAL> {

    {WHITESPACE}                 { return symbol(WHITESPACE, yytext()); }
    {NEWLINE}                    { return symbol(NEWLINE, yytext()); }

    "def"                        { return symbol(KEYWORD, yytext()); }
    "return"|"if"|"else"|"for"|"while"|"print"|"None"|"True"|"False"|"and"|"or"|"not"
                                 { return symbol(KEYWORD, yytext()); }

    {ID}                         { return symbol(IDENTIFIER, yytext()); }
    {INT}                        { return symbol(NUMBER, yytext()); }
    {FLOAT}                      { return symbol(NUMBER, yytext()); }
    {STRING}                     { return symbol(STRING, yytext()); }

    "=="|"!="|"<="|">="|"<"|">"  { return symbol(OPERATOR, yytext()); }
    "="|"+"|"-"|"*"|"/"|"%"      { return symbol(OPERATOR, yytext()); }

    "("|")"|"["|"]"|"{"|"}"|":"|"," { return symbol(SYMBOL, yytext()); }

    {COMMENT}                    { return symbol(COMMENT, yytext()); }

    .                            { return symbol(ERROR, yytext()); }
}