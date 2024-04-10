package absyn;

public class NilExp extends Exp {
    public NilExp(int row, int col) {
        super(row, col);
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return "NilExp";
    }
}
