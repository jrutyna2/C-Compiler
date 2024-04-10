package absyn;

abstract public class Absyn {
    public int row, col;

    public Absyn(int row, int col) {
            this.row = row;
            this.col = col;
    }

    abstract public void accept( AbsynVisitor visitor, int level, boolean isAddr );
}
