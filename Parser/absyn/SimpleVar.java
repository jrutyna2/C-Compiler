package absyn;

public class SimpleVar extends Var {

    public SimpleVar(int row, int col, String name) {
        super(row, col, name);
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        // return "SimpleVar: " + name; // Return var's type and name as string rep
        return name;
    }
}
