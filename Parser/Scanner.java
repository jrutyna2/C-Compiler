import java.io.InputStreamReader;
import java_cup.runtime.Symbol;
import java.io.FileReader;


public class Scanner {
  private Lexer scanner = null;

  public Scanner( Lexer lexer ) {
    scanner = lexer; 
  }

  public Symbol getNextToken() throws java.io.IOException {
    return scanner.next_token();
  }

  // public static void main(String argv[]) {
  //   try {
  //     Scanner scanner = new Scanner(new Lexer(new InputStreamReader(System.in)));
  //     Symbol tok = null;
  //     while( (tok=scanner.getNextToken()) != null )
  //       System.out.println(sym.terminalNames[tok.sym]);
  //   }
  //   catch (Exception e) {
  //     System.out.println("Unexpected exception:");
  //     e.printStackTrace();
  //   }
  // }

  public static void main(String argv[]) {
    try {
      // Ensure there's a command-line argument provided for the file path
      // if (argv.length < 1) {
      //   System.out.println("Usage: java Scanner <filename>");
      //   return;
      // }
      // Scanner scanner = new Scanner(new Lexer(new InputStreamReader(System.in)));
      Scanner scanner = new Scanner(new Lexer(new FileReader(argv[0])));
      Symbol tok = null;
      while ((tok = scanner.getNextToken()) != null) {
          // if (tok.sym == sym.ID) {
          //     System.out.println("ID " + tok.value);
          // } else if (tok.sym == sym.NUM) {
          //     System.out.println("NUM " + tok.value);
          // } else {
              System.out.println(sym.terminalNames[tok.sym]);
          // }
      }
    }
    catch (Exception e) {
      System.out.println("Unexpected exception:");
      e.printStackTrace();
    }
  }
}

/*

make clean
make
java -cp /usr/share/java/cup.jar:. Main ../func_decl.cm


git status
git add .
git commit -m "update"
git push origin main

Parser/absyn/AbsynVisitor.java
Parser/ShowTreeVisitor.java

Checkpoint One Description.txt
C-Specification.txt

*/