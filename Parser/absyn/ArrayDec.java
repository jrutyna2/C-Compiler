package absyn;

public class ArrayDec extends VarDec {
    public int size; // The size of the array

    public ArrayDec(int row, int col, NameTy typ, String name, int size) {
        super(row, col, typ, name);
        this.size = size;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    // Method to get the size of the array
    public int getSize() {
        return size;
    }

    // Method to represent the ArrayDec node as a String
    @Override
    public String toString() {
        return "ArrayDec: " + name + ", type: " + typ.toString() + "[" + size + "]";
    }
}
