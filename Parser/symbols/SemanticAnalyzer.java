package symbols;

import absyn.*;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import utils.*;

public class SemanticAnalyzer implements AbsynVisitor {
    private StringBuilder builder = new StringBuilder();  
    private SymbolTable symbolTable;
    private FunDec currentFunctionContext = null;
    private String currentFunctionName = null;
    private NameTy boolTy = new NameTy(-1, -1, NameTy.BOOL);
    private NameTy intTy = new NameTy(-1, -1, NameTy.INT);
    private boolean isInGlobalScope = true;
    
    public SemanticAnalyzer() {
        symbolTable = new SymbolTable(builder);
    }

    private void indent(int level) {
        for(int i = 0; i < level; i++) {
            builder.append("  "); // 2 spaces for each indentation level
        }
    }

    private void appendLine(String text) {
        builder.append(text);
        builder.append("\n"); // Append a newline character after the text
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    // Method to add an error message
    private void addSemanticError(Absyn node, String message) {
        int row = node.row; // Assuming row is zero-indexed
        int col = node.col; // Assuming col is zero-indexed
        ErrorLogger.addSemanticError(row, col, message);
    }
    
    @Override
    public void visit(ErrorDec errorDec, int level, boolean isAddr) {
        indent(level);
        appendLine("Error detected: " + errorDec.toString());
        appendLine("Cancelling semantic analysis..");
    }

@Override
public void visit(Program program, int level, boolean isAddr) {
    // 1. Explicitly enter the global scope at the start
    symbolTable.enterScope(level, null, false); // level is 0 here for global

    // 2. Insert predefined global functions
    // Assuming input function returns int and takes no parameters
    symbolTable.insert("input", new NodeType("input", new FunDec(0, 0, new NameTy(0, 0, NameTy.INT), "input", null, null), 0));
    // Assuming output function returns void and takes one int parameter
    VarDecList outputParams = new VarDecList(new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INT), "x"), null);
    symbolTable.insert("output", new NodeType("output", new FunDec(0, 0, new NameTy(0, 0, NameTy.VOID), "output", outputParams, null), 0));
    
    // 3. Enter a new scope for program-specific declarations (function definitions, etc.)
    // symbolTable.enterScope(level + 1, null); // Increment level for nested scope
    
    // 4. Process declarations within this scope
    if (program.declarations != null) {
        program.declarations.accept(this, level + 1, isAddr);
    }
    
    // 5. Exit the nested scope to return to the global scope
    // symbolTable.exitScope(level + 1, false, null);

    // Optional: Perform any global-scope specific finalizations here if needed

    // 6. Exit the global scope at the end
    symbolTable.exitScope(level, false, false, null); // Exiting the global scope

