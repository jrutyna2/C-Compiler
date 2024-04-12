package symbols;
import absyn.*;
import utils.*;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Deque;
public class CodeGenerator implements AbsynVisitor {
    private static final int ADMIN_FIELDS = 2; // Return address and old frame pointer
    private static final int MAX_LOCAL_VARIABLES = 10; // Example maximum to prevent overflow
    private static final int FRAME_SIZE = ADMIN_FIELDS + MAX_LOCAL_VARIABLES;
    private static final int RETURN_VALUE_OFFSET = -1;  // Example: 1 slot above the frame pointer
    private static final int RETURN_ADDRESS_OFFSET = -2; // Example offset for the return address

    private int currentLocation = 0; 
    private int emitLoc = 0; // to rack the next instruction location
    private int highEmitLoc = 0; // highest emit location for temporary variables
    
    private int gp = 0; // global Pointer register in TM
    private int mp = 1; // memory pointer points to top of memory (for temp storage)
    private int fp = 5; // frame Pointer register in TM
    private int ac = 0; // Accumulator register in TM for expression evaluation
    private int ac1 = 1; // second accumulator register in TM
    private int sp = 1; // Assuming you want to introduce a stack pointer    
    private static final int pc = 7; // Assuming register 7 is the program counter

    private int globalVarOffset = 0; // Track the offset for global variables
    private int localVarOffset = 0;  // Track the offset for local variables
    private int mainEntry = -1; // Entry point for the main function
    private int inputEntry = -1;
    private int outputEntry = -1;
    // A stack to remember positions for emitBackup
    private Stack<Integer> backupStack = new Stack<>();
    private Map<String, Integer> functionDirectory = new HashMap<>();
    private SymbolTable symbolTable;
    private StringBuilder codeBuilder = new StringBuilder();

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public String getGeneratedCode() {
        return codeBuilder.toString();
    }

    private void emitStandardPrelude() {
        emitComment("Standard prelude:");
        emitRM("LD", 6, 0, 0, "load gp with maxaddress");
        emitRM("LDA", 5, 0, 6, "copy to gp to fp");
        emitRM("ST", 0, 0, 0, "clear location 0");
        // Reserve space for jump instruction to skip around I/O code
        int jumpInstLoc = emitLoc; // Record the location for backpatching
        emitSkip(1); // Effectively reserves space by incrementing emitLoc
        // Emit predefined I/O routines immediately after the prelude
        emitIORoutines();
        // Now backpatch the jump instruction to skip over I/O routines
        int mainStartLoc = emitLoc; // Use emitLoc directly as the current location
        emitBackup(jumpInstLoc); // Move back to the jump instruction's location
        // Assuming 'LDA pc, offset(pc)' is how you've structured jump instructions...
        emitRM("LDA", pc, mainStartLoc - (jumpInstLoc + 1), pc, "jump around i/o code");
        emitRestore(); // Return to the current location to continue code generation
        emitComment("End of standard prelude.");        
    }

    private void emitIORoutines() {
        emitComment("Jump around i/o routines here");
        emitComment("code for input routine");
        emitRM("ST", ac, -1, fp, "store return");
        emitRO("IN", ac, 0, 0, "input");
        emitRM("LD", pc, -1, fp, "return to caller");

        emitComment("code for output routine");
        emitRM("ST", ac, -1, fp, "store return");
        emitRM("LD", ac, -2, fp, "load output value");
        emitRO("OUT", ac, 0, 0, "output");
        emitRM("LD", pc, -1, fp, "return to caller");
    }

