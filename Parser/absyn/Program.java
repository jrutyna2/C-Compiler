package absyn;

public class Program extends Absyn {
    public DecList declarations;

    public Program(int row, int col, DecList declarations) {
        super(row, col);
        this.declarations = declarations;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        // Start with the basic program structure
        StringBuilder sb = new StringBuilder("Program:\n");
        
        // Check if there are declarations to print
        if (declarations != null) {
            sb.append(declarations.toString()); // Append the string representation of the declarations list
        } else {
            sb.append(" No Declarations");
        }
        
        return sb.toString();
    }
}
