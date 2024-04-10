package absyn;

public class OpExp extends Exp {
    // Define operator constants
    public final static int PLUS = 0;
    public final static int MINUS = 1;
    public final static int UMINUS = 2; // Unary minus
    public final static int MUL = 3;
    public final static int DIV = 4;
    public final static int EQ = 5; // Equal
    public final static int NE = 6; // Not equal
    public final static int LT = 7; // Less than
    public final static int LE = 8; // Less than or equal to
    public final static int GT = 9; // Greater than
    public final static int GE = 10; // Greater than or equal to
    public final static int NOT = 11;
    public final static int AND = 12;
    public final static int OR = 13;

    public Exp left;
    public int op; // The operation, using the constants defined above
    public Exp right; // Right-hand side of the operation; can be null for unary ops

    public OpExp(int row, int col, Exp left, int op, Exp right) {
        super(row, col);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        String opStr = switch (op) {
            case PLUS -> "+";
            case MINUS -> "-";
            case UMINUS -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case EQ -> "==";
            case NE -> "!=";
            case LT -> "<";
            case LE -> "<=";
            case GT -> ">";
            case GE -> ">=";
            case NOT -> "!";
            case AND -> "&&";
            case OR -> "||";
            default -> "unknown_op";
        };
        return "(" + left.toString() + " " + opStr + " " + right.toString() + ")";
    }
}