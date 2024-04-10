/* --------------------------Usercode Section------------------------ */
import java_cup.runtime.*;
%%
/* -----------------Options and Declarations Section----------------- */
/*  The name of the class JFlex will create will be Lexer.
    Will write the code to the file Lexer.java. */
%class Lexer
%eofval{
  return null;
%eofval};
/*  The current line number can be accessed with the variable yyline
    and the current column number with the variable yycolumn. */
%line
%column
/*  Will switch to a CUP compatibility mode to interface with a CUP
    generated parser. */
%cup
/*  Declarations
    Code between %{ and %}, both of which must be at the beginning of a
    line, will be copied letter to letter into the lexer class source.
    Here you declare member variables and functions that are used inside
    scanner actions.  */
%{
    /* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
   private Symbol symbol(int type) {
      // System.out.println("Token: " + sym.terminalNames[type]);
      return new Symbol(type, yyline, yycolumn);
   }
   /* Also creates a new java_cup.runtime.Symbol with information
      about the current token, but this object has a value. */
   private Symbol symbol(int type, Object value) {
      // System.out.println("Token: " + sym.terminalNames[type] + ", Value: " + value);
      return new Symbol(type, yyline, yycolumn, value);
   }
%}
/*  Macro Declarations
    These declarations are regular expressions that will be used latter
    in the Lexical Rules Section. */
LineTerminator = \r|\n|\r\n /* A line terminator is a \r (carriage return), \n (line feed), or \r\n. */
WhiteSpace     = {LineTerminator} | [ \t\f] /* White space is a line terminator, space, tab, or form feed. */
digit = [0-9]
%%
/* ------------------------Lexical Rules Section---------------------- */
/* This section contains regular expressions and actions, i.e. Java
code, that will be executed when the scanner matches the associated
regular expression. */

"int"       { return symbol(sym.INT); }
"bool"      { return symbol(sym.BOOLEAN); }
"void"      { return symbol(sym.VOID); }
"if"        { return symbol(sym.IF); }
"while"     { return symbol(sym.WHILE); }
"else"      { return symbol(sym.ELSE); }
"return"    { return symbol(sym.RETURN); }
/* Boolean Literals - Handled separately for clarity */
"true"                  { return symbol(sym.TRUTH, Boolean.valueOf(yytext())); }
"false"                 { return symbol(sym.TRUTH, Boolean.valueOf(yytext())); }

"+"  { return symbol(sym.PLUS); }
"-"  { return symbol(sym.MINUS); }
"*"  { return symbol(sym.TIMES); }
"/"  { return symbol(sym.DIVIDE); }
"<"  { return symbol(sym.LT); }
"="  { return symbol(sym.ASSIGN); }
";"  { return symbol(sym.SEMI); }
","  { return symbol(sym.COMMA); }
"("  { return symbol(sym.LPAREN); }
")"  { return symbol(sym.RPAREN); }
"["  { return symbol(sym.LBRACKET); }
"]"  { return symbol(sym.RBRACKET); }
// Other relational operators removed/commented out if not used
"<=" { return symbol(sym.LE); }
">"  { return symbol(sym.GT); }
">=" { return symbol(sym.GE); }
"==" { return symbol(sym.EQ); }
"!=" { return symbol(sym.NE); }
"~"  { return symbol(sym.NOT); }
"||" { return symbol(sym.OR); }
"&&" { return symbol(sym.AND); }
// Braces may be removed if not handling compound statements
"{"  { return symbol(sym.LBRACE); }
"}"  { return symbol(sym.RBRACE); }

{digit}+          { return symbol(sym.NUM, Integer.valueOf(yytext())); }  /* Numeric Literal */
{WhiteSpace}+     { /* skip whitespace */ }
"//" [^\r\n]*     { /* skip single-line comments */ }
"/*" [^*]* "*/"   { /* skip multi-line comments. */ }
[_a-zA-Z][_a-zA-Z0-9]*  { return symbol(sym.ID, yytext()); }              /* Identifier */
.                       { return symbol(sym.ERROR); }







// {number}           { return symbol(sym.NUM, yytext()); }
// {identifier}       { return symbol(sym.ID, yytext()); }
