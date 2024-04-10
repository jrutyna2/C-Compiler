package utils;
import java.util.ArrayList;
import java.util.List;

public class ErrorLogger {
    // A list to hold syntax error messages.
    private static final List<String> syntaxErrors = new ArrayList<>();
    // A separate list for semantic error messages.
    private static final List<String> semanticErrors = new ArrayList<>();

    // Method to add a syntax error with row, column, and a message.
    public static void addSyntaxError(int row, int col, String message) {
        syntaxErrors.add("Error in row " + row + ", col " + col + ": " + message);
    }

    // Method to add a semantic error with row, column, and a message.
    public static void addSemanticError(int row, int col, String message) {
        String fullMessage = "Error in row " + (row+1) + ", col " + (col+1) + ": " + message;
        semanticErrors.add(fullMessage);
        // System.err.println(fullMessage); // Print the error immediately
    }

    // Method to print all collected syntax errors.
    public static void reportSyntaxErrors() {
        if (syntaxErrors.isEmpty()) {
            System.out.println("No syntax errors.");
        } else {
            System.out.println("Syntax Errors:");
            for (String error : syntaxErrors) {
                System.out.println(error);
            }
        }
    }

    // Method to print all collected semantic errors.
    public static void reportSemanticErrors() {
        if (semanticErrors.isEmpty()) {
            System.out.println("No semantic errors.");
        } else {
            System.out.println("Semantic Errors:");
            for (String error : semanticErrors) {
                System.out.println(error);
            }
        }
    }

    // Optionally, methods to check if any errors have been logged.
    public static boolean hasSyntaxErrors() {
        return !syntaxErrors.isEmpty();
    }

    public static boolean hasSemanticErrors() {
        return !semanticErrors.isEmpty();
    }

    // Method to check if any errors have been logged in either list.
    public static boolean hasErrors() {
        return !syntaxErrors.isEmpty() || !semanticErrors.isEmpty();
    }
}
