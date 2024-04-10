package absyn;

public class FunDec extends Dec {
    public NameTy result; // The return type of the function
    public String funcName; // The name of the function
    public VarDecList params; // List of parameters
    public Exp body; // The body of the function

    public FunDec(int row, int col, NameTy result, String funcName, VarDecList params, Exp body) {
        super(row, col);
        this.result = result;
        this.funcName = funcName;
        this.params = params;
        this.body = body;
    }

    @Override
    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Function: ").append(funcName);
        sb.append(", Return type: ").append(result);
        if (params != null) {
            sb.append(", Parameters: [").append(params.toString()).append("]");
        } else {
            sb.append(", Parameters: []");
        }
        if (body != null) {
            sb.append(", Body: ").append(body.toString());
        } else {
            sb.append(", Body: {}");
        }
        return sb.toString();
    }
}
