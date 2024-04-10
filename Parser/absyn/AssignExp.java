package absyn;

public class AssignExp extends Exp {
    public VarExp lhs;
    public Exp rhs;

    public AssignExp(int row, int col, VarExp lhs, Exp rhs) {
        super(row, col);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        // This assumes that VarExp and Exp both have a functioning toString method.
        return lhs.toString() + " = " + rhs.toString();
    }
}
