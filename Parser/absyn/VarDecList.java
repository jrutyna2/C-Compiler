package absyn;

public class VarDecList extends Absyn {
    public VarDec head;
    public VarDecList tail;

    public VarDecList(VarDec head, VarDecList tail) {
        super(-1, -1); // Placeholder values for row and col
        this.head = head;
        this.tail = tail;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        VarDecList current = this;
        while (current != null) {
            if (current.head != null) {
                sb.append(current.head.toString());
                if (current.tail != null) {
                    sb.append(", ");
                }
            }
            current = current.tail;
        }
        return "VarDecList: [" + sb.toString() + "]";
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
