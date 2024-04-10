package absyn;

public class ExpList extends Absyn {
    public Exp head;
    public ExpList tail;

    public ExpList(Exp head, ExpList tail) {
        super(-1, -1);
        this.head = head;
        this.tail = tail;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        // Start with the head of the list
        String result = (head == null) ? "" : head.toString();
        // If there's more in the list, handle it recursively
        if (tail != null) {
            result += ", " + tail.toString(); // Use a comma to separate expressions
        }
        return result;
    }
}