    @Override
    public void visit(Program program, int level, boolean isAddr) {
        // Emit the standard prelude first
        emitStandardPrelude();
        // Process the program declarations and statements
        if (program.declarations != null) {
            program.declarations.accept(this, level, false);
        }
        // Emit any finale code if needed (optional, depending on your code structure)
        // This might include finalization logic or cleanup, if applicable
        emitFinale();
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
        // Pop each declaration off the stack and visit it
        // This ensures that declarations are processed in the order they appear in the source code
        while (!stack.isEmpty()) {
            Dec dec = stack.pop();
            dec.accept(this, level + 1, isAddr);
        }
    }
private void emitHalt() {
    System.out.println("HALT emitted at location: " + emitLoc);  // Debugging line
    emitRO("HALT", 0, 0, 0, "End of program execution");
}

@Override
public void visit(FunDec funDec, int level, boolean isAddr) {
    if (!funDec.funcName.equals("main")) {
        int funcStart = emitSkip(0); // Start of function
        int skipLoc = emitSkip(1); // Reserve space to skip over function body
        functionDirectory.put(funDec.funcName, emitSkip(0)); // Record start of actual function body for calls
        emitComment("Processing function: " + funDec.funcName);

        // Function body processing...
        if (funDec.body != null) {
            funDec.body.accept(this, level + 1, false);
        }

        // Backpatch to skip the function body
        int afterFuncLoc = emitLoc; // Location after function body
        emitBackup(skipLoc);
        emitRM("LDA", pc, afterFuncLoc - (skipLoc + 1), pc, "Jump around function body");
        emitRestore();        
    } else {
        // Special handling for main, where we typically do not jump around
        mainEntry = emitSkip(0);
        emitComment("Start of main function");

        // Function body processing...
        if (funDec.body != null) {
            funDec.body.accept(this, level + 1, false);
        }
    }

    // localVarOffset = 0;
    // if (funDec.params != null) {
    //     funDec.params.accept(this, level + 1, false);
    // }
    // if (funDec.body != null) {
    //     funDec.body.accept(this, level + 1, false);
    // }

    // emitComment("End of function: " + funDec.funcName);
    // if (!funDec.funcName.equals("main")) {
    //     emitRM("LD", pc, -1, fp, "Return to caller");
    // } else {
    //     emitHalt();  // Use the helper method for emitting HALT
    // }
    // Emit function return handling
    emitComment("End of function: " + funDec.funcName);
    if (!funDec.funcName.equals("main")) {
        emitRM("LD", pc, -1, fp, "Return to caller");
    }
}

    // @Override
    // public void visit(FunDec funDec, int level, boolean isAddr) {
    //     if (funDec.funcName.equals("main")) {
    //         mainEntry = emitSkip(0); // Mark the start of the main function
    //         emitComment("Start of main function");
    //     } else {
    //         // Handle other functions
    //         functionDirectory.put(funDec.funcName, emitSkip(0));
    //         emitComment("Function declaration: " + funDec.funcName);
    //         // System.out.println("Function declaration: " + funDec.funcName);
    //     }

    //     // Initialize localVarOffset for new function scope
    //     localVarOffset = 0;

    //     // Handle function arguments and body
    //     if (funDec.params != null) {
    //         funDec.params.accept(this, level + 1, false);
    //     }
    //     if (funDec.body != null) {
    //         funDec.body.accept(this, level + 1, false);
    //     }
    //     // Function return or end
    //     emitComment("End of function: " + funDec.funcName);
    //     if (!funDec.funcName.equals("main")) {
    //         // Implement return mechanism for user-defined functions
    //         // For example, jump back to the caller, handle stack frame clean-up
    //         emitRM("LD", pc, -1, fp, "Return to caller");
    //     } else {
    //         emitRO("HALT", 0, 0, 0, "End of program execution");
    //     }
    // }

    // @Override
    // public void visit(FunDec funDec, int level, boolean isAddr) {
    //     //int funcEntry = emitSkip(0); // Save the current location as the function's entry point
    //     // functionDirectory.put(funcDec.funcName, funcEntry);
    //         emitComment("Function: " + funDec.funcName + " entry point");
    //         if ("main".equals(funDec.funcName)) {
    //             mainEntry = emitLoc; // Record the start of main
    //         }
    //     // emitComment("Function: " + funcDec.funcName + " entry point");
    //     // emitComment("Function: " + funDec.name);
    //     emitComment("Prologue: Start function");
    //     // emitRM("ST", 0, -1, fp, "Store return address");
    //     // emitRM("LDA", fp, -1, fp, "Adjust fp");
    //     // emitRM("LDA", ac, 1, pc, "Load ac with return address");
    //     // emitRM_Abs("LDA", pc, funDec.entry, "Jump to function entry");
    //     // emitComment("Function: " + funDec.funcName + " ends here");
    // }

