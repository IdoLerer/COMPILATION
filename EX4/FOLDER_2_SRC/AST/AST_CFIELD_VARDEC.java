package AST;

import TYPES.*;
import UTILS.Context;
import SYMBOL_TABLE.*;
import TEMP.TEMP;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import IR.*;
import TEMP.TEMP_FACTORY;

public class AST_CFIELD_VARDEC extends AST_CFIELD {
	public AST_DEC_VARDEC vardec;

	public AST_CFIELD_VARDEC(AST_DEC_VARDEC vardec) {
		this.vardec = vardec;
		SerialNumber = AST_Node_Serial_Number.getFresh();
		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== CFIELD -> VARDEC\n");
	}
	
	public void setDefaultValue(TEMP instanceAddr) {
		TEMP value = TEMP_FACTORY.getInstance().getFreshTEMP();
		IR.getInstance().Add_IRcommand(new IRcommand_Move(value, vardec.IRme()));
		List<String> fieldList = new ArrayList<>(Context.classFields.get(Context.currentClassBuilder));
		int fieldNumber = fieldList.indexOf(vardec.uniqueId);
		IR.getInstance().Add_IRcommand(new IRcommand_Field_Set(instanceAddr, fieldNumber, value));
	}

	public TYPE_CLASS_VAR_DEC SemantMe() {
		if (!(this.vardec instanceof AST_DEC_VARDEC_OLD)) {
			OutputFileWriter.writeError(this.lineNumber,
					"1A declared data member inside a class can be initialized only with a constant value");
		}
		AST_DEC_VARDEC_OLD old_vardec = (AST_DEC_VARDEC_OLD) this.vardec;
		if (old_vardec.exp != null) {
			if (!((old_vardec.exp instanceof AST_EXP_INT) || (old_vardec.exp instanceof AST_EXP_STRING)
					|| (old_vardec.exp instanceof AST_EXP_NIL) || (old_vardec.exp instanceof AST_EXP_MINUS))) {
				OutputFileWriter.writeError(this.lineNumber,
						"2A declared data member inside a class can be initialized only with a constant value");
			}
		}
		TYPE t = vardec.SemantMe();
		TYPE_CLASS_VAR_DEC t2 = new TYPE_CLASS_VAR_DEC(t, vardec.name);
		t2.uniqueId = vardec.uniqueId;
		return t2;
	}

	/******************************************************/
	/* The printing message for a statement list AST node */
	/******************************************************/
	public void PrintMe() {
		/**************************************/
		/* AST NODE TYPE = AST CFIELD_VARDEC */
		/**************************************/
		System.out.print("AST NODE CFIELD VAR DEC\n");

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (vardec != null)
			vardec.PrintMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AST_GRAPHVIZ.getInstance().logNode(SerialNumber, "CFIELD\nVAR DEC\n");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (vardec != null)
			AST_GRAPHVIZ.getInstance().logEdge(SerialNumber, vardec.SerialNumber);
	}
	
	public TEMP IRme() {
		String myClass = Context.currentClassBuilder;
		LinkedHashSet<String> fields = Context.classFields.get(myClass);
		fields.add(vardec.uniqueId);
		return null;
	}
}
