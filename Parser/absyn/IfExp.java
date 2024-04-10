package absyn;

public class IfExp extends Exp {
    public Exp test;
    public Exp thenClause;
    public Exp elseClause; // This can be null if there is no else clause

    public IfExp(int row, int col, Exp test, Exp thenClause, Exp elseClause) {
        super(row, col);
        this.test = test;
        this.thenClause = thenClause;
        this.elseClause = elseClause;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        String result = "if (" + test.toString() + ") " + thenClause.toString();
        if (elseClause != null) {
            result += " else " + elseClause.toString();
        }
        return result;
    }
}