    @Override
    public void visit(VarDecList varDecList, int level, boolean isAddr) {
        while (varDecList != null && varDecList.head != null) { // Ensures null safety
            varDecList.head.accept(this, level + 1, isAddr);
            varDecList = varDecList.tail;
        }
    }

    @Override
    public void visit(SimpleDec simpleDec, int level, boolean isAddr) {
        // Comment on the purpose of this variable
        emitComment((level == 0 ? "Global" : "Local") + " variable: " + simpleDec.name);

        if (level == 0) { // Global variable
            globalVarOffset--; // Deduct from global offset for new variable
            emitRM("LDC", ac, 0, 0, "Init " + simpleDec.name + " to 0"); // Initialize global variable
            emitRM("ST", ac, globalVarOffset, gp, "Store global variable " + simpleDec.name);
        } else { // Local variable within a function
            localVarOffset--; // Allocate space for local variable on the stack
            // Initialization of local variables (optional based on language specs)
            emitRM("LDC", ac, 0, 0, "Optionally init " + simpleDec.name + " to 0");
            emitRM("ST", ac, localVarOffset, fp, "Store local variable " + simpleDec.name);
        }
    }

    @Override
    public void visit(ArrayDec arrayDec, int level, boolean isAddr) {
        if (level == 0) { // Assuming global array
            emitComment("Global array: " + arrayDec.name);
            // Allocate space; assuming each element is 1 memory cell
            emitRM("LDC", ac, arrayDec.size, 0, "load array size");
            emitRM("ST", ac, globalVarOffset--, gp, "store array size at global offset");
            for (int i = 0; i < arrayDec.size; i++) {
                emitRM("LDC", ac, 0, 0, "initialize to 0");
                emitRM("ST", ac, globalVarOffset--, gp, "init array element");
            }
        } else {
            // For local arrays, you'd adjust the mp (memory pointer) accordingly
            // This is more complex and involves runtime memory management
            emitComment("Local arrays not implemented in this example");
        }
    }
    // @Override
    // public void visit(AssignExp assignExp, int level, boolean isAddr) {
    //     assignExp.rhs.accept(this, level, false); // Evaluate the RHS expression first, result in `ac`
    //     // Now handle the LHS as a location where we need to store the result
    //     if (assignExp.lhs instanceof VarExp) {
    //         VarExp lhsVarExp = (VarExp) assignExp.lhs;
    //         if (lhsVarExp.variable instanceof SimpleVar) {
    //             SimpleVar simpleVar = (SimpleVar) lhsVarExp.variable;
    //         // Assuming `getVariableOffset` correctly calculates the offset for the variable
    //         int offset = getVariableOffset(simpleVar.name, level); // Needs implementation
    //         emitRM("ST", ac, offset, (level == 0 ? gp : fp), "Assign: store value");
    //     }
    //     }
    // }

@Override
public void visit(AssignExp assignExp, int level, boolean isAddr) {
    // Evaluate RHS
    assignExp.rhs.accept(this, level, false);
    // Determine the offset and base register based on scope level
    int offset;
    // Determine the base register for global vs. local scope
    int baseReg;
    if (level == 0) {  // Global variable
        baseReg = gp;
        offset = globalVarOffset++;  // Manage global offsets directly here
    } else {  // Local variable
        baseReg = fp;
        offset = --localVarOffset;  // Use the local offset, decrement to simulate stack growth
    }
    // Handle the LHS which should compute its own address for storing the RHS value
    assignExp.lhs.accept(this, level, true); // Accept with isAddr = true, assuming it sets up for an address
    // Store the value from the accumulator into the computed address
    emitRM("ST", ac, offset, baseReg, "Assign: store value");
}

// @Override
// public void visit(AssignExp assignExp, int level, boolean isAddr) {
//     // Evaluate the RHS expression first, result in `ac`
//     assignExp.rhs.accept(this, level, false);
    
//     // Now handle the LHS as a location where we need to store the result
//     if (assignExp.lhs instanceof SimpleVar) {
//         SimpleVar simpleVar = (SimpleVar) assignExp.lhs;
//         boolean isGlobal = symbolTable.isGlobal(simpleVar.name);
//         int offset = symbolTable.getOffset(simpleVar.name);
        
//         if (isGlobal) {
//             // For global variables, use the gp (global pointer) as the base
//             emitRM("ST", ac, offset, gp, "Assign: store value in global variable");
//         } else {
//             // For local variables, use the fp (frame pointer) as the base and adjust by current stack frame level
//             emitRM("ST", ac, calculateLocalOffset(offset, level), fp, "Assign: store value in local variable");
//         }
//     }
//     // Additional handling for more complex LHS expressions if needed
// }

