package symbols;

import absyn.*;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import utils.*;

public class SemanticAnalyzer implements AbsynVisitor {
    private SymbolTable symbolTable;
    private FunDec currentFunctionContext = null;
    private NameTy boolTy = new NameTy(-1, -1, NameTy.BOOL);
    private NameTy intTy = new NameTy(-1, -1, NameTy.INT);
    private boolean isInGlobalScope = true;
    private StringBuilder builder = new StringBuilder();

    private void indent(int level) {
        for(int i = 0; i < level; i++) {
            builder.append("  "); // Assuming 2 spaces for each indentation level
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

    public SemanticAnalyzer() {
        symbolTable = new SymbolTable();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
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
        isInGlobalScope = true; // We start in the global scope
        symbolTable.enterScope(level, null);
        symbolTable.insert("input", new NodeType("input", new FunDec(0, 0, new NameTy(0, 0, NameTy.INT), "input", null, null), 0));
        VarDecList outputParams = new VarDecList(new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INT), "x"), null);
        symbolTable.insert("output", new NodeType("output", new FunDec(1, 0, new NameTy(0, 0, NameTy.VOID), "output", outputParams, null), 0));
        if (program.declarations != null) {
            program.declarations.accept(this, level + 1, isAddr);
        }
        symbolTable.exitScope(level, false);
        symbolTable.printAllScopes();
        isInGlobalScope = false; // Reset the flag after leaving the global scope
    }
    @Override
    public void visit(DecList decList, int level, boolean isAddr) {
        Deque<Dec> stack = new ArrayDeque<>();
        while (decList != null && decList.head != null) {
            stack.push(decList.head);
            decList = decList.tail;
        }
        while (!stack.isEmpty()) {
            Dec dec = stack.pop();
            dec.accept(this, level, isAddr);
        }
    }

    @Override
    public void visit(SimpleDec simpleDec, int level, boolean isAddr) {
        String typeStr = getTypeAsString(simpleDec.typ);
        if (simpleDec.typ.typ == NameTy.VOID) {
            indent(level);
            addSemanticError(simpleDec, "Variable cannot be declared as 'void'");            
            System.err.println("Semantic Error: Variable " + simpleDec.name + " cannot be declared as 'void'");
        }
        NodeType existing = symbolTable.lookup(simpleDec.name);
        if (existing != null && existing.level == level) {
            indent(level);
            addSemanticError(simpleDec, "Variable " + simpleDec.name + " cannot be redeclared");            
            System.err.println("Semantic Error: Variable " + simpleDec.name + " is redeclared");
        } else {
            symbolTable.insert(simpleDec.name, new NodeType(simpleDec.name, simpleDec, level));
        }
        if (!isInGlobalScope) {
            indent(level);
            appendLine(simpleDec.name + ": " + typeStr);
        }
    }
    @Override
    public void visit(ArrayDec arrayDec, int level, boolean isAddr) {
        indent(level);
        if (arrayDec.typ.typ == NameTy.VOID) {
            indent(level);
            addSemanticError(arrayDec, "Variable cannot be declared as 'void'");
            System.err.println("Semantic Error: Array " + arrayDec.name + " cannot be declared as 'void'");
        }
        NodeType existing = symbolTable.lookup(arrayDec.name);
        if (existing != null && existing.level == level) {
            indent(level);
            addSemanticError(arrayDec, "Variable cannot be redeclared");            
            System.err.println("Semantic Error: Array " + arrayDec.name + " is redeclared");
        } else {
            symbolTable.insert(arrayDec.name, new NodeType(arrayDec.name, arrayDec, level));
        }
        if (arrayDec.typ != null) {
            arrayDec.typ.accept(this, level + 1, isAddr);
        }
    }

    @Override
    public void visit(NameTy nameTy, int level, boolean isAddr) {

        if (isInGlobalScope) {
            indent(level);
            appendLine(":" + nameTy.toString());
        }
    }

