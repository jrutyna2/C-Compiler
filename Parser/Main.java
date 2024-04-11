import java.io.*;
import absyn.*;
import symbols.*;
import utils.*;

class Main {
    public final static boolean SHOW_TREE = true;

    static public void main(String argv[]) {
        try {
            if (argv.length < 2) {
                System.out.println("Usage: java Main <input file> [-a | -s | -c]");
                return;
            }

            System.out.println("Starting the parser...");
            parser p = new parser(new Lexer(new FileReader(argv[0])));
            Absyn result = (Absyn)(p.parse().value);

            if (result == null) {
                System.out.println("No AST generated.");
                return;
            }

            String baseFileName = "results/" + getBaseFileName(argv[0]);

            // Perform syntactic analysis for all options since it's the foundation
            if ("-a".equals(argv[1]) || "-s".equals(argv[1]) || "-c".equals(argv[1])) {
                printFile(argv[0]);
                generateAndWriteAST(result, baseFileName);
            }

            // Perform semantic analysis if option is -s or -c
            SymbolTable symbolTable = null;
            if ("-s".equals(argv[1]) || "-c".equals(argv[1])) {
                printFile(argv[0]);
                symbolTable = performSemanticAnalysis(result, baseFileName);
            }

            // Proceed with code generation if option is -c
            if ("-c".equals(argv[1])) {
                // Ensure semantic analysis is performed to populate symbolTable
                if (symbolTable == null) {
                    System.out.println("Semantic analysis skipped, aborting code generation.");
                    return;
                }
                generateAndWriteTMCode(result, baseFileName, symbolTable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void generateAndWriteAST(Absyn result, String baseFileName) {
        if (SHOW_TREE) {
            AbsynVisitor visitor = new ShowTreeVisitor();
            result.accept(visitor, 0, false);
            System.out.println(visitor.toString() + '\n');
            writeToFile(baseFileName + ".abs", visitor.toString());
        }
    }

    static SymbolTable performSemanticAnalysis(Absyn result, String baseFileName) {
        System.out.println("Performing Semantic Analysis...");
        // Instantiate the SemanticAnalyzer, creating new SymbolTable
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        result.accept(analyzer, 0, false);
        if (ErrorLogger.hasSemanticErrors()) {
            ErrorLogger.reportSemanticErrors();
            return null;
        }        
        System.out.println(analyzer.toString());    // print Semantics Tree
        String symbolTableString = analyzer.getSymbolTable().toStringRepresentation();
        writeToFile(baseFileName + ".sym", symbolTableString);
        System.out.println("Semantic Analysis DONE - Symbol Table saved to " + baseFileName + ".sym");
        return analyzer.getSymbolTable();
    }

    // static SymbolTable performSemanticAnalysis(Absyn result, String baseFileName) {
    //     System.out.println("Performing Semantic Analysis...");
    //     // Instantiate the SemanticAnalyzer which includes creating a new SymbolTable internally
    //     SemanticAnalyzer analyzer = new SemanticAnalyzer();
    //     // Visit the AST with the SemanticAnalyzer to perform type checks and populate the symbol table
    //     result.accept(analyzer, 0, false);
    //     // Optionally, serialize and write the symbol table contents to a file
    //     String symTableContents = analyzer.getSymbolTable().toString();
    //     writeToFile(baseFileName + ".sym", symTableContents);
    //     System.out.println("Semantic Analysis DONE");
    //     // Return the populated SymbolTable for further use
    //     return analyzer.getSymbolTable();
    // }

    // Stub for the -c option. This needs to be implemented based on your project specifications.
    static void generateAndWriteTMCode(Absyn result, String baseFileName, SymbolTable symbolTable) {
        System.out.println("Starting Code Generation...");
        CodeGenerator codeGenerator = new CodeGenerator(symbolTable);
        // codeGenerator.generateCode(result); // Assuming `generateCode` starts the code generation process
        result.accept(codeGenerator, 0, false); // Pass the AST for traversal and code generation

        String tmCode = codeGenerator.getGeneratedCode();
        // Prepend comments with the file name and intro
        String headerComment = "* C-Minus Compilation to TM Code\n" + "* File: " + baseFileName + ".tm\n";
        tmCode = headerComment + tmCode; // Add the header comments to the beginning of the TM code
        writeToFile(baseFileName + ".tm", tmCode);

        System.out.println("Code Generation DONE");
        System.out.println(tmCode);
    }

    static String getBaseFileName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    static void writeToFile(String fileName, String content) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(content);
        } catch (FileNotFoundException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void printFile(String inputFilePath) {
        System.out.println("Reading file: " + inputFilePath);
        // Print file contents
        System.out.println("\n*********************************************************************\nTesting ["+ inputFilePath + "]:*****");
        System.out.println("*************************");
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("*********************************************************************\n");
    }
}