    @Override
    public void visit(ExpList expList, int level, boolean isAddr) {
        while (expList != null) {
            expList.head.accept(this, level, false); // Expressions in a list are evaluated for their side effects or values
            expList = expList.tail;
        }
    }

    @Override
    public void visit(CompoundExp compoundExp, int level, boolean isAddr) {
        emitComment("Begin compound statement");

        // Scope management might be needed here if handling stack directly
        emitComment("Push new frame or scope marker");

        if (compoundExp.localDecs != null) {
            emitComment("Variable declarations in compound statement");
            compoundExp.localDecs.accept(this, level + 1, false);
        }

        if (compoundExp.stmtList != null) {
            emitComment("Statements/Expressions in compound statement");
            compoundExp.stmtList.accept(this, level + 1, isAddr);
        }

        emitComment("Pop frame or scope marker");
        emitComment("End compound statement");
    }

    // @Override
    // public void visit(IfExp ifExp, int level, boolean isAddr) {
    //     ifExp.test.accept(this, level, false); // Evaluate condition; result in AC
    //     int savedLoc = emitLoc;
    //     emitRM("JEQ", ac, 0, pc, "if: jmp to else part");
    //     //ifExp.thenClause.accept(this, level, false);

    //     int savedLoc2 = emitLoc;
    //     emitRM("LDA", pc, 0, pc, "jmp to end");
    //     int currentLoc = emitLoc;

    //     // Backpatching not directly supported; this is illustrative
    //     emitLoc = savedLoc;
    //     emitRM("JEQ", ac, currentLoc - emitLoc - 1, pc, "if: jmp to else part (backpatch)");

    //     // if (ifExp.elseClause != null) {
    //     //     ifExp.elseClause.accept(this, level, false);
    //     // }
    // }

    @Override
    public void visit(IfExp ifExp, int level, boolean isAddr) {
        ifExp.test.accept(this, level, false); // Evaluate condition; result in AC
        int savedLoc = emitSkip(1); // Skip over the jump instruction initially

        ifExp.thenClause.accept(this, level, false); // Process the "then" part

        int savedLoc2 = emitSkip(1); // Skip unconditional jump (for jumping to end after then-part)
        int elsePartLoc = emitLoc;  // Current location is where else part starts

        emitBackup(savedLoc);
        emitRM("JEQ", ac, elsePartLoc - savedLoc - 1, pc, "Jump to else part"); // Backpatch to jump to else
        emitRestore();

        if (ifExp.elseClause != null) {
            ifExp.elseClause.accept(this, level, false);  // Process the "else" part
        }

        int endLoc = emitLoc; // End location after else part
        emitBackup(savedLoc2);
        emitRM("LDA", pc, endLoc - savedLoc2 - 1, pc, "Jump to end of if"); // Backpatch to jump to end
        emitRestore();
    }
    
    @Override
    public void visit(WhileExp whileExp, int level, boolean isAddr) {
        emitComment("start of while loop");
        int startLoopLoc = emitLoc; // Location for start of the loop

        whileExp.test.accept(this, level, false); // Evaluate the loop condition
        int jumpToEndLoc = emitSkip(1); // Space for conditional jump out of loop

        whileExp.body.accept(this, level, false); // Generate loop body code
        emitRM_Abs("LDA", pc, startLoopLoc, "jump back to the start of the loop"); // Jump back to start

        int loopEndLoc = emitLoc; // Location after loop body
        emitBackup(jumpToEndLoc);
        emitRM("JEQ", ac, loopEndLoc - jumpToEndLoc - 1, pc, "Jump to end of loop if condition is false"); // Backpatch
        emitRestore();
        emitComment("end of while loop");
    }

