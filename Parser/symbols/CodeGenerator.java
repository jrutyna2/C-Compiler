package symbols;
import absyn.*;
import utils.*;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Deque;
public class CodeGenerator implements AbsynVisitor {
   
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

    private int globalOffset = 0; // Track the offset for global variables
    private int localOffset = 0;  // Track the offset for local variables
    private int mainEntry = -1; // Entry point for the main function
    private int inputEntry = -1;
    private int outputEntry = -1;
    // A stack to remember positions for emitBackup
    private Stack<Integer> backupStack = new Stack<>();
    private Map<String, Integer> functionDirectory = new HashMap<>();
    private static final int returnAddrOffset = -1; // If the return address is stored just below the fp
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
        emitComment("End of standard prelude.");
    }

    private void emitIORoutines() {
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

    private void emitJumpAroundIOCode(int jumpTarget) {
        // Placeholder for the actual method to emit an absolute jump instruction
        emitRM_Abs("LDA", pc, jumpTarget, "Jump around I/O routines to start of main program");
    }

    @Override
    public void visit(Program program, int level, boolean isAddr) {
        // Emit the standard prelude first
        emitStandardPrelude();
        // Reserve space for jump instruction to skip around I/O code
        int jumpInstLoc = emitLoc; // Record the location for backpatching
        emitSkip(1); // Effectively reserves space by incrementing emitLoc
        // Emit predefined I/O routines immediately after the prelude
        emitIORoutines();
        // Now backpatch the jump instruction to skip over I/O routines
        int mainStartLoc = emitLoc; // Use emitLoc directly as the current location
        emitBackup(jumpInstLoc); // Move back to the jump instruction's location
        // Assuming 'LDA pc, offset(pc)' is how you've structured jump instructions...
        emitRM("LDA", pc, mainStartLoc - (jumpInstLoc + 1), pc, "Jump around I/O routines to the start of the main program");
        emitRestore(); // Return to the current location to continue code generation
        emitComment("C- compilation to TM code");
        // Process the program declarations and statements
        if (program.declarations != null) {
            program.declarations.accept(this, level, false);
        }
        // Emit a HALT instruction at the end of the program
        emitRO("HALT", 0, 0, 0, "End of program execution");
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

    @Override
    public void visit(FunDec funDec, int level, boolean isAddr) {
        if (funDec.funcName.equals("main")) {
            mainEntry = emitSkip(0); // Mark the start of the main function
            emitComment("Start of main function");
        } else {
            // Handle other functions
            functionDirectory.put(funDec.funcName, emitSkip(0));
            emitComment("Function declaration: " + funDec.funcName);
            System.out.println("Function declaration: " + funDec.funcName);
        }

        // Initialize localOffset for new function scope
        localOffset = 0;

        // Handle function arguments and body
        if (funDec.params != null) {
            funDec.params.accept(this, level + 1, false);
        }
        if (funDec.body != null) {
            funDec.body.accept(this, level + 1, false);
        }
        // Function return or end
        emitComment("End of function: " + funDec.funcName);
        if (!funDec.funcName.equals("main")) {
            // Implement return mechanism for user-defined functions
            // For example, jump back to the caller, handle stack frame clean-up
            emitRM("LD", pc, -1, fp, "Return to caller");
        } else {
            emitRO("HALT", 0, 0, 0, "End of program execution");
        }
    }

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

    //to visit variable and array declarations
    @Override
    public void visit(SimpleDec simpleDec, int level, boolean isAddr) {
        if (level == 0) {
            emitComment("Global variable: " + simpleDec.name);
            globalOffset--; // Adjust globalOffset for the new global variable
            // Example: initialize global variable to 0
            emitRM("LDC", ac, 0, 0, "Init " + simpleDec.name + " to 0");
            emitRM("ST", ac, globalOffset, gp, "Store global variable " + simpleDec.name);
        }
    }
    
    @Override
    public void visit(ArrayDec arrayDec, int level, boolean isAddr) {
        if (level == 0) { // Assuming global array
            emitComment("Global array: " + arrayDec.name);
            // Allocate space; assuming each element is 1 memory cell
            emitRM("LDC", ac, arrayDec.size, 0, "load array size");
            emitRM("ST", ac, globalOffset--, gp, "store array size at global offset");
            for (int i = 0; i < arrayDec.size; i++) {
                emitRM("LDC", ac, 0, 0, "initialize to 0");
                emitRM("ST", ac, globalOffset--, gp, "init array element");
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
    // Assuming global variables start at a fixed offset and local variables use a stack-like allocation
    int offset;
    if (level == 0) { // Global variable
        offset = globalOffset++;
    } else { // Local variable
        offset = --localVarOffset; // Allocate from the current frame pointer
    }
    // Determine the base register for global vs. local scope
    int baseReg = (level == 0) ? gp : fp;
    // Now handle the LHS to store the result
    assignExp.lhs.accept(this, level, true); // Accept with isAddr = true, assuming it sets up for an address
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
    public void visit(IntExp intExp, int level, boolean isAddr) {
        emitRM("LDC", ac, intExp.value, 0, "load const");
    }

    @Override
    public void visit(IfExp ifExp, int level, boolean isAddr) {
        ifExp.test.accept(this, level, false); // Evaluate condition; result in AC
        int savedLoc = emitLoc;
        emitRM("JEQ", ac, 0, pc, "if: jmp to else part");
        //ifExp.thenpart.accept(this, level, false);

        int savedLoc2 = emitLoc;
        emitRM("LDA", pc, 0, pc, "jmp to end");
        int currentLoc = emitLoc;

        // Backpatching not directly supported; this is illustrative
        emitLoc = savedLoc;
        emitRM("JEQ", ac, currentLoc - emitLoc - 1, pc, "if: jmp to else part (backpatch)");

        // if (ifExp.elsepart != null) {
        //     ifExp.elsepart.accept(this, level, false);
        // }
    }
   
    @Override
    public void visit(ErrorDec errorDec, int level, boolean isAddr) {
        //for error handling
    }

    @Override
    public void visit(OpExp opExp, int level, boolean isAddr) {
        // Generate code for the left and right expressions of the operation
        opExp.left.accept(this, level, false);
        emitRM("ST", ac, --mp, mp, "op: push left");
        opExp.right.accept(this, level, false);
        emitRM("LD", ac1, mp++, mp, "op: load left");

        // Generate code based on the type of the operation
        switch (opExp.op) {
            case OpExp.PLUS:
                emitRO("ADD", ac, ac1, ac, "op: +");
                break;
            case OpExp.MINUS:
                emitRO("SUB", ac, ac1, ac, "op: -");
                break;
            case OpExp.MUL:
                emitRO("MUL", ac, ac1, ac, "op: *");
                break;
            case OpExp.DIV:
                emitRO("DIV", ac, ac1, ac, "op: /");
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
            // For NOT and UMINUS, these are unary operations and would typically be handled in their own visit methods
            default:
                emitComment("Error: Unsupported operation");
                break;
        }
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
    public void visit(VarDecList varDecList, int level, boolean isAddr) {
        while (varDecList != null && varDecList.head != null) { // Ensures null safety
            varDecList.head.accept(this, level + 1, isAddr);
            varDecList = varDecList.tail;
        }
    }

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

        // If there are variable declarations in the compound statement, generate code for them.
        if (compoundExp.localDecs != null) {
            emitComment("Variable declarations in compound statement");
            compoundExp.localDecs.accept(this, level + 1, false);
        }

        // Generate code for each statement or expression in the compound statement.
        if (compoundExp.stmtList != null) {
            emitComment("Statements/Expressions in compound statement");
            compoundExp.stmtList.accept(this, level + 1, isAddr);
        }

        emitComment("End compound statement");
    }

    @Override
    public void visit(WhileExp whileExp, int level, boolean isAddr) {
        emitComment("start of while loop");

        int startLoopLoc = emitLoc; // Directly use integer for loop start location
        whileExp.test.accept(this, level + 1, false); // Generate test expression code
        
        int jumpToEndLoc = emitSkip(1); // Reserve space for conditional jump out of loop
        whileExp.body.accept(this, level + 1, false); // Generate loop body code

        emitRM_Abs("LDA", pc, startLoopLoc, "jump back to the start of the loop"); // Jump back to start
        int loopEndLoc = emitLoc; // Location after loop body
        
        emitBackup(jumpToEndLoc); 
        emitRM_Abs("JEQ", ac, loopEndLoc, "Jump to end of loop if condition is false"); // Correct backpatching
        emitRestore();

        emitComment("end of while loop");
    }

    @Override
    public void visit(ReturnExp returnExp, int level, boolean isAddr) {
        emitComment("start of return");

        // If there's an expression to return, evaluate it and store the result
        if (returnExp.exp != null) {
            returnExp.exp.accept(this, level + 1, false);
            // Assume the result is now in 'ac' and needs to be moved to the return value location
            // if your convention specifies a location for return values.
        }

        // Assuming the function's prologue has set up 'fp' such that the return address is at a known offset
        emitRM("LD", pc, returnAddrOffset, fp, "Load return address and jump to caller");

        emitComment("end of return");
    }

    @Override
    public void visit(CallExp callExp, int level, boolean isAddr) {
    // Debug output to list functions in the directory before checking a specific function call
    // System.out.println("[Debug] Current functionDirectory contents:");
    // functionDirectory.forEach((funcName, entryPoint) -> System.out.println(funcName + " -> " + entryPoint));
        emitComment("CallExp: " + callExp.func);
        // Step 1: Evaluate arguments and store their results on the stack.
        if ("input".equals(callExp.func)) {
            // Emit code for reading an integer value from standard input.
            emitRO("IN", ac, 0, 0, "input integer value");
        } else if ("output".equals(callExp.func)) {
            // Evaluate the single argument for output.
            callExp.args.accept(this, level + 1, false); // Assuming the argument to output is pushed onto the stack or placed in a register
            emitRO("OUT", ac, 0, 0, "output integer value");
        }

        ExpList argList = callExp.args;
        int argCount = 0;
        while (argList != null && argList.head != null) {
            argList.head.accept(this, level + 1, false); // Evaluate and result in 'ac'
            // Assuming each argument's result is now in 'ac', push it onto the stack.
            emitRM("ST", ac, --globalOffset, mp, "push argument for " + callExp.func);
            argList = argList.tail;
            argCount++;
        }

        // Safety check for function existence in the directory
        if (!functionDirectory.containsKey(callExp.func)) {
            System.err.println("Undefined function call to " + callExp.func);
            return; // Early return or handle undefined function more gracefully
        }

        // Step 2: Jump to the function's code.
        int funcEntry = functionDirectory.get(callExp.func);
        emitComment("Function call to " + callExp.func);
        emitRM("LDA", pc, funcEntry - (currentLocation + 1), pc, "jump to function " + callExp.func);

        // Consideration for function return process
        emitComment("Function call " + callExp.func + " return");
        // Assuming functions return the result in 'ac' and that we need to adjust the stack pointer back after the call
        emitRM("LDA", sp, argCount, sp, "adjust sp back after function call " + callExp.func);

        // If the call expression itself is used in a larger expression and expects an address
        if (isAddr) {
            // You might need to handle this scenario based on your language's semantics and the capabilities of the TM simulator.
            // This might involve storing the result in a temporary location or handling it differently.
            emitComment("Handling function return as an address might not be directly supported and needs specific handling");
        }
    }

    @Override
    public void visit(VarExp varExp, int level, boolean isAddr) {
        // emitComment("VarExp: " + varExp.variable.name);

        // int varOffset = getVariableOffset(varExp.variable.name);

        // if (isAddr) {
        //     // If we need the address of the variable, load it into the accumulator.
        //     emitRM("LDA", ac, varOffset, gp, "load address of " + varExp.variable.name);
        // } else {
        //     // Otherwise, load the variable's value into the accumulator.
        //     emitRM("LD", ac, varOffset, gp, "load value of " + varExp.variable.name);
        // }
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
    public void visit(BoolExp boolExp, int level, boolean isAddr) {
        emitComment("BoolExp");

        // Assuming BoolExp contains a 'value' field that is either true or false.
        if (boolExp.value) {
            // If the boolean value is true, load 1 into the accumulator.
            emitRM("LDC", ac, 1, 0, "load true");
        } else {
            // If the boolean value is false, load 0 into the accumulator.
            emitRM("LDC", ac, 0, 0, "load false");
        }
        
        // If the boolean expression is used in a context that requires its address (unlikely for simple booleans),
        // additional handling would be needed here, which might involve storing the boolean value in a temporary
        // location and loading its address into the accumulator.
        if (isAddr) {
            emitComment("Address of BoolExp requested, handle accordingly");
            // Depending on your language semantics and TM capabilities, you might need to handle this case.
            // For example, storing the boolean value in memory and loading its address.
        }
    }


/**
 * Calculates the memory offset for a local variable relative to the current stack frame.
 * @param baseOffset The base offset of the variable within its declared scope.
 * @param level The current nesting level or scope level.
 * @return The adjusted offset relative to the frame pointer (fp).
 */
private int calculateLocalOffset(int baseOffset, int level) {
    // This method adjusts the baseOffset based on the current level.
    // Implement the logic based on how your stack frames are organized.
    // This is a placeholder for illustration.
    return baseOffset - (level * FRAME_SIZE); // FRAME_SIZE would be a constant representing the size of a stack frame.
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
        this.globalOffset = offset;
    }

    public void emitFunctionEntry(String functionName, int localVariablesCount) {
        int frameOffset = -2 - localVariablesCount; // Adjust for 'ofp' and 'return addr', and local variables
        emitComment(String.format("Entering function: %s with frameOffset %d", functionName, frameOffset));
        // Emit TM instructions to adjust fp and allocate space for locals
        emitRM("ST", 5, globalOffset + (-1), 5, "Store old fp at top of frame");
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