    @Override
    public void visit(FunDec funDec, int level, boolean isAddr) {
        isInGlobalScope = false;
        currentFunctionContext = funDec;
            if (isInGlobalScope) {
            indent(level);
            appendLine("Visiting FunDec: " + funDec.funcName);
        }
        NodeType existingFunc = symbolTable.lookup(funDec.funcName);
        if (existingFunc != null && existingFunc.level == level) {
            System.err.println("Semantic Error: Function " + funDec.funcName + " is redeclared");
        } else {
            symbolTable.insert(funDec.funcName, new NodeType(funDec.funcName, funDec, level));
        }
        if (funDec.result != null) {
            funDec.result.accept(this, level + 1, isAddr);
        }
        symbolTable.enterScope(level++, funDec.funcName);
        if (funDec.params != null) {
            funDec.params.accept(this, level + 1, isAddr);
        }
        if (funDec.body != null) {
            funDec.body.accept(this, level + 1, isAddr);
        }
        currentFunctionContext = null;
        isInGlobalScope = true;
        symbolTable.exitScope(level--, true);
    }

    @Override
    public void visit(NilExp nilExp, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting NilExp");
    }

    @Override
    public void visit(VarDecList varDecList, int level, boolean isAddr) {
        while (varDecList != null) {
            if (varDecList.head != null) {
                indent(level);
                appendLine("Visiting VarDecList");
                varDecList.head.accept(this, level, isAddr);
            }
            varDecList = varDecList.tail;
        }
    }

    @Override
    public void visit(ExpList stmtList, int level, boolean isAddr) {
        while (stmtList != null) {
            if (!(stmtList.head instanceof NilExp)) {
                stmtList.head.accept(this, level + 1, isAddr);
            }
            stmtList = stmtList.tail;
        }
    }

    @Override
    public void visit(CompoundExp compoundExp, int level, boolean isAddr) {
        symbolTable.enterScope(level++, null);
        if (compoundExp.localDecs != null) {
            VarDecList localDecs = compoundExp.localDecs;
            while (localDecs != null) {
                if (localDecs.head != null) {
                    localDecs.head.accept(this, level + 1, isAddr);
                }
                localDecs = localDecs.tail;
            }
        }
        if (compoundExp.stmtList != null) {
            ExpList stmtList = compoundExp.stmtList;
            while (stmtList != null) {
                if (stmtList.head != null) {
                    stmtList.head.accept(this, level + 1, isAddr);
                }
                stmtList = stmtList.tail;
            }
        }
        symbolTable.exitScope(level--, false);
    }

    @Override
    public void visit(IfExp ifExp, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting IfExp");
        if (!isNilOrNull(ifExp.test)) {
            ifExp.test.accept(this, level + 1, isAddr);
        }
        if (ifExp.thenClause != null) {
            symbolTable.enterScope(level + 1, null);
            ifExp.thenClause.accept(this, level + 1, isAddr);
            symbolTable.exitScope(level + 1, false);
        }
        if (ifExp.elseClause != null) {
            symbolTable.enterScope(level + 1, null);
            ifExp.elseClause.accept(this, level + 1, isAddr);
            symbolTable.exitScope(level + 1, false);
        }
    }

    @Override
    public void visit(WhileExp whileExp, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting WhileExp");
        if (!isNilOrNull(whileExp.test)) {
            whileExp.test.accept(this, level + 1, isAddr);
        }
        if (!isNilOrNull(whileExp.test)) {
            symbolTable.enterScope(level + 1, null);
            whileExp.body.accept(this, level + 1, isAddr);
            symbolTable.exitScope(level + 1, false);
        }
    }

