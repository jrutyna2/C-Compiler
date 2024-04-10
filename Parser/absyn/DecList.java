package absyn;

public class DecList extends Absyn {
    public Dec head;
    public DecList tail;

    public DecList(Dec head, DecList tail) {
        super(0, 0); // Placeholder values for row and col
        this.head = head;
        this.tail = tail;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DecList current = this;
        while (current != null && current.head != null) {
            sb.append(current.head.toString());
            if (current.tail != null) {
                sb.append(", ");
            }
            current = current.tail;
        }
        return "DecList: [" + sb.toString() + "]";
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
