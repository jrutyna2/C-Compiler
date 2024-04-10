Class-Based AST for C-minus

package absyn;

abstract class Absyn - Absyn(int row, int col)
    DecList(Dec head, DecList tail)
    ExpList(Exp head, ExpList tail)
    NameTy(int row, int col, int typ)
    Program(int row, int col, DecList declarations)
    VarDecList(VarDec head, VarDecList tail)

abstract class Var extends Absyn - Var(int row, int col, String name)
    SimpleVar(int row, int col, String name)
    IndexVar(int pos, String name, Exp index)

abstract class Exp extends Absyn - Exp(int row, int col)
    AssignExp(int row, int col, VarExp lhs, Exp rhs)
    BoolExp(int row, int col, boolean value)
    CallExp(int row, int col, String func, ExpList args)
    CompoundExp(int row, int col, VarDecList localDecs, ExpList stmtList)
    IfExp(int row, int col, Exp test, Exp thenClause, Exp elseClause)
    IntExp(int row, int col, int value)
    NilExp(int row, int col)
    OpExp(int row, int col, Exp left, int op, Exp right)
    ReturnExp(int row, int col, Exp exp)
    VarExp(int row, int col, Var variable)
    WhileExp(int row, int col, Exp test, Exp body)

abstract class Dec extends Absyn - Dec(int row, int col)
    ErrorDec(int row, int col, String errorMessage)
    FunDec(int row, int col, NameTy result, String funcName, VarDecList params, Exp body)

abstract class VarDec extends Dec - VarDec(int row, int col, NameTy typ, String name)
    ArrayDec(int row, int col, NameTy typ, String name, int size)
    SimpleDec(int row, int col, NameTy typ, String name)

// constants for op field of OpExp
final static int OpExp.PLUS, OpExp.MINUS,
OpExp.UMINUS, OpExp.MUL, OpExp.DIV,
OpExp.EQ, OpExp.NE, OpExp.LT,
OpExp.LE, OpExp.GT, OpExp.GE,
OpExp NOT, OpExp AND, OpExp OR;

// constants for typ field of NameTy:
final static int NameTy.BOOL, NameTy.INT, NameTy.VOID

OTHER
=== AbsynVisitor.java ===
package absyn;
interface AbsynVisitor {
    // Abstract Syntax Tree classes visit methods
    void visit(ErrorDec errorDec, int level);
    void visit(Program program, int level);
    void visit(DecList decList, int level);
    void visit(SimpleDec simpleDec, int level);
    void visit(ArrayDec arrayDec, int level);
    void visit(NameTy nameTy, int level);
    void visit(FunDec funDec, int level);
    void visit(NilExp nilExp, int level);
    void visit(VarDecList varDecList, int level);
    void visit(ExpList expList, int level);
    void visit(CompoundExp compoundExp, int level);
    void visit(IfExp ifExp, int level);
    void visit(WhileExp whileExp, int level);
    void visit(ReturnExp returnExp, int level);
    void visit(VarExp varExp, int level);
    void visit(AssignExp assignExp, int level);
    void visit(SimpleVar simpleVar, int level);
    void visit(IndexVar indexVar, int level);
    void visit(OpExp opExp, int level);
    void visit(IntExp intExp, int level);
    void visit(BoolExp boolExp, int level);
    void visit(CallExp callExp, int level);
}
