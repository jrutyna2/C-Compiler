package absyn;

public class CompoundExp extends Exp {
    public VarDecList localDecs;
    public ExpList stmtList;

    public CompoundExp(int row, int col, VarDecList localDecs, ExpList stmtList) {
        super(row, col);
        this.localDecs = localDecs;
        this.stmtList = stmtList;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return "LocalDeclarations: " + localDecs.toString() + "StatementList: " + stmtList.toString();
    }
}
