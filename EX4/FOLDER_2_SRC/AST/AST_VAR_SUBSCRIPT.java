package AST;

import TYPES.*;
import IR.IR;
import IR.IRcommand_Array_Access;
import IR.IRcommand_LoadLocalVar;
import SYMBOL_TABLE.*;
import TEMP.TEMP;
import TEMP.TEMP_FACTORY;

public class AST_VAR_SUBSCRIPT extends AST_VAR {
	public AST_VAR var;
	public AST_EXP subscript;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AST_VAR_SUBSCRIPT(AST_VAR var, AST_EXP subscript) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		SerialNumber = AST_Node_Serial_Number.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== var -> var [ exp ]\n");

		/*******************************/
		/* COPY INPUT DATA NENBERS ... */
		/*******************************/
		this.var = var;
		this.subscript = subscript;
	}

	public TYPE SemantMe() {
		TYPE varType = var.SemantMe();

		// Check that variable is an array
		if (!varType.isArray()) {
			OutputFileWriter.writeError(this.lineNumber, "Tried to acces subscript of non array variable\n");
		}

		if (subscript.SemantMe() != TYPE_INT.getInstance()) {
			OutputFileWriter.writeError(this.lineNumber, "Used non integer subscript on array\n");
		}
		this.type = ((TYPE_ARRAY) varType).arrayType;
		return this.type;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void PrintMe() {
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSRIPT ... */
		/****************************************/
		if (var != null)
			var.PrintMe();
		if (subscript != null)
			subscript.PrintMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AST_GRAPHVIZ.getInstance().logNode(SerialNumber, "SUBSCRIPT\nVAR\n...[...]");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null)
			AST_GRAPHVIZ.getInstance().logEdge(SerialNumber, var.SerialNumber);
		if (subscript != null)
			AST_GRAPHVIZ.getInstance().logEdge(SerialNumber, subscript.SerialNumber);
	}
	
	public TEMP IRme() {
		TEMP temp = TEMP_FACTORY.getInstance().getFreshTEMP();
		TEMP arrTemp = var.IRme();
		TEMP subTemp = subscript.IRme();
		IR.getInstance().Add_IRcommand(new IRcommand_Array_Access(temp,arrTemp, subTemp));
		return temp;
	}
}