    // Print all scopes to verify the contents, including the global functions
    symbolTable.printAllScopes();
}

    @Override
    public void visit(DecList decList, int level, boolean isAddr) {
        // Initialize a stack to hold the declarations
        Deque<Dec> stack = new ArrayDeque<>();
        // Traverse the DecList and push each declaration onto the stack
        while (decList != null && decList.head != null) {
            stack.push(decList.head);
            decList = decList.tail;
        }
        // Pop each declaration off the stack and visit it, effectively in reverse order
        // of how they were added to the stack, which corrects the original reverse order
        while (!stack.isEmpty()) {
            Dec dec = stack.pop();
            dec.accept(this, level, isAddr);
        }
    }

    @Override
    public void visit(FunDec funDec, int level, boolean isAddr) {
        currentFunctionContext = funDec;
        currentFunctionName = funDec.funcName;
        boolean global = isInGlobalScope;

        // First, check if the function is already declared in this scope
        NodeType existingFunc = symbolTable.lookup(funDec.funcName);
        if (existingFunc != null && existingFunc.level == level) {
            // Handle error: redeclaration of the function
            System.err.println("Semantic Error: Function " + funDec.funcName + " is redeclared");
        } else {
            // Add the function declaration to the symbol table
            symbolTable.insert(funDec.funcName, new NodeType(funDec.funcName, funDec, level));
        }

        // Now visit the return type of the function
        if (funDec.result != null) {
            funDec.result.accept(this, level + 1, isAddr);
        }

        // Determine if there is a need to enter a new scope
        boolean hasBodyOrParameters = funDec.body != null || funDec.params != null;
        if (hasBodyOrParameters) {
            // Enter a new scope for the function's parameters and body if either exists
            // Do not increment the level here since we are not inside the function's body yet.
            if (global) isInGlobalScope = false;            
            symbolTable.enterScope(level, currentFunctionName, false);
        }

        // Visit parameters, if any
        if (funDec.params != null) {
            funDec.params.accept(this, level + 1, isAddr);
        }
        // Visit the function body
        if (funDec.body != null) {
            funDec.body.accept(this, level + 1, isAddr);
        }

        // Exit the scope for the function's parameters. 
        // We only increment the level for entering/exiting the body within its own CompoundExp.
        if (hasBodyOrParameters) {
            // Exiting the function's scope after processing its body and parameters
            if (global) isInGlobalScope = true;            
            symbolTable.exitScope(level, true, false, currentFunctionName);
        }        

        currentFunctionContext = null;
        currentFunctionName = null;
    }

    @Override
    public void visit(SimpleDec simpleDec, int level, boolean isAddr) {
        String typeStr = getTypeAsString(simpleDec.typ);

        // Check for void type variable declaration
        if (simpleDec.typ.typ == NameTy.VOID) {
            //v indent(level);
            addSemanticError(simpleDec, "Variable cannot be declared as 'void'");            
            System.err.println("Semantic Error: Variable " + simpleDec.name + " cannot be declared as 'void'");
        }

        // Assuming that we're using 'level' to representFUn scope depth:
        // Here you would check if the variable has already been declared in the current scope.
        NodeType existing = symbolTable.lookup(simpleDec.name);
        if (existing != null && existing.level == level) {
            // Handle error: redeclaration of variable
            //v indent(level);
            addSemanticError(simpleDec, "Variable " + simpleDec.name + " cannot be redeclared");            
            System.err.println("Semantic Error: Variable " + simpleDec.name + " is redeclared");
        } else {
            // If not, add the SimpleDec to the symbol table without conditions
            symbolTable.insert(simpleDec.name, new NodeType(simpleDec.name, simpleDec, level));
        }

        // Only print the declaration if not in the global scope
        if (!isInGlobalScope) {
            indent(level);
            appendLine(simpleDec.name + ": " + typeStr);
        }
    }

    @Override
    public void visit(ArrayDec arrayDec, int level, boolean isAddr) {
        //v indent(level);
        // appendLine("Visiting ArrayDec: " + arrayDec.name + "[]");
        // Check for void type array declaration
        if (arrayDec.typ.typ == NameTy.VOID) {
            //v indent(level);
            addSemanticError(arrayDec, "Variable cannot be declared as 'void'");
            System.err.println("Semantic Error: Array " + arrayDec.name + " cannot be declared as 'void'");
            // Optionally, you can stop processing this declaration or the whole analysis.
            // return; // Early return due to error
        }
        // Similar to SimpleDec, but we're also dealing with an array.
        NodeType existing = symbolTable.lookup(arrayDec.name);
        if (existing != null && existing.level == level) {
            // Handle error: redeclaration of array variable
            //v indent(level);
            addSemanticError(arrayDec, "Variable cannot be redeclared");            
            System.err.println("Semantic Error: Array " + arrayDec.name + " is redeclared");
        } else {
            // If not, add the ArrayDec to the symbol table.
            symbolTable.insert(arrayDec.name, new NodeType(arrayDec.name, arrayDec, level));
        }
        // Continue to visit the type of the ArrayDec if necessary.
        if (arrayDec.typ != null) {
            arrayDec.typ.accept(this, level + 1, isAddr);
        }
    }

    @Override
    public void visit(NameTy nameTy, int level, boolean isAddr) {
        // indent(level);
        // appendLine("Visiting NameTy: " + nameTy.toString());
//        // if (isInGlobalScope) {
//        //     indent(level);
//        //     appendLine(":" + nameTy.toString());
//        // }
    }

    @Override
    public void visit(NilExp nilExp, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting NilExp");

        // Semantic analysis for NilExp:
        // 1. Check if NilExp is in a valid context (if necessary).
        // 2. NilExp may not have any associated actions other than valid context checks.
    }

    // @Override
    // public void visit(VarDecList varDecList, int level, boolean isAddr) {
    //     while (varDecList != null) {
    //         if (varDecList.head != null) {
    //             // indent(level);
    //             // appendLine("Visiting VarDecList");
    //             varDecList.head.accept(this, level, isAddr);
    //         }
    //         varDecList = varDecList.tail;
    //     }
    // }

    @Override
    public void visit(VarDecList varDecList, int level, boolean isAddr) {
        // Initialize a stack to hold the variable declarations
        Deque<VarDec> stack = new ArrayDeque<>();
        // Traverse the VarDecList and push each declaration onto the stack
        while (varDecList != null && varDecList.head != null) {
            stack.push(varDecList.head);
            varDecList = varDecList.tail;
        }
        // Pop each declaration off the stack and visit it, effectively in reverse order
        while (!stack.isEmpty()) {
            VarDec varDec = stack.pop();
            varDec.accept(this, level, isAddr);
        }
    }

    @Override
    public void visit(ExpList stmtList, int level, boolean isAddr) {
        // Initialize a stack to hold the expressions
        Deque<Exp> stack = new ArrayDeque<>();
        // Traverse the ExpList and push each expression onto the stack
        while (stmtList != null && stmtList.head != null) {
            if (!(stmtList.head instanceof NilExp)) {
                stack.push(stmtList.head);
            }
            stmtList = stmtList.tail;
        }
        // Pop each expression off the stack and visit it, effectively in reverse order
        while (!stack.isEmpty()) {
            Exp exp = stack.pop();
            exp.accept(this, level + 1, isAddr);
        }
    }

    // @Override
    // public void visit(ExpList stmtList, int level, boolean isAddr) {
    //     while (stmtList != null) {
    //         if (!(stmtList.head instanceof NilExp)) {
    //             stmtList.head.accept(this, level + 1, isAddr);
    //         }
    //         stmtList = stmtList.tail;
    //     }
    // }

    @Override
    public void visit(CompoundExp compoundExp, int level, boolean isAddr) {
        // Visit local declarations if there are any
        if (compoundExp.localDecs != null) {
            VarDecList localDecs = compoundExp.localDecs;
            while (localDecs != null) {
                if (localDecs.head != null) {
                    localDecs.head.accept(this, level + 1, isAddr);
                }
                localDecs = localDecs.tail;
            }
        }

        // Visit statements if there are any
        if (compoundExp.stmtList != null) {
            ExpList stmtList = compoundExp.stmtList;
            while (stmtList != null) {
                if (stmtList.head != null) {
                    stmtList.head.accept(this, level + 1, isAddr);
                }
                stmtList = stmtList.tail;
            }
        }
    }

    @Override
    public void visit(IfExp ifExp, int level, boolean isAddr) {
        //q indent(level);
        //q appendLine("Visiting IfExp");// //pSystem.out.println("Visiting IfExp");

        if (!isNilOrNull(ifExp.test)) {
            ifExp.test.accept(this, level + 1, isAddr);
        }

        // Visit the then clause
        if (ifExp.thenClause != null) {
            boolean isThenBlock = ifExp.thenClause instanceof CompoundExp;
            symbolTable.enterScope(level, null, isThenBlock);
            ifExp.thenClause.accept(this, level + 1, isAddr);
            symbolTable.exitScope(level, false, isThenBlock, null);
        }

        // And the else clause, if it exists
        if (ifExp.elseClause != null) {
            boolean isElseBlock = ifExp.elseClause instanceof CompoundExp;
            symbolTable.enterScope(level, null, isElseBlock);
            ifExp.elseClause.accept(this, level + 1, isAddr);
            symbolTable.exitScope(level, false, isElseBlock, null);
        }
    }

    @Override
    public void visit(WhileExp whileExp, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting WhileExp");

        if (!isNilOrNull(whileExp.test)) {
            whileExp.test.accept(this, level + 1, isAddr);
        }

        // Visit the body of the loop
        if (!isNilOrNull(whileExp.test)) {
            boolean isBodyBlock = whileExp.body instanceof CompoundExp;
            symbolTable.enterScope(level, null, isBodyBlock);
            whileExp.body.accept(this, level + 1, isAddr);
            symbolTable.exitScope(level, false, isBodyBlock, currentFunctionName);
        }
    }

    @Override
    public void visit(ReturnExp returnExp, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting ReturnExp");

        // Check if there's an expression being returned
        if (returnExp.exp != null) {
            returnExp.exp.accept(this, level + 1, isAddr);

            // If currentFunctionContext is null, it means we're not inside a function, which is an error
            if (currentFunctionContext == null) {
                String errorMsg = "Return statement not inside a function.";
                // ErrorLogger.addSemanticError(returnExp.row, returnExp.col, errorMsg);
                addSemanticError(returnExp, errorMsg);                
            } else if (returnExp.exp != null) {
                // If there's a return expression, it implies the function should not return void.
                // Check if the function's return type is void, which would be an error.
                if (currentFunctionContext.result.typ == NameTy.VOID && !isNilOrNull(returnExp.exp)) {
                    String errorMsg = "'return' with a value, in function returning void";
                    // ErrorLogger.addSemanticError(returnExp.row, returnExp.col, errorMsg);
                    addSemanticError(returnExp, errorMsg);
                } else {
                    // Perform type compatibility check between the function's return type and the return expression's type
                    if (!areTypesCompatible(currentFunctionContext.result, returnExp.exp)) {
                        String errorMsg = "Type mismatch in return statement.";
                        //pSystem.out.println("currentFunctionContext.result: " + currentFunctionContext.result.toString());
                        //pSystem.out.println("returnExp: " + returnExp.toString());

//prints                // Check and print the type of returnExp if it's not null, otherwise print "null"
                        if (returnExp.getType() != null) {
                            //pSystem.out.println("returnExp.getType(): " + returnExp.getType().toString());
                        } else {
                            //pSystem.out.println("returnExp.getType(): null");
                        }

                        // Check and print the type of returnExp.exp if it's not null, otherwise print "null"
                        if (returnExp.exp != null && returnExp.exp.getType() != null) {
                            //pSystem.out.println("returnExp.exp.getType(): " + returnExp.exp.getType().toString());
                        } else {
                            //pSystem.out.println("returnExp.exp.getType(): null");
                        }
                        // if (returnExp.getType().toString())                        
                        // ErrorLogger.addSemanticError(returnExp.row, returnExp.col, errorMsg);
                        addSemanticError(returnExp, errorMsg);
                    }
                }
            } else {
                // For 'return;' with no expression, ensure the current function's return type is void
                if (currentFunctionContext != null && currentFunctionContext.result.typ != NameTy.VOID) {
                    String errorMsg = "Error in line " + returnExp.row + ", column " + returnExp.col + " : Semantic Error: Missing return value in non-void function.";
                    // ErrorLogger.addSemanticError(returnExp.row, returnExp.col, errorMsg);
                    addSemanticError(returnExp, errorMsg);
                }
            }
        }
    }

    @Override
    public void visit(VarExp varExp, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting VarExp: " + varExp.variable.getName());

        // Check if the variable has been declared by looking it up in the symbol table
        NodeType nodeType = symbolTable.lookup(varExp.variable.getName());
        if (nodeType == null) {
            // Error: variable not declared
            String errorMsg = "Variable " + varExp.variable.getName() + " not declared.";
            // ErrorLogger.addSemanticError(varExp.variable.row, varExp.variable.col, errorMsg);
            addSemanticError(varExp.variable, errorMsg);
        } else {
            // If the variable is found, you might want to set the type of the expression
            varExp.setType(nodeType.dec);
        }
    }

    @Override
    public void visit(AssignExp assignExp, int level, boolean isAddr) {
        // Preliminary visits to ensure types are set.
        assignExp.lhs.accept(this, level, isAddr);
        assignExp.rhs.accept(this, level, isAddr);

        // Assuming getType() for lhs and rhs effectively returns a NameTy or allows determination of compatibility.
        NameTy lhsType = extractNameTy(assignExp.lhs.getType());
        NameTy rhsType = extractNameTy(assignExp.rhs.getType());

        if (lhsType != null && rhsType != null && lhsType.typ != rhsType.typ) {
            // Log type mismatch error
            String errorMsg = "Type mismatch in assignment.";
            // ErrorLogger.addSemanticError(assignExp.row, assignExp.col, errorMsg);
            addSemanticError(assignExp, errorMsg);
        }
    }

    @Override
    public void visit(SimpleVar simpleVar, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting SimpleVar: " + simpleVar.getName());
//pSystem.out.println("Visiting SimpleVar: " + simpleVar.getName());
        // Look up the variable in the symbol table to see if it's been declared
        NodeType nodeType = symbolTable.lookup(simpleVar.getName());
        if (nodeType == null) {
            // Variable has not been declared
            System.err.println("Semantic Error: Undeclared variable " + simpleVar.getName() + " at " + simpleVar.row + ", " + simpleVar.col);
        } else {
            // Set the type of the variable expression based on the declaration
            // simpleVar.setType(nodeType.dec);
        }
    }

    @Override
    public void visit(IndexVar indexVar, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting IndexVar: " + indexVar.getName());

        // First, ensure the variable itself is declared and is an array
        NodeType nodeType = symbolTable.lookup(indexVar.getName());
        if (nodeType == null) {
            String errorMsg = "Undeclared variable '" + indexVar.getName() + "' used as an array at line " + indexVar.row + ", column " + indexVar.col;
            addSemanticError(indexVar, errorMsg);
        } else if (!(nodeType.dec instanceof ArrayDec)) {
            String errorMsg = "Variable '" + indexVar.getName() + "' is not an array type at line " + indexVar.row + ", column " + indexVar.col;
            addSemanticError(indexVar, errorMsg);
        } else {
            // If it is an array, now check the index expression
            if (indexVar.index != null) {
                indexVar.index.accept(this, level + 1, isAddr);
                // Use the isInteger helper function to check if the index expression is an integer
                if (!isInteger(indexVar.index)) {
                    String errorMsg = "Array index must be an integer at line " + indexVar.row + ", column " + indexVar.col;
                    addSemanticError(indexVar, errorMsg);
                }
            }
        }
    }

    @Override
    public void visit(OpExp opExp, int level, boolean isAddr) {
        //qindent(level);
        //qappendLine("Visiting OpExp");
//pSystem.out.println("\nVisiting OpExp: " + opExp.toString());
//pSystem.out.println("left: " + opExp.left.toString());
//pSystem.out.println("right: " + opExp.right.toString());
        // Visit the left and right operands to ensure they are processed and their types are set
        opExp.left.accept(this, level + 1, isAddr);
        if (!isNilOrNull(opExp.right)) { // Unary operations won't have a right operand
            opExp.right.accept(this, level + 1, isAddr);
        } 
        // else {
        //     //pSystem.out.println("invalid case: right operand isNilOrNull");                  
        //     addSemanticError(opExp, "Operation missing right operand");
        // }

        // Assuming OpExp defines PLUS, MINUS, etc., for binary operations, and NOT, UMINUS for unary
        switch (opExp.op) {
            case OpExp.PLUS:
            case OpExp.MINUS:
            case OpExp.MUL:
            case OpExp.DIV:
                // // For arithmetic operations, both operands should be integers
                // if (!isNilOrNull(opExp.right)) {
                //     if (isInteger(opExp.left) == isInteger(opExp.right) == true) {
                //         // Valid case: both operands are of the same type
                //         //pSystem.out.println("Valid case: Arithmetic operands are of the same type:\n\tleft: " + opExp.left.toString() + "\n\tright: " + opExp.right.toString());
                //     } else {
                //         //pSystem.out.println("left: " + isInteger(opExp.left));
                //         //pSystem.out.println("right: " + isInteger(opExp.right));
                        
                //         //pSystem.out.println("invalid case: Arithmetic operands are of NOT same type:\n\tleft: " + opExp.left.toString() + "\n\tright: " + opExp.right.toString());
                //         addSemanticError(opExp, "Arithmetic operations require integer operands");
                //     }
                // } else {
                //     //pSystem.out.println("invalid case: right operand isNilOrNull");                  
                //     addSemanticError(opExp, "Arithmetic operation missing right operand");
                // }
                // break;
                // For arithmetic operations, both operands should be integers
                if (areTypesCompatible(intTy, opExp.left) && areTypesCompatible(intTy, opExp.right)) {
                    // Valid case: both operands are of the same type
                    //pSystem.out.println("Valid case: Arithmetic operands are of the same type:\n\tleft: " + opExp.left.toString() + "\n\tright: " + opExp.right.toString());
                    opExp.type = intTy;
                } else {
                    //pSystem.out.println("left: " + areTypesCompatible(intTy, opExp.left));
                    //pSystem.out.println("right: " + areTypesCompatible(intTy, opExp.right));

                    //pSystem.out.println("invalid case: Arithmetic operands are of NOT same type:\n\tleft: " + opExp.left.toString() + "\n\tright: " + opExp.right.toString());
                    addSemanticError(opExp, "Arithmetic operations require integer operands");
                }
                break;
            case OpExp.EQ:
            case OpExp.NE:
                // For EQ and NE, operands can be both int or both bool, ensuring type consistency
                if (areTypesCompatible(intTy, opExp.left) && areTypesCompatible(intTy, opExp.right)) {
                    opExp.type = intTy;
                } else if (areTypesCompatible(boolTy, opExp.left) && areTypesCompatible(boolTy, opExp.right)) {
                    opExp.type = boolTy;
                } else {
                    //pSystem.out.println("invalid case: both operands are of NOT same type");
                    addSemanticError(opExp, "Equality operations require operands of the same type (both int or both bool)");
                }
                break;
            case OpExp.LT:
            case OpExp.LE:
            case OpExp.GT:
            case OpExp.GE:
                // For comparison operations, operands must be of the same type, either int or bool
                // Simplified check assuming both sides must be int for simplicity
                if (areTypesCompatible(intTy, opExp.left) && areTypesCompatible(intTy, opExp.right)) {
                    opExp.type = intTy;
                } else {
                    addSemanticError(opExp, "Comparison operations require operands of int type");
                }
                break;
            case OpExp.NOT:
                if (areTypesCompatible(boolTy, opExp.left)) {
                    opExp.type = boolTy;
                } else {
                    addSemanticError(opExp, "'!' operation requires a boolean operand");
                }
                break;
            case OpExp.UMINUS:
                // Unary operations: NOT should have a boolean operand, UMINUS an integer operand
                if (areTypesCompatible(intTy, opExp.left)) {
                    opExp.type = intTy;
                } else {
                    addSemanticError(opExp, "Unary minus operation requires an integer operand");
                }                
                break;
            // Handle logical operations AND, OR if your language supports them
        }
    }

    @Override
    public void visit(IntExp intExp, int level, boolean isAddr) {
//pSystem.out.println("Visiting IntExp: " + intExp.toString());
        int value = intExp.value;
        if (value < 0 || value > 100) {
            System.err.println("Semantic Error: Integer value out of range (0-100)");
        }
    }

   @Override
    public void visit(BoolExp boolExp, int level, boolean isAddr) {
        indent(level);
        appendLine("BoolExp: " + boolExp.value);
//pSystem.out.println("Visiting BoolExp: " + boolExp.toString());

        // Perform semantic analysis specific to BoolExp
        // if (!isInValidContext(boolExp)) {
        //     addSemanticError(boolExp, "BoolExp used in an invalid context");            
        // }

        if (!isValidBoolExp(boolExp)) {
            addSemanticError(boolExp, "BoolExp is not well-formed");                      
        }
    }

    private boolean isInValidContext(BoolExp boolExp) {

        return false;

    }

    // Check if the boolean expression is well-formed
    private boolean isValidBoolExp(BoolExp boolExp) {
        // Check if the expression contains valid boolean operands and operators
        return boolExp != null && (boolExp.value == true || boolExp.value == false);
    }

    @Override
    public void visit(CallExp callExp, int level, boolean isAddr) {
        //v indent(level);
        //pSystem.out.println("Visiting CallExp");

        // Ensure the function is declared
        NodeType funcNode = symbolTable.lookup(callExp.func);
        if (funcNode == null || !(funcNode.dec instanceof FunDec)) {
            addSemanticError(callExp, "Function '" + callExp.func + "' is not declared.");
            return; // Exit if the function is not declared to avoid further errors
        }
        FunDec funcDec = (FunDec) funcNode.dec;

        // Visit each argument expression for type checking or other validation as needed
        ExpList arg = callExp.args;
        while (arg != null) {
            arg.head.accept(this, level + 1, isAddr); // Process each argument
            arg = arg.tail;
        }
    }

    // Methods to handle other types of nodes...
    private boolean isNilOrNull(Exp exp) {
        // Check if the expression is null or an instance of NilExp.
        return exp == null || exp instanceof NilExp;
    }
    
    private boolean isInteger(Exp exp) {
        // Directly check if the expression is an instance of IntExp.
        if (exp instanceof IntExp) {
            return true;
        }
        NodeType node = symbolTable.lookup(exp.toString());
        if (node != null) {
          String typeName = symbolTable.mapDecToSimpleType(node.dec);
          if (typeName == "int") {
            return true;
          }
        }
        
        // If not, try to parse the expression's toString value as an integer.
        try {
            Integer.parseInt(exp.toString());
            return true; // Parsing succeeded, so it's an integer representation.
        } catch (NumberFormatException e) {
            return false; // Parsing failed, so it's not an integer representation.
        }
    }

    private boolean isBoolean(Exp exp) {
        if (exp instanceof BoolExp) {
            return true;
        } else if (exp.toString() == "true" || exp.toString() == "false") {
            return true;
        }
        NodeType node = symbolTable.lookup(exp.toString());
        if (node != null) {
          String typeName = symbolTable.mapDecToSimpleType(node.dec);
          if (typeName == "bool") {
            return true;
          }
        }
        //else if (exp instanceof CallExp) {
        //     String funcName = exp.func;
        //     NodeType nodeType = symbolTable.lookup(exp.func);
        //     if (areTypesCompatible(boolTy, nodeType.dec))
        //     return true;
        // }
        return false;
    }
