package AST;

import TYPES.*;
import UTILS.Context;
import SYMBOL_TABLE.*;
import TEMP.TEMP;
import IR.*;

public class AST_STMT_RETURN extends AST_STMT {
	public AST_EXP returnExp;

	public AST_STMT_RETURN(AST_EXP exp) {
		this.returnExp = exp;
		SerialNumber = AST_Node_Serial_Number.getFresh();
	}

	/******************************************************/
	/* The printing message for a statement list AST node */
	/******************************************************/
	public void PrintMe() {
		/**************************************/
		/* AST NODE TYPE = AST STATEMENT LIST */
		/**************************************/
		System.out.print("AST NODE STMT RETURN\n");

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (returnExp != null)
			returnExp.PrintMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AST_GRAPHVIZ.getInstance().logNode(SerialNumber, "STMT\nRETURN\n");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (returnExp != null)
			AST_GRAPHVIZ.getInstance().logEdge(SerialNumber, returnExp.SerialNumber);
	}

	public TYPE SemantMe() {
		TYPE_FOR_SCOPE_BOUNDARIES fatherFunc = SYMBOL_TABLE.getInstance().getLastScopeOfType(ScopeType.FUNCTION_SCOPE);

		//
		if (fatherFunc == null) {
			OutputFileWriter.writeError(this.lineNumber, "Return statemenet out of function scope\n");
		}
		
		// if function declared to return void, make sure there is no return value.
		if (fatherFunc.returnType == TYPE_VOID.getInstance()) {
			if (this.returnExp != null)
				OutputFileWriter.writeError(this.lineNumber, "Return statemenet has value, though declaired as void.\n");
			// then its ok
			else
				return null;
		}
		
		// if function declared to return non-void, then the return is not empty.
		if (fatherFunc.returnType != TYPE_VOID.getInstance() && this.returnExp == null) {
			OutputFileWriter.writeError(this.lineNumber, "Return statemenet is not void, though empty return statement.\n");
		}

		// check that actual return type matches or subclass of the functions declared return type
		TYPE returnExpType = this.returnExp.SemantMe();
		if (!returnExpType.equalsOrSubclass(fatherFunc.returnType)) {
			OutputFileWriter.writeError(this.lineNumber, String.format("Return type missmatch %s %s.\n", returnExpType.name, fatherFunc.returnType.name));
		}
		
		// tell father that he has a return statement in him.
		TYPE fatherFuncInstance = SYMBOL_TABLE.getInstance().getLastFunctionWithName(fatherFunc.name);
		((TYPE_FUNCTION)fatherFuncInstance).isReturnStatemntInside = true;

		// don't care about return value
		return null;

	}
	
	public TEMP IRme() {
		if (returnExp != null) {			
			TEMP tReturn = returnExp.IRme();
			IR.getInstance().Add_IRcommand(new IRcommand_StoreReturnValue(tReturn));
		}
		IR.getInstance().Add_IRcommand(new IRcommand_Jump_Label(Context.epilogueLabel));
		return null;
	}

}
