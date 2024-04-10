package absyn;

public class IndexVar extends Var {
    public Exp index;

    public IndexVar(int row, int col, String name, Exp index) {
        super(row, col, name); // Pass name to the superclass constructor
        this.index = index;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    // Implementation of the abstract toString method
    @Override
    public String toString() {
        // Return var's type and name as string rep
        return "IndexVar: " + name + "[" + (index == null ? "" : index.toString()) + "]"; // Handle null index expression if necessary
    }
}
