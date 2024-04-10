package absyn;

public class NameTy extends Absyn {
    // Define type constants
    public final static int BOOL = 0;
    public final static int INT = 1;
    public final static int VOID = 2;

    public int typ; // The type of the variable/function return, using the constants above

    public NameTy(int row, int col, int typ) {
        super(row, col);
        this.typ = typ;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        switch (typ) {
            case BOOL:
                return "BOOL";
            case INT:
                return "INT";
            case VOID:
                return "VOID";
            default:
                return "UNKNOWN";
        }
    }
}