    @Override
    public void visit(ReturnExp returnExp, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting ReturnExp");
        if (returnExp.exp != null) {
            returnExp.exp.accept(this, level + 1, isAddr);
            if (currentFunctionContext == null) {
                String errorMsg = "Return statement not inside a function.";
                addSemanticError(returnExp, errorMsg);                
            } else if (returnExp.exp != null) {
                if (currentFunctionContext.result.typ == NameTy.VOID && !isNilOrNull(returnExp.exp)) {
                    String errorMsg = "'return' with a value, in function returning void";
                    addSemanticError(returnExp, errorMsg);
                } else {
                    if (!areTypesCompatible(currentFunctionContext.result, returnExp.exp)) {
                        String errorMsg = "Type mismatch in return statement.";
                        if (returnExp.getType() != null) {
                        } else {
                        }
                        if (returnExp.exp != null && returnExp.exp.getType() != null) {
                        } else {
                        }
                        addSemanticError(returnExp, errorMsg);
                    }
                }
            } else {
                if (currentFunctionContext != null && currentFunctionContext.result.typ != NameTy.VOID) {
                    String errorMsg = "Error in line " + returnExp.row + ", column " + returnExp.col + " : Semantic Error: Missing return value in non-void function.";
                    addSemanticError(returnExp, errorMsg);
                }
            }
        }
    }

    @Override
    public void visit(VarExp varExp, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting VarExp: " + varExp.variable.getName());
        NodeType nodeType = symbolTable.lookup(varExp.variable.getName());
        if (nodeType == null) {
            String errorMsg = "Variable " + varExp.variable.getName() + " not declared.";
            addSemanticError(varExp.variable, errorMsg);
        } else {
            varExp.setType(nodeType.dec);
        }
    }

    @Override
    public void visit(AssignExp assignExp, int level, boolean isAddr) {
        assignExp.lhs.accept(this, level, isAddr);
        assignExp.rhs.accept(this, level, isAddr);
        NameTy lhsType = extractNameTy(assignExp.lhs.getType());
        NameTy rhsType = extractNameTy(assignExp.rhs.getType());

        if (lhsType != null && rhsType != null && lhsType.typ != rhsType.typ) {
            String errorMsg = "Type mismatch in assignment.";
            addSemanticError(assignExp, errorMsg);
        }
    }

    @Override
    public void visit(SimpleVar simpleVar, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting SimpleVar: " + simpleVar.getName());
        NodeType nodeType = symbolTable.lookup(simpleVar.getName());
        if (nodeType == null) {
            System.err.println("Semantic Error: Undeclared variable " + simpleVar.getName() + " at " + simpleVar.row + ", " + simpleVar.col);
        } else {
        }
    }

    @Override
    public void visit(IndexVar indexVar, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting IndexVar: " + indexVar.getName());
        NodeType nodeType = symbolTable.lookup(indexVar.getName());
        if (nodeType == null) {
            String errorMsg = "Undeclared variable '" + indexVar.getName() + "' used as an array at line " + indexVar.row + ", column " + indexVar.col;
            addSemanticError(indexVar, errorMsg);
        } else if (!(nodeType.dec instanceof ArrayDec)) {
            String errorMsg = "Variable '" + indexVar.getName() + "' is not an array type at line " + indexVar.row + ", column " + indexVar.col;
            addSemanticError(indexVar, errorMsg);
        } else {
            if (indexVar.index != null) {
                indexVar.index.accept(this, level + 1, isAddr);
                if (!isInteger(indexVar.index)) {
                    String errorMsg = "Array index must be an integer at line " + indexVar.row + ", column " + indexVar.col;
                    addSemanticError(indexVar, errorMsg);
                }
            }
        }
    }

    @Override
    public void visit(OpExp opExp, int level, boolean isAddr) {
        indent(level);
        appendLine("Visiting OpExp");
        opExp.left.accept(this, level + 1, isAddr);
        if (!isNilOrNull(opExp.right)) { // Unary operations won't have a right operand
            opExp.right.accept(this, level + 1, isAddr);
        } 
        switch (opExp.op) {
            case OpExp.PLUS:
            case OpExp.MINUS:
            case OpExp.MUL:
            case OpExp.DIV:
                if (areTypesCompatible(intTy, opExp.left) && areTypesCompatible(intTy, opExp.right)) {
                    opExp.type = intTy;
                } else {
                    addSemanticError(opExp, "Arithmetic operations require integer operands");
                }
                break;
            case OpExp.EQ:
            case OpExp.NE:
                if (areTypesCompatible(intTy, opExp.left) && areTypesCompatible(intTy, opExp.right)) {
                    opExp.type = intTy;
                } else if (areTypesCompatible(boolTy, opExp.left) && areTypesCompatible(boolTy, opExp.right)) {
                    opExp.type = boolTy;
                } else {
                    addSemanticError(opExp, "Equality operations require operands of the same type (both int or both bool)");
                }
                break;
            case OpExp.LT:
            case OpExp.LE:
            case OpExp.GT:
            case OpExp.GE:
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
                if (areTypesCompatible(intTy, opExp.left)) {
                    opExp.type = intTy;
                } else {
                    addSemanticError(opExp, "Unary minus operation requires an integer operand");
                }                
                break;
        }
    }