    // @Override
    // public void visit(WhileExp whileExp, int level, boolean isAddr) {
    //     emitComment("start of while loop");
    //     int startLoopLoc = emitLoc; // Directly use integer for loop start location
    //     whileExp.test.accept(this, level + 1, false); // Generate test expression code
    //     int jumpToEndLoc = emitSkip(1); // Reserve space for conditional jump out of loop
    //     whileExp.body.accept(this, level + 1, false); // Generate loop body code
    //     emitRM_Abs("LDA", pc, startLoopLoc, "jump back to the start of the loop"); // Jump back to start
    //     int loopEndLoc = emitLoc; // Location after loop body
    //     emitBackup(jumpToEndLoc); 
    //     emitRM_Abs("JEQ", ac, loopEndLoc, "Jump to end of loop if condition is false"); // Correct backpatching
    //     emitRestore();
    //     emitComment("end of while loop");
    // }

@Override
public void visit(ReturnExp returnExp, int level, boolean isAddr) {
    emitComment("start of return");

    // If there's an expression to return, evaluate it
    if (returnExp.exp != null) {
        returnExp.exp.accept(this, level + 1, false);
        // DONT Assume a fixed location for the return value, if applicable:
        emitRM("ST", ac, RETURN_VALUE_OFFSET, fp, "Store return value");
    }

    // Load the return address from the stack and jump to it
    emitRM("LD", pc, RETURN_ADDRESS_OFFSET, fp, "Load return address and jump to caller");

    emitComment("end of return");
}

    @Override
    public void visit(OpExp opExp, int level, boolean isAddr) {
        emitComment("Arithmetic/Logic Operation: " + opExp.toString());
        // Direct handling of operands from memory if possible, removing unnecessary push to stack
        opExp.left.accept(this, level, false);  // Assume result in ac
        int leftReg = ac;  // Use ac for the left operand
        opExp.right.accept(this, level, false); // Assume result in ac
        int rightReg = ac; // Use a new register or re-use ac if possible

        switch (opExp.op) {
            case OpExp.PLUS:
                emitRO("ADD", ac, leftReg, rightReg, "op: +");
                break;
            case OpExp.MINUS:
                emitRO("SUB", ac, leftReg, rightReg, "op: -");
                break;
            case OpExp.MUL:
                emitRO("MUL", ac, leftReg, rightReg, "op: *");
                break;
            case OpExp.DIV:
                emitRO("DIV", ac, leftReg, rightReg, "op: /");
                break;
            case OpExp.LT:
                emitRO("SUB", ac, ac1, ac, "op: <");
                emitRM("JLT", ac, 2, pc, "branch if true");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.LE:
                emitRO("SUB", ac, ac1, ac, "op: <=");
                emitRM("JLE", ac, 2, pc, "branch if true");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.GT:
                emitRO("SUB", ac, ac1, ac, "op: >");
                emitRM("JGT", ac, 2, pc, "branch if true");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.GE:
                emitRO("SUB", ac, ac1, ac, "op: >=");
                emitRM("JGE", ac, 2, pc, "branch if true");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            // Logical operators AND and OR require short-circuit evaluation logic, which can be more complex
            // Here's a simplified version for AND:
            case OpExp.AND:
                emitRO("MUL", ac, ac1, ac, "op: AND");
                emitRM("JEQ", ac, 2, pc, "branch if false");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            // For OR, we would use a similar approach, but with the addition logic
            case OpExp.OR:
                emitRO("OR", ac, ac1, ac, "op: OR");  // Logical OR
                break;
            // For NOT and UMINUS, these are unary operations and would typically be handled in their own visit methods
            case OpExp.NOT:
                // NOT operation typically works on a single operand, adjust accordingly
                emitRO("NOT", ac, ac, 0, "op: NOT");  // Logical NOT
                break;
            case OpExp.UMINUS:
                emitRO("NEG", ac, ac, 0, "op: unary -");  // Unary minus
                break;
            default:
                emitComment("Error: Unsupported operation");
                break;
        }
    }

@Override
public void visit(CallExp callExp, int level, boolean isAddr) {
    // emitComment("CallExp: " + callExp.func);
    // // Step 1: Evaluate arguments and store their results on the stack.
    // if ("input".equals(callExp.func)) {
    //     // Emit code for reading an integer value from standard input.
    //     emitRO("IN", ac, 0, 0, "input integer value");
    //     return;
    // } else if ("output".equals(callExp.func)) {
    //     // Evaluate the single argument for output.
    //     callExp.args.accept(this, level + 1, false);
    //     emitRO("OUT", ac, 0, 0, "output integer value");
    //     return;
    // }
    emitComment("CallExp: " + callExp.func);
    if ("input".equals(callExp.func)) {
        emitRO("IN", ac, 0, 0, "input integer value");
        return;
    } else if ("output".equals(callExp.func)) {
        if (callExp.args != null) {
            callExp.args.accept(this, level + 1, false); // Evaluate argument for output
        } else {
            // Assume fac is a local variable
            emitRM("LD", ac, localVarOffset, fp, "Load fac value");
        }
        emitRO("OUT", ac, 0, 0, "output integer value");
        return;
    }
    // Evaluate and store arguments
    int argOffset = -1; // Start offset for arguments
    for (ExpList argList = callExp.args; argList != null; argList = argList.tail) {
        argList.head.accept(this, level + 1, false);
        emitRM("ST", ac, argOffset--, sp, "push argument");
    }
    // Check for function existence
    if (!functionDirectory.containsKey(callExp.func)) {
        System.err.println("Undefined function call to " + callExp.func);
        return;
    }
    // Jump to function
    int funcEntry = functionDirectory.get(callExp.func);
    emitRM("LDA", pc, funcEntry - emitLoc - 1, pc, "jump to function");
    // Adjust stack after call
    emitRM("LDA", sp, argOffset + 1, sp, "adjust sp back after call");
    emitComment("end of function call " + callExp.func);
}

