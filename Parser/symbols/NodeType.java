package symbols;

import absyn.Dec;

public class NodeType {
    public String name;
    public Dec dec;
    public int level;

    public NodeType(String name, Dec dec, int level) {
        this.name = name;
        this.dec = dec;
        this.level = level;
    }

    @Override
    public String toString() {
        // Using getClass().getSimpleName() to get a simple name of the Dec subclass
        // (e.g., "VarDec", "FunDec") as a representation of its type.
        String decType = dec.getClass().getSimpleName();
        return "Name: " + name + ", Type: " + decType + ", Level: " + level;
    }
}
