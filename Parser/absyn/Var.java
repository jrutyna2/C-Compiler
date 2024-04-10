package absyn;

abstract public class Var extends Absyn {
    public String name; // Consider using protected if subclasses need direct access

    public Var(int row, int col, String name) {
        super(row, col);
        this.name = name;
    }

    // Getter for the variable's name
    public String getName() {
        return name;
    }

    @Override
    public abstract String toString();
}
