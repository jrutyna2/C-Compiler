package absyn;

abstract public class VarDec extends Dec {
    public NameTy typ;
    public String name;

    public VarDec(int row, int col, NameTy typ, String name) {
        super(row, col);
        this.typ = typ;
        this.name = name;
    }

    // Getter for the type
    public NameTy getType() {
        return typ;
    }

    // Getter for the name
    public String getName() {
        return name;
    }
}