    // @Override
    // public void visit(CallExp callExp, int level, boolean isAddr) {
    // // Debug output to list functions in the directory before checking a specific function call
    // // System.out.println("[Debug] Current functionDirectory contents:");
    // // functionDirectory.forEach((funcName, entryPoint) -> System.out.println(funcName + " -> " + entryPoint));
    //     emitComment("CallExp: " + callExp.func);
    //     // Step 1: Evaluate arguments and store their results on the stack.
    //     if ("input".equals(callExp.func)) {
    //         // Emit code for reading an integer value from standard input.
    //         emitRO("IN", ac, 0, 0, "input integer value");
    //     } else if ("output".equals(callExp.func)) {
    //         // Evaluate the single argument for output.
    //         callExp.args.accept(this, level + 1, false); // Assuming the argument to output is pushed onto the stack or placed in a register
    //         emitRO("OUT", ac, 0, 0, "output integer value");
    //     }

    //     ExpList argList = callExp.args;
    //     int argCount = 0;
    //     while (argList != null && argList.head != null) {
    //         argList.head.accept(this, level + 1, false); // Evaluate and result in 'ac'
    //         // Assuming each argument's result is now in 'ac', push it onto the stack.
    //         emitRM("ST", ac, --globalVarOffset, mp, "push argument for " + callExp.func);
    //         argList = argList.tail;
    //         argCount++;
    //     }

    //     // Safety check for function existence in the directory
    //     if (!functionDirectory.containsKey(callExp.func)) {
    //         System.err.println("Undefined function call to " + callExp.func);
    //         return; // Early return or handle undefined function more gracefully
    //     }

    //     // Step 2: Jump to the function's code.
    //     int funcEntry = functionDirectory.get(callExp.func);
    //     emitComment("Function call to " + callExp.func);
    //     emitRM("LDA", pc, funcEntry - (currentLocation + 1), pc, "jump to function " + callExp.func);

    //     // Consideration for function return process
    //     emitComment("Function call " + callExp.func + " return");
    //     // Assuming functions return the result in 'ac' and that we need to adjust the stack pointer back after the call
    //     emitRM("LDA", sp, argCount, sp, "adjust sp back after function call " + callExp.func);

    //     // If the call expression itself is used in a larger expression and expects an address
    //     if (isAddr) {
    //         // You might need to handle this scenario based on your language's semantics and the capabilities of the TM simulator.
    //         // This might involve storing the result in a temporary location or handling it differently.
    //         emitComment("Handling function return as an address might not be directly supported and needs specific handling");
    //     }
    // }

