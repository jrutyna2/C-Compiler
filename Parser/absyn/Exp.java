package absyn;

public abstract class Exp extends Absyn {
    public Dec dtype;
    public NameTy type;

    public Exp(int row, int col) {
        super(row, col);
        this.dtype = null;
    }

    public Dec getType() {
        return dtype;
    }

    public void setType(Dec dtype) {
        this.dtype = dtype;
    }
    
    public boolean isBoolean() {
        return this instanceof BoolExp;
    }
    
    @Override
    public abstract String toString();
}
