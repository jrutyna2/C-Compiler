package absyn;

public class ReturnExp extends Exp {
    public Exp exp; // This can be null if the function returns void

    public ReturnExp(int row, int col, Exp exp) {
        super(row, col);
        this.exp = exp;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        if (exp == null) {
            return "return;";
        } else {
            // return "return " + exp.toString() + ";";
            return exp.toString();
        }
    }
}