    @Override
    public void visit(IntExp intExp, int level, boolean isAddr) {
        int value = intExp.value;
        if (value < 0 || value > 100) {
            System.err.println("Semantic Error: Integer value out of range (0-100)");
        }
    }

   @Override
    public void visit(BoolExp boolExp, int level, boolean isAddr) {
        indent(level);
        appendLine("BoolExp: " + boolExp.value);

        if (!isValidBoolExp(boolExp)) {
            addSemanticError(boolExp, "BoolExp is not well-formed");                      
        }
    }

    private boolean isValidBoolExp(BoolExp boolExp) {
        return boolExp != null && (boolExp.value == true || boolExp.value == false);
    }

    @Override
    public void visit(CallExp callExp, int level, boolean isAddr) {
        indent(level);
        NodeType funcNode = symbolTable.lookup(callExp.func);
        if (funcNode == null || !(funcNode.dec instanceof FunDec)) {
            addSemanticError(callExp, "Function '" + callExp.func + "' is not declared.");
            return; // Exit if the function is not declared to avoid further errors
        }
        FunDec funcDec = (FunDec) funcNode.dec;
        ExpList arg = callExp.args;
        while (arg != null) {
            arg.head.accept(this, level + 1, isAddr); // Process each argument
            arg = arg.tail;
        }
    }
    private boolean isNilOrNull(Exp exp) {
        return exp == null || exp instanceof NilExp;
    }
    
    private boolean isInteger(Exp exp) {
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
        return false;
    }

    private boolean areTypesCompatible(NameTy expected, Exp actual) {

        if ((expected.typ == NameTy.INT) && isInteger(actual)) {
          return true;
        } else if ((expected.typ == NameTy.BOOL) && isBoolean(actual)) {
          return true;
        }

        if (actual.getType() instanceof VarDec) {
            VarDec varDec = (VarDec) actual.getType();
            return expected.typ == varDec.typ.typ; // Assuming VarDec contains a NameTy object as 'typ'
        } else if (actual.getType() instanceof FunDec) {
            FunDec funDec = (FunDec) actual.getType();
            return expected.typ == funDec.result.typ;
        }
        if (actual instanceof BoolExp || isBoolean(actual)) {
            if (expected.typ == NameTy.BOOL) return true;
        } else if (actual instanceof IntExp || isInteger(actual)) {
            if (expected.typ == NameTy.INT) return true;
        } else if (isNilOrNull(actual) && expected.typ == NameTy.VOID) {
            return true;
        } else if (actual instanceof CallExp) {
            CallExp callExp = (CallExp) actual;
            NodeType funcNode = symbolTable.lookup(callExp.func);
            if (funcNode != null && funcNode.dec instanceof FunDec) {
                FunDec funDec = (FunDec) funcNode.dec;
                if (expected.typ == funDec.result.typ) {
                    return true;
                }
            } else {
            }
        } else if (actual instanceof VarExp) {
            VarExp varExp = (VarExp) actual;
            NodeType nodeType = symbolTable.lookup(varExp.variable.toString());
            if (nodeType != null && nodeType.dec instanceof VarDec) {
                VarDec varDec = (VarDec) nodeType.dec;
                if (expected.typ == varDec.typ.typ) {
                    return true;
                }
            } else {
            }
        } else if (actual instanceof OpExp) {
            OpExp opExp = (OpExp) actual;
            if (opExp.type != null) {
                return expected.typ == opExp.type.typ;
            } else {
                return false;
            }
        } else {
        }
        return false;
    }

    private NameTy extractNameTy(Dec dec) {
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

}