private boolean areTypesCompatible(NameTy expected, Exp actual) {
    //pSystem.out.println("\nareTypesCompatible\nexpected.typ: " + expected.toString()+"\nactual: "+actual.toString()); // Print the expected type
    //pSystem.out.println("Actual instance class: " + actual.getClass().getSimpleName());

    if ((expected.typ == NameTy.INT) && isInteger(actual)) {
      //pSystem.out.println("Actual is IntExp or explicitly integer");
      return true;
    } else if ((expected.typ == NameTy.BOOL) && isBoolean(actual)) {
      //pSystem.out.println("Actual is BoolExp or explicitly integer");
      return true;
    }

    if (actual.getType() instanceof VarDec) {
        VarDec varDec = (VarDec) actual.getType();
        //pSystem.out.println("VarDec.typ.typ: " + varDec.typ.typ); // Print the type of the variable
        return expected.typ == varDec.typ.typ; // Assuming VarDec contains a NameTy object as 'typ'
    } else if (actual.getType() instanceof FunDec) {
        FunDec funDec = (FunDec) actual.getType();
        //pSystem.out.println("FunDec.result.typ: " + funDec.result.typ); // Print the return type of the function
        return expected.typ == funDec.result.typ;
    }
    
    // For BoolExp or explicitly boolean expressions
    if (actual instanceof BoolExp || isBoolean(actual)) {
        //pSystem.out.println("Actual is BoolExp or explicitly boolean"); // Indicative print
        if (expected.typ == NameTy.BOOL) return true;
    // For IntExp or explicitly integer expressions
    } else if (actual instanceof IntExp || isInteger(actual)) {
        //pSystem.out.println("Actual is IntExp or explicitly integer"); // Indicative print
        if (expected.typ == NameTy.INT) return true;
    // For Nil or null expressions, checking if expected is VOID
    } else if (isNilOrNull(actual) && expected.typ == NameTy.VOID) {
        //pSystem.out.println("Actual is Nil or null and expected is VOID"); // Indicative print
        return true;
    } else if (actual instanceof CallExp) {
        CallExp callExp = (CallExp) actual;
        NodeType funcNode = symbolTable.lookup(callExp.func);
        if (funcNode != null && funcNode.dec instanceof FunDec) {
            FunDec funDec = (FunDec) funcNode.dec;
            // Compare the function's return type with the expected type
            if (expected.typ == funDec.result.typ) {
                return true;
            }
        } else {
            //pSystem.out.println("Function " + callExp.func + " not found in symbol table.");
        }
    } else if (actual instanceof VarExp) {
        //pSystem.out.println("actual is instance of VarExp");
        VarExp varExp = (VarExp) actual;
        // Assuming VarExp's variable has a way to determine its type, possibly through symbol table lookup
        NodeType nodeType = symbolTable.lookup(varExp.variable.toString());
        if (nodeType != null && nodeType.dec instanceof VarDec) {
            VarDec varDec = (VarDec) nodeType.dec;
            // Compare the variable's type with the expected type
            if (expected.typ == varDec.typ.typ) {
                return true;
            }
        } else {
            //pSystem.out.println("Variable " + varExp.variable.toString() + " not found in symbol table or not a VarDec.");
        }
    } else if (actual instanceof OpExp) {
        OpExp opExp = (OpExp) actual;
        if (opExp.type != null) {
            // Check if the type of the OpExp matches the expected type
            return expected.typ == opExp.type.typ;
        } else {
            // If opExp.type is null, it means the type hasn't been determined/set yet,
            // which might indicate a need to evaluate or a missing piece in the analysis.
            //pSystem.out.println("OpExp type is null, indicating missing type evaluation.");
            // Depending on your language semantics, you might handle this case differently.
            // For simplicity, we're returning false here, but you might need a more nuanced approach.
            return false;
        }
        // Assume arithmetic operations result in an integer type
        // if (opExp.op == OpExp.PLUS || opExp.op == OpExp.MINUS || opExp.op == OpExp.MUL || opExp.op == OpExp.DIV) {
        //     // return expected.typ == NameTy.INT; // Check if the expected type is integer
        // }
        // // Assume comparison operations result in a boolean type
        // else if (opExp.op == OpExp.EQ || opExp.op == OpExp.NE || opExp.op == OpExp.LT || opExp.op == OpExp.LE || opExp.op == OpExp.GT || opExp.op == OpExp.GE) {
        //     // return expected.typ == NameTy.BOOL; // Check if the expected type is boolean
        // }
        // // Expecting the result to be boolean since it's a negation
        // else if (opExp.op == OpExp.NOT) {
        //     // return expected.typ == NameTy.BOOL;
        // }
        // // Expecting both the operand and the result to be integer for unary minus
        // else if (opExp.op == OpExp.UMINUS) {
        //     // return expected.typ == NameTy.INT;
        // }
    } else {
        // Log unexpected cases
        //pSystem.out.println("Unexpected case encountered in areTypesCompatible");
    }
    return false;
}

    private NameTy extractNameTy(Dec dec) {
        // This method is a placeholder for extracting NameTy from Dec.
        // Implement this based on how your Dec instances relate to NameTy.
        if (dec instanceof VarDec) {
            return ((VarDec) dec).typ;
        } else if (dec instanceof FunDec) {
            return ((FunDec) dec).result;
        } else if (dec instanceof ErrorDec) {
            appendLine("extractNameTy: dec instanceof ErrorDec");
            return null;
        }
        return null;
    }

    private String getTypeAsString(NameTy nameTy) {
        switch (nameTy.typ) {
            case NameTy.INT:
                return "int";
            case NameTy.BOOL:
                return "bool";
            default:
                return "unknown";
        }
    }
    private boolean areDecTypesCompatible(NameTy expected, Dec actual) {
        // First, check if the actual expression's type (dtype) is one of the Dec subclasses that contains type information
        //appendLine("\n***** areDecTypesCompatible(NameTy expected, Dec actual) *****");
        if (actual instanceof VarDec) {
            VarDec varDec = (VarDec) actual;
            //appendLine("***** actual instanceof VarDec: "+varDec.name+" *****");
            //appendLine("\t*** NameTy: " + expected.toString() + "\tDec: " + varDec.typ.typ+" *****");
            // Now compare the types
            return expected.typ == varDec.typ.typ; // Assuming VarDec contains a NameTy object as 'typ'
        } else if (actual instanceof FunDec) {
            FunDec funDec = (FunDec) actual;
            //appendLine("***** actual instanceof FunDec: "+funDec.funcName+" *****");
            //appendLine("\t*** NameTy: " + expected.toString() + "\tDec: " + funDec.result.typ+" *****");
            // If it's a function, compare the function's return type
            return expected.typ == funDec.result.typ;
        }
        //appendLine("***** actual instanceof NEITHER *****");
        //appendLine("\t*** NameTy: " + expected.toString() + "\tDec: " + actual.toString()+" *****");
        //appendLine("\t*** NameTy: " + expected.typ + "\tDec: " + actual.toString()+" *****");
        
        // If the type doesn't match any known Dec subclass that contains direct type information, return false
        return false;
    }
    // private boolean areTypesCompatible(NameTy expected, Exp actual) {
    //     if (actual.getType() instanceof VarDec) {
    //         VarDec varDec = (VarDec) actual.getType();
    //         return expected.typ == varDec.typ.typ; // Assuming VarDec contains a NameTy object as 'typ'
    //     } else if (actual.getType() instanceof FunDec) {
    //         FunDec funDec = (FunDec) actual.getType();
    //         return expected.typ == funDec.result.typ;
    //     }
        
    //     if (actual instanceof BoolExp || isBoolean(actual)) {
    //         if (expected.typ == 0) return true;
    //     } else if (actual instanceof IntExp || isInteger(actual)) {
    //         if (expected.typ == 1) return true;
    //     } else if (isNilOrNull(actual) && expected.typ == 2) {
    //         return true;
    //     } else {
    //     }
    //     return false;
    // }

    // private boolean areTypesCompatible(NameTy expected, Exp actual) {
    //     // First, check if the actual expression's type (dtype) is one of the Dec subclasses that contains type information
    //     //appendLine("\n***** areTypesCompatible(NameTy expected, Exp actual) *****");
    //     if (actual.getType() instanceof VarDec) {
    //         VarDec varDec = (VarDec) actual.getType();
    //         //appendLine("***** actual.getType() instanceof VarDec: "+varDec.name+" *****");
    //         //appendLine("\t*** NameTy: " + expected.toString() + "\tExp: " + varDec.typ.typ+" *****");
    //         // Now compare the types
    //         return expected.typ == varDec.typ.typ; // Assuming VarDec contains a NameTy object as 'typ'
    //     } else if (actual.getType() instanceof FunDec) {
    //         FunDec funDec = (FunDec) actual.getType();
    //         //appendLine("***** actual.getType() instanceof FunDec: "+funDec.funcName+" *****");
    //         //appendLine("\t*** NameTy: " + expected.toString() + "\tExp: " + funDec.result.typ+" *****");
    //         // If it's a function, compare the function's return type
    //         return expected.typ == funDec.result.typ;
    //     }
    //     //appendLine("***** actual.getType() instanceof NEITHER *****");
    //     //appendLine("\t*** NameTy: " + expected.toString() + "\tExp: " + actual.toString()+" *****");
    //     //appendLine("\t*** NameTy: " + expected.typ + "\tExp: " + actual.toString()+" *****");
        
    //     if (actual instanceof BoolExp || isBoolean(actual)) {
    //         //appendLine("\t*** actual instanceof BoolExp *****\n");
    //         if (expected.typ == 0) return true;
    //     } else if (actual instanceof IntExp || isInteger(actual)) {
    //         //appendLine("\t*** actual instanceof IntExp *****\n");
    //         if (expected.typ == 1) return true;
    //     } else if (isNilOrNull(actual) && expected.typ == 2) {
    //         //appendLine("\t*** actual instanceof VOID && empty return *****\n");          
    //         return true;
    //     } 
    //     // else {
    //     //     //appendLine("\t*** actual instanceof OTHER *****");
    //     //     // //appendLine("\t*** NameTy: " + expected.toString() + " Exp: " + actual.getType().toString()+" *****\n");
    //     // }
    //     // If the type doesn't match any known Dec subclass that contains direct type information, return false
    //     return false;
    // }
    // Rest of the semantic analysis logic...
}
