import absyn.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class ShowTreeVisitor implements AbsynVisitor {

    final static int SPACES = 4;
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

    // private void indent(int level, boolean isAddr) {
    //     for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    // }

    private String translateType(int typ) {
        switch (typ) {
            case 0: return "bool";
            case 1: return "int";
            case 2: return "void";
            default: return "unknown";
        }
    }

    @Override
    public void visit(ErrorDec errorDec, int level, boolean isAddr) {
        indent(level);
        appendLine(errorDec.toString());
    }

    @Override
    public void visit(Program program, int level, boolean isAddr) {
        indent(level);
        appendLine("Program");
        // Assuming Program contains a list of declarations
        program.declarations.accept(this, level + 1, isAddr);
    }

    // print in reverse order
    @Override
    public void visit(DecList decList, int level, boolean isAddr) {
        // Initialize a stack to hold the declarations
        Deque<Dec> stack = new ArrayDeque<>();
        // Traverse the DecList and push each declaration onto the stack
        while (decList != null) {
            if (decList.head != null) {
                stack.push(decList.head);
            }
            decList = decList.tail;
        }
        // Pop each declaration off the stack and visit it, effectively in reverse order
        while (!stack.isEmpty()) {
            Dec dec = stack.pop();
            dec.accept(this, level, isAddr);
        }
    }

    @Override
    public void visit(SimpleDec simpleDec, int level, boolean isAddr) {
        indent(level);
        appendLine("SimpleDec: " + simpleDec.name);
        simpleDec.typ.accept(this, level + 1, isAddr);
    }

    @Override
    public void visit(ArrayDec arrayDec, int level, boolean isAddr) {
        indent(level);
        appendLine("ArrayDec: " + arrayDec.name + "[" + arrayDec.size + "]");
        arrayDec.typ.accept(this, level + 1, isAddr);
    }

    @Override
    public void visit(NameTy nameTy, int level, boolean isAddr) {
        indent(level);
        String typeName = switch (nameTy.typ) {
            case NameTy.BOOL -> "BOOL";
            case NameTy.INT -> "INT";
            case NameTy.VOID -> "VOID";
            default -> "UNKNOWN";
        };
        appendLine("Type: " + typeName);
    }

    @Override
    public void visit(FunDec funDec, int level, boolean isAddr) {
        indent(level);
        // Assuming funDec.result.typ is an integer corresponding to a type.
        String returnType = translateType(funDec.result.typ);
        appendLine("FunctionDec: " + funDec.funcName + ", Return type: " + returnType);
        if (funDec.params != null) {
            funDec.params.accept(this, level + 1, isAddr);
        }
        if (funDec.body != null) {
            funDec.body.accept(this, level + 1, isAddr);
        }
    }

    @Override
    public void visit(NilExp nilExp, int level, boolean isAddr) {
        // indent(level);
        // appendLine("NilExp");
    }

    @Override
    public void visit(VarDecList varDecList, int level, boolean isAddr) {
        while (varDecList != null && varDecList.head != null) { // Ensure head is not null before proceeding
            varDecList.head.accept(this, level + 1, isAddr); // Only visit non-null heads
            varDecList = varDecList.tail; // Move to next in list
        }
    }

    @Override //stmtList
    public void visit(ExpList stmtList, int level, boolean isAddr) {
        while (stmtList != null && stmtList.head != null) {
            stmtList.head.accept(this, level + 1, isAddr);
            stmtList = stmtList.tail;
        }
    }

    @Override //localDecs
    public void visit(CompoundExp compoundExp, int level, boolean isAddr) {
        indent(level);
        appendLine("CompoundExp:");
        if (compoundExp.localDecs != null) {
            compoundExp.localDecs.accept(this, level + 1, isAddr);
        }
        if (compoundExp.stmtList != null) {
            compoundExp.stmtList.accept(this, level + 1, isAddr);
        }
    }

        @Override
    public void visit(IfExp ifExp, int level, boolean isAddr) {
        indent(level);
        appendLine("IfExp:");
        ifExp.test.accept(this, level + 1, isAddr);
        ifExp.thenClause.accept(this, level + 1, isAddr);
        if (ifExp.elseClause != null) {
            ifExp.elseClause.accept(this, level + 1, isAddr);
        }
    }

    @Override
    public void visit(WhileExp whileExp, int level, boolean isAddr) {
        indent(level);
        appendLine("WhileExp:");
        whileExp.test.accept(this, level + 1, isAddr);
        whileExp.body.accept(this, level + 1, isAddr);
    }

    @Override
    public void visit(ReturnExp returnExp, int level, boolean isAddr) {
        indent(level); // Apply current level of indentation
        builder.append("ReturnExp: ");
        if (returnExp.exp != null) {
            // Temporarily hold the current length to remove any new lines added by the child accept call
            int originalLength = builder.length();
            returnExp.exp.accept(this, level, isAddr); // Keep the same level for inline display
            // Remove any trailing new line characters added by the child expression
            String inlineExp = builder.substring(originalLength).replaceAll("\n$", "");
            builder.setLength(originalLength); // Reset builder to original length
            appendLine(inlineExp.trim()); // Append the child expression's output inline and add a new line at the end
        } else {
            appendLine(" // returns void or no value");
        }
    }


    @Override
    public void visit(VarExp varExp, int level, boolean isAddr) {
        indent(level);
        appendLine("VarExp:");
        // Assuming VarExp contains a variable that also needs to be visited
        varExp.variable.accept(this, level + 1, isAddr);
    }

    @Override
    public void visit(AssignExp assignExp, int level, boolean isAddr) {
        indent(level);
        appendLine("AssignExp:");
        // Visit the left-hand side variable
        assignExp.lhs.accept(this, level + 1, isAddr);
        // Visit the right-hand side expression
        assignExp.rhs.accept(this, level + 1, isAddr);
    }

    @Override
    public void visit(SimpleVar simpleVar, int level, boolean isAddr) {
        indent(level);
        appendLine("SimpleVar: " + simpleVar.name);
    }

    @Override
    public void visit(IndexVar indexVar, int level, boolean isAddr) {
        indent(level);
        appendLine("IndexVar: " + indexVar.name + " [");
        indexVar.index.accept(this, level + 1, isAddr);
        appendLine("]");
    }

    @Override
    public void visit(OpExp opExp, int level, boolean isAddr) {
        indent(level);
        String opSymbol = switch (opExp.op) {
            case OpExp.PLUS -> { System.out.print(" + "); yield "+"; }
            case OpExp.MINUS -> { System.out.print(" - "); yield "-"; }
            case OpExp.UMINUS -> { System.out.print(" unary - "); yield "-"; }
            case OpExp.MUL -> { System.out.print(" * "); yield "*"; }
            case OpExp.DIV -> { System.out.print(" / "); yield "/"; }
            case OpExp.EQ -> { System.out.print(" == "); yield "=="; }
            case OpExp.NE -> { System.out.print(" != "); yield "!="; }
            case OpExp.LT -> { System.out.print(" < "); yield "<"; }
            case OpExp.LE -> { System.out.print(" <= "); yield "<="; }
            case OpExp.GT -> { System.out.print(" > "); yield ">"; }
            case OpExp.GE -> { System.out.print(" >= "); yield ">="; }
            case OpExp.NOT -> { System.out.print(" ! "); yield "!"; }
            case OpExp.AND -> { System.out.print(" && "); yield "&&"; }
            case OpExp.OR -> { System.out.print(" || "); yield "||"; }
            // default -> { System.out.print(" unknown "); yield "unknown"; }
            default -> {
                appendLine("Unrecognized operator at line " + opExp.row + " and column " + opExp.col);
                yield "unknown";
            }
        };
        appendLine("OpExp: " + opSymbol);
        if (!(opExp.left instanceof NilExp) && opExp.left != null) {
            opExp.left.accept(this, level + 1, isAddr);
        }
        if (opExp.right != null) { // Right operand might be null for unary operators
            opExp.right.accept(this, level + 1, isAddr);
        }
    }

    @Override
    public void visit(IntExp intExp, int level, boolean isAddr) {
        indent(level);
        appendLine("IntExp: " + intExp.value);
    }

    @Override
    public void visit(BoolExp boolExp, int level, boolean isAddr) {
        indent(level);
        appendLine("BoolExp: " + boolExp.value);
    }

    @Override
    public void visit(CallExp callExp, int level, boolean isAddr) {
        indent(level);
        appendLine("CallExp: Function call to " + callExp.func + " with arguments:");
        if (callExp.args != null) {
            // Initialize a temporary variable to iterate through the list
            ExpList temp = callExp.args;
            if (temp.head == null) {
                // Handling calls with no arguments
                indent(level + 1); // Indent for alignment
                appendLine("[no arguments]");
            } else {
                // Recursively visit each argument
                while (temp != null && temp.head != null) {
                    temp.head.accept(this, level + 1, isAddr);
                    temp = temp.tail; // Move to the next element in the list
                }
            }
        } else {
            // Handling calls with no arguments
            indent(level + 1); // Indent for alignment
            appendLine("[no arguments]");
        }
    }
}
