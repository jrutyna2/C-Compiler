package absyn;

public class SimpleDec extends VarDec {

    public SimpleDec(int row, int col, NameTy typ, String name) {
        super(row, col, typ, name);
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        // Call super.typ.toString() to use the NameTy's toString method
        return "SimpleDec: " + name + ", type: " + typ.toString();
    }
}
