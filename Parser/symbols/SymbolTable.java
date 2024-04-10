// File: symbols/SymbolTable.java
package symbols;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;
import absyn.*;

public class SymbolTable {
    private Stack<HashMap<String, NodeType>> scopes;
    private List<HashMap<String, NodeType>> allScopes; // New list to store all scopes

    public SymbolTable() {
        this.scopes = new Stack<>();
        this.allScopes = new ArrayList<>(); // Initialize the list
    }

    public void enterScope(int level, String functionName) {
        this.scopes.push(new HashMap<>());
        if (functionName != null) {
            printIndented("Entering the scope for function " + functionName + " :", level);
        } else {
            //String scopeType = level == 0 ? "Entering the global scope:" : "Entering a new scope: ";
            String scopeType = level == 0 ? "Entering the global scope:" : "";
            printIndented(scopeType, level);
        }
    }

    public void exitScope(int level, boolean isFunctionScope) {
        if (!this.scopes.isEmpty()) {
            HashMap<String, NodeType> exitedScope = this.scopes.pop();
            allScopes.add(new HashMap<>(exitedScope));
            if (isFunctionScope) {
                printIndented("Leaving the function scope.", level);
            } else {
                if (this.scopes.isEmpty()) {
                    printIndented("Leaving the global scope.", level);
                } 
                // else {
                //     printIndented("Exiting a scope: ", level);
                // }
            }
        }
    }

    public void printSymbolImmediately(String name, NodeType nodeType, int level) { 
        String typeString = mapDecToSimpleType(nodeType.dec); // Assuming this method maps the node type to a string representation.
        printIndented(name + ":" + typeString, level + 1);
    }

    private void printIndented(String message, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    "); // Assuming 4 spaces per indentation level
        }
        System.out.println(message);
    }
    
    // Insert a new NodeType into the current scope
    public void insert(String name, NodeType nodeType) {
        if (!this.scopes.isEmpty()) {
            HashMap<String, NodeType> currentScope = this.scopes.peek();
            currentScope.put(name, nodeType);
            // System.out.println("Inserted " + name + " into scope level " + (scopes.size() - 1));
        }
    }

    // Lookup a NodeType by name, checking from innermost to outermost scope
    public NodeType lookup(String name) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            HashMap<String, NodeType> scope = this.scopes.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // Not found
    }

    // Remove a NodeType from the current scope
    public void delete(String name) {
        if (!this.scopes.isEmpty()) {
            HashMap<String, NodeType> currentScope = this.scopes.peek();
            currentScope.remove(name);
        }
    }

    public void printSymbolTable() {
        System.out.println("\nSymbol Table:");
        // Iterate through each scope, starting from the global scope (bottom of the stack)
        for (int scopeLevel = 0; scopeLevel < scopes.size(); scopeLevel++) {
            //System.out.println("Scope level: " + scopeLevel);
            HashMap<String, NodeType> scope = scopes.get(scopeLevel);
            for (String key : scope.keySet()) {
                NodeType nodeType = scope.get(key);
                System.out.println("  " + nodeType);
            }
        }
        System.out.println("\n");
    }

    public String toStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        int scopeLevel = 0;
        for (HashMap<String, NodeType> scope : allScopes) {
            sb.append("Scope level: ").append(scopeLevel++).append("\n");
            for (Map.Entry<String, NodeType> entry : scope.entrySet()) {
                String symbol = entry.getKey();
                NodeType nodeType = entry.getValue();
                String typeString = mapDecToSimpleType(nodeType.dec);
                sb.append("    ").append(symbol).append(": ").append(typeString).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String mapDecToSimpleType(Dec dec) {
        // Handling simple variable declarations
        if (dec instanceof SimpleDec) {
            SimpleDec simpleDec = (SimpleDec) dec;
            return mapTypeName(simpleDec.typ.typ);
        }
        // Handling function declarations
        else if (dec instanceof FunDec) {
            FunDec funDec = (FunDec) dec;
            return mapTypeName(funDec.result.typ); // Accessing the return type through the result field
        }
        return "unknown";
    }

    public String mapTypeName(int typeConst) {
        switch (typeConst) {
            case NameTy.INT:
                return "int";
            case NameTy.BOOL:
                return "bool";
            case NameTy.VOID:
                return "void";
            default:
                return "unknown";
        }
    }

    private void printScopeContents(HashMap<String, NodeType> scope, int level) {
        for (Map.Entry<String, NodeType> entry : scope.entrySet()) {
            String symbol = entry.getKey();
            NodeType nodeType = entry.getValue();
            String typeString = mapDecToSimpleType(nodeType.dec);
            printIndented(symbol + ": " + typeString, level + 1);
        }
    }

    public void printAllScopes() {
        //System.out.println("\nComplete Symbol Table History:");
        int scopeLevel = 0;
        for (HashMap<String, NodeType> scope : allScopes) {
            //System.out.println("Scope level: " + scopeLevel++);
            for (Map.Entry<String, NodeType> entry : scope.entrySet()) {
                String symbol = entry.getKey();
                NodeType nodeType = entry.getValue();
                String typeString = mapDecToSimpleType(nodeType.dec); // Use mapDecToSimpleType
                printIndented(symbol + ":" + typeString, scopeLevel); // Adjusted to use the typeString
            }
        }
        System.out.println();
    }
}