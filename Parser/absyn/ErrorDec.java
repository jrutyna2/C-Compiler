package absyn;

public class ErrorDec extends Dec {
    public String errorMessage;

    public ErrorDec(int row, int col, String errorMessage) {
        super(row, col); // Assuming a constructor similar to other Dec subclasses
        // this.row = row;
        // this.col = col;
        this.errorMessage = errorMessage;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        return "Error at row " + row + ", col " + col + ": " + errorMessage;
    }
}
