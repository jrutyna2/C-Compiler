package absyn;

public class IntExp extends Exp {
    public int value;

    public IntExp(int row, int col, int value) {
        super(row, col);
        this.value = value;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