    @Override
    public void visit(VarExp varExp, int level, boolean isAddr) {
        emitComment("Variable Expression: " + varExp.variable.name);
        int offset;
        int baseReg;
        // Determine the base register and calculate offset based on the scope level.
        if (level == 0) { // Global variables
            baseReg = gp;
            offset = globalVarOffset;  // Direct use of globalVarOffset
            globalVarOffset--;  // Adjust globalVarOffset assuming each variable takes 1 memory unit.
        } else {  // Local variables
            baseReg = fp;
            offset = localVarOffset;  // Use localVarOffset to manage local variables
            localVarOffset--;  // Adjust localVarOffset for each local variable declared.
        }
        if (isAddr) {
            // If address is requested, load the address (base + offset) into the accumulator
            emitRM("LDA", ac, offset, baseReg, "Load address of " + varExp.variable.name);
        } else {
            // Load the variable's value into the accumulator
            emitRM("LD", ac, offset, baseReg, "Load value of " + varExp.variable.name);
        }
    }

    @Override
    public void visit(IntExp intExp, int level, boolean isAddr) {
        emitRM("LDC", ac, intExp.value, 0, "load const");
    }

    @Override
    public void visit(BoolExp boolExp, int level, boolean isAddr) {
        emitComment("BoolExp");
        int value = boolExp.value ? 1 : 0;  // Convert boolean to integer value
        emitRM("LDC", ac, value, 0, "load " + (boolExp.value ? "true" : "false"));
    }


    @Override
    public void visit(SimpleVar simpleVar, int level, boolean isAddr) {
        emitComment("SimpleVar: " + simpleVar.name);

        int varOffset = getVariableOffset(simpleVar.name, level);
        if (isAddr) {
            // If we need the address of the variable, adjust based on global or local scope
            emitRM("LDA", ac, varOffset, level == 0 ? gp : fp, "load address of " + simpleVar.name);
        } else {
            // Load the variable's value into the accumulator
            emitRM("LD", ac, varOffset, level == 0 ? gp : fp, "load value of " + simpleVar.name);
        }
    }


    @Override
    public void visit(IndexVar indexVar, int level, boolean isAddr) {
        // emitComment("IndexVar: " + indexVar.name);

        // // First, load the base address of the array
        // int baseAddrOffset = getVariableOffset(indexVar.name, level);
        // emitRM("LDA", ac, baseAddrOffset, level == 0 ? gp : fp, "load base address of " + indexVar.name);

        // // Next, evaluate the index expression and store its result in a temporary register
        // indexVar.index.accept(this, level, false);
        // emitRM("ST", ac, tmpOffset, mp, "store index value");
        
        // // Calculate the effective address by adding the base address and the index
        // // Assume `tmpOffset` is the offset for a temporary storage location on the stack
        // emitRM("LD", ac1, tmpOffset, mp, "load index value");
        // emitRO("SUB", ac, ac, ac1, "calculate effective address of the array element");

        // if (!isAddr) {
        //     // If we need the value at the index, not the address, load it into the accumulator
        //     emitRM("LD", ac, 0, ac, "load value of array element");
        // }
        // // Note: This example assumes the array index is 0-based and each element is 1 word in size.
    
    }

    @Override
    public void visit(NameTy nameTy, int level, boolean isAddr) {
        // This might not produce direct assembly code but could set context
        emitComment("NameTy:/tType: " + (nameTy.typ == NameTy.INT ? "int" : "void"));
    }

    @Override
    public void visit(NilExp nilExp, int level, boolean isAddr) {
        // Since NilExp represents a "no operation" or null value in the AST,
        // it does not directly translate to a TM assembly instruction.
        // Emit a comment for clarity or simply pass.
        emitComment("NilExp encountered - no operation generated");
    }

