package absyn;

public interface AbsynVisitor {
    // Abstract Syntax Tree classes visit methods
    public void visit(ErrorDec errorDec, int level, boolean isAddr);
    
    public void visit(Program program, int level, boolean isAddr);
    public void visit(DecList decList, int level, boolean isAddr);
    public void visit(SimpleDec simpleDec, int level, boolean isAddr);
    public void visit(ArrayDec arrayDec, int level, boolean isAddr);
    public void visit(NameTy nameTy, int level, boolean isAddr);
    public void visit(FunDec funDec, int level, boolean isAddr);
    public void visit(NilExp nilExp, int level, boolean isAddr);
    public void visit(VarDecList varDecList, int level, boolean isAddr);

    public void visit(ExpList expList, int level, boolean isAddr);
    public void visit(CompoundExp compoundExp, int level, boolean isAddr);
    
    public void visit(IfExp ifExp, int level, boolean isAddr);
    public void visit(WhileExp whileExp, int level, boolean isAddr);
    public void visit(ReturnExp returnExp, int level, boolean isAddr);
    public void visit(VarExp varExp, int level, boolean isAddr);
    public void visit(AssignExp assignExp, int level, boolean isAddr);
    public void visit(SimpleVar simpleVar, int level, boolean isAddr);
    public void visit(IndexVar indexVar, int level, boolean isAddr);
    public void visit(OpExp opExp, int level, boolean isAddr);
    public void visit(IntExp intExp, int level, boolean isAddr);
    public void visit(BoolExp boolExp, int level, boolean isAddr);
    public void visit(CallExp callExp, int level, boolean isAddr);
}
