package absyn;

public class CallExp extends Exp {
    public String func; // The name of the function being called
    public ExpList args; // The list of arguments passed to the function

    public CallExp(int row, int col, String func, ExpList args) {
        super(row, col);
        this.func = func;
        this.args = args;
    }

    public String getName() {
        return(func);
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(func).append("("); // Append function name and opening parenthesis

        // Append a string representation of each argument, separated by commas
        ExpList current = args;
        while (current != null && current.head != null) {
            sb.append(current.head.toString()); // Use toString of the head (which is an Exp)
            current = current.tail;
            if (current != null && current.head != null) {
                sb.append(", "); // Separate arguments with a comma
            }
        }

        sb.append(")"); // Append closing parenthesis
        return sb.toString();
    }
}
