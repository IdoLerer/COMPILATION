package AST;

public class AST_EXP_FUNC extends AST_EXP {
	public AST_EXP exp;
	public String name;
	public AST_EXP_LIST expList;
	public AST_EXP_FUNC(AST_VAR var, String name, AST_EXP_LIST expList) {
		this.exp = exp;
		this.name = name;
		this.expList = expList;
	}
}