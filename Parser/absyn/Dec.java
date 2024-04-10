package absyn;

public abstract class Dec extends Absyn {
    public Dec(int row, int col) {
        super(row, col);
    }
    
    // Abstract toString method to be implemented by subclasses
    @Override
    public abstract String toString();
}
