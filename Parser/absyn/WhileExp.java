package absyn;

public class WhileExp extends Exp {
    public Exp test;
    public Exp body;

    public WhileExp(int row, int col, Exp test, Exp body) {
        super(row, col);
        this.test = test;
        this.body = body;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return "while (" + test.toString() + ") " + body.toString();
    }
}
