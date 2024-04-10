package absyn;

public class VarExp extends Exp {
    public Var variable;

    public VarExp(int row, int col, Var variable) {
        super(row, col);
        this.variable = variable;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return variable.toString(); // Delegate to Var's toString method
    }
}
