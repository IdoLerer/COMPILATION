package AST;

import TYPES.*;
import SYMBOL_TABLE.*;

public class AST_EXP_FUNC_CALL extends AST_EXP {
	public AST_VAR var;
	public String funcName;
	public AST_EXP_LIST expList;
	public AST_EXP_FUNC_CALL(AST_VAR var, String funcName,AST_EXP_LIST expList) {
		this.var = var;
		this.funcName = funcName;
		this.expList = expList;
		SerialNumber = AST_Node_Serial_Number.getFresh();
	}

	/******************************************************/
	/* The printing message for a statement list AST node */
	/******************************************************/
	public void PrintMe()
	{
		/**************************************/
		/* AST NODE TYPE = AST STATEMENT LIST */
		/**************************************/
		System.out.print("AST NODE EXP ID\n");

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (var != null) var.PrintMe();
		if (expList != null) expList.PrintMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AST_GRAPHVIZ.getInstance().logNode(
			SerialNumber,
			"EXP\nID\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,var.SerialNumber);
		if (expList != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,expList.SerialNumber);
	}

		public TYPE SemantMe() {
		TYPE varType = null;
		TYPE funcType = null;
		TYPE_LIST expTypeList = null;

		if (this.var != null)
			varType = this.var.SemantMe();
		if (this.expList != null)
			expTypeList = this.expList.SemantMe();

		if (this.var == null)
			// either in the same scope or in global scope
			funcType = SYMBOL_TABLE.getInstance().find(this.funcName);
		else
			// check if the function is declared in the type's class
			funcType = ((TYPE_CLASS) varType).getOveridedMethod(this.funcName);

		if (funcType == null)
			OutputFileWriter.writeError(this.lineNumber, String.format("function is not declared %s", funcName));

		if (!isFunctionCallValid((TYPE_FUNCTION) funcType, expTypeList))
			OutputFileWriter.writeError(this.lineNumber, String.format("function call is not valid %s %s", funcName));

		return ((TYPE_FUNCTION) funcType).returnType;
	}
}
