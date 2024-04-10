package absyn;

public class BoolExp extends Exp {
    public boolean value;

    public BoolExp(int row, int col, boolean value) {
        super(row, col);
        this.value = value;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