    @Override
    public void visit(ErrorDec errorDec, int level, boolean isAddr) {
        //for error handling
    }

/**
 * Calculates the memory offset for a local variable relative to the current stack frame.
 * @param baseOffset The base offset of the variable within its declared scope.
 * @return The adjusted offset relative to the frame pointer (fp).
 */
private int calculateLocalOffset(int baseOffset) {
    // Assume all local variables are of uniform size (e.g., each takes 1 memory unit).
    // The baseOffset represents the position of the variable within the local scope (e.g., 0 for the first local variable, 1 for the second).
    return -baseOffset - ADMIN_FIELDS; // Admin fields may include saved frame pointer, return address, etc.
}

    private int getVariableOffset(String varName, int level) {
        // Placeholder for the actual offset retrieval logic.
        int offset = 0;

        // Example of querying a symbol table. The actual implementation would depend on
        // how the symbol table is structured and how scope levels are managed.
        //symbolTableEntry entry = symbolTable.lookup(varName);
        // if (entry != null) {
        //     // Check if the variable is in the current scope or any enclosing scope up to the global scope.
        //     while (entry != null && entry.level > level) {
        //         entry = entry.enclosingScope;
        //     }
        //     if (entry != null && entry.level == level) {
        //         offset = entry.offset;
        //     } else {
        //         // Handle error: Variable not found in the current or enclosing scopes.
        //         System.err.println("Error: Variable '" + varName + "' not found in scope.");
        //     }
        // } else {
        //     // Handle error: Variable not declared.
        //     System.err.println("Error: Variable '" + varName + "' not declared.");
        // }

        return 1;//offset;
    }

    private void backpatchJump(int jumpLoc, int targetLoc) {
        int offset = targetLoc - jumpLoc - 1;
        emitBackup(jumpLoc); // Move back to the reserved jump instruction location
        emitRM("LDA", pc, offset, pc, "Jump around I/O routines");
        emitRestore(); // Return to the previous location in the emission sequence
    }

    private void incrementEmitLoc() {
        emitLoc++;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitFunctionExit() {
        emitComment("Exiting current function");
        emitRM("LD", 5, 0, 5, "Restore old fp");
    }

    public void setGlobalOffset(int offset) {
        this.globalVarOffset = offset;
    }

    public void emitFunctionEntry(String functionName, int localVariablesCount) {
        int frameOffset = -2 - localVariablesCount; // Adjust for 'ofp' and 'return addr', and local variables
        emitComment(String.format("Entering function: %s with frameOffset %d", functionName, frameOffset));
        // Emit TM instructions to adjust fp and allocate space for locals
        emitRM("ST", 5, globalVarOffset + (-1), 5, "Store old fp at top of frame");
        emitRM("LDA", 5, frameOffset, 5, "Adjust fp for new frame");
        // Additional setup for parameters and local variables could be emitted here
    }

    private void emitComment(String comment) {
        codeBuilder.append("* ").append(comment).append("\n");
    }

    private void emitRO(String op, int r, int s, int t, String comment) {
        codeBuilder.append(String.format("%3d:  %5s  %d,%d,%d \t%s\n", emitLoc++, op, r, s, t, comment));
        updateHighEmitLoc();
    }

    private void emitRM(String op, int r, int d, int s, String comment) {
        codeBuilder.append(String.format("%3d:  %5s  %d,%d(%d) \t%s\n", emitLoc++, op, r, d, s, comment));
        updateHighEmitLoc();
    }

    private void emitRM_Abs(String op, int r, int a, String comment) {
        codeBuilder.append(String.format("%3d:  %5s  %d,%d(%d) \t%s\n", emitLoc++, op, r, a - (emitLoc + 1), pc, comment));
        updateHighEmitLoc();
    }

    private void updateHighEmitLoc() {
        if (emitLoc > highEmitLoc) highEmitLoc = emitLoc;
    }

    private void emitFinale() {
        emitComment("End of execution.");
        emitRO("HALT", 0, 0, 0, "");
    }

    // Skips a certain number of locations in the assembly output.
    public int emitSkip(int amount) {
        int oldLocation = emitLoc;
        emitLoc += amount;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
        return oldLocation;
    }

    // Moves the current location counter backward for backpatching purposes.
    public void emitBackup(int loc) {
        backupStack.push(emitLoc);
        emitLoc = loc;
    }

    // Restores the current location counter to the most recently backed-up position.
    public void emitRestore() {
        if (!backupStack.isEmpty()) {
            emitLoc = backupStack.pop();
        }
    }
}