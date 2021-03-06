/***********/
/* PACKAGE */
/***********/
package SYMBOL_TABLE;

/*******************/
/* GENERAL IMPORTS */
/*******************/

import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import TYPES.*;

/****************/
/* SYMBOL TABLE */
/****************/
public class SYMBOL_TABLE {
	private int hashArraySize = 13;

	/**********************************************/
	/* The actual symbol table data structure ... */
	/**********************************************/
	private SYMBOL_TABLE_ENTRY[] table = new SYMBOL_TABLE_ENTRY[hashArraySize];
	private SYMBOL_TABLE_ENTRY top;
	private int top_index = 0;

	/**************************************************************/
	/* A very primitive hash function for exposition purposes ... */
	/**************************************************************/
	private int hash(String s) {
		return Math.abs(s.hashCode()) % 13;
	}
	
	public String enter(String name, TYPE t) {
		return enter(name, t, null);
	}

	/****************************************************************************/
	/* Enter a variable, function, class type or array type to the symbol table */
	/****************************************************************************/
	public String enter(String name, TYPE t, String uniqueId) {
		System.out.println("~~entering into table: " + name);
		/*************************************************/
		/* [1] Compute the hash value for this new entry */
		/*************************************************/
		int hashValue = hash(name);

		/******************************************************************************/
		/* [2] Extract what will eventually be the next entry in the hashed position */
		/* NOTE: this entry can very well be null, but the behaviour is identical */
		/******************************************************************************/
		SYMBOL_TABLE_ENTRY next = table[hashValue];

		/**************************************************************************/
		/* [3] Prepare a new symbol table entry with name, type, next and prevtop */
		/**************************************************************************/
		SYMBOL_TABLE_ENTRY e = new SYMBOL_TABLE_ENTRY(name, t, hashValue, next, top, top_index++, uniqueId);

		/**********************************************/
		/* [4] Update the top of the symbol table ... */
		/**********************************************/
		top = e;

		/****************************************/
		/* [5] Enter the new entry to the table */
		/****************************************/
		table[hashValue] = e;

		/**************************/
		/* [6] Print Symbol Table */
		/**************************/
		PrintMe();
		
		return e.getUniqueId();
	}

	/***********************************************/
	/* Find the inner-most scope element with name */
	/***********************************************/
	public TYPE find(String name) {
		SYMBOL_TABLE_ENTRY e;

		for (e = table[hash(name)]; e != null; e = e.next) {
			if (name.equals(e.name)) {
				return e.type;
			}
		}

		return null;
	}

	public String getUniqueId(String name) {
		SYMBOL_TABLE_ENTRY e = this.top;
		int counter = 0;
		while (e != null) {
			if (e.name.equals(name)) {
				return e.getUniqueId();
			}
			// if reached class scope, check if name is a field of the father class:
			if (e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES) {
				TYPE_FOR_SCOPE_BOUNDARIES eScope = (TYPE_FOR_SCOPE_BOUNDARIES) e.type;
				if (eScope.scopeType == ScopeType.CLASS_SCOPE) {
					TYPE_CLASS classEntry = (TYPE_CLASS) classFind(eScope.name);
					TYPE_CLASS_VAR_DEC classVar = classEntry.getOverriddenDataMemember(name);
					if (classVar != null) {
						return classVar.uniqueId;
					}
				}
			}

			e = e.prevtop;
		}
		return null;
	}
	
	/***********************************************/
	/* Find the inner-most class element with name */
	/***********************************************/
	public TYPE classFind(String name) {
		SYMBOL_TABLE_ENTRY e;

		for (e = table[hash(name)]; e != null; e = e.next) {
			if (name.equals(e.name) && e.type.isClass()) {
				return e.type;
			}
		}

		return null;
	}

	/***********************************************/
	/*
	 * For finding correct variables: Find the correct TYPE of wanted var, checks
	 * also in father class fields.
	 */
	/***********************************************/
	public TYPE varFind(String name) {
		SYMBOL_TABLE_ENTRY e = this.top;

		while (e != null) {
			if (e.name.equals(name)) {
				return e.type;
			}
			// if reached class scope, check if name is a field of the father class:
			if (e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES) {
				TYPE_FOR_SCOPE_BOUNDARIES eScope = (TYPE_FOR_SCOPE_BOUNDARIES) e.type;
				if (eScope.scopeType == ScopeType.CLASS_SCOPE) {
					TYPE_CLASS classEntry = (TYPE_CLASS) classFind(eScope.name);
					TYPE_CLASS_VAR_DEC classVar = classEntry.getOverriddenDataMemember(name);
					if (classVar != null) {
						return classVar.t;
					}
				}
			}

			e = e.prevtop;
		}

		return null;
	}

	/***********************************************/
	/* Whether an element with this name exists in current scope */
	/***********************************************/
	public boolean isInScope(String name) {
		SYMBOL_TABLE_ENTRY e = top;

		while (e != null && !(e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES)) {
			if (e.name.equals(name)) {
				return true;
			}
			e = e.prevtop;
		}

		return false;
	}

	/***********************************************/
	/*
	 * Whether an element with this name exists as primitive type outside all scopes
	 */
	/***********************************************/
	public boolean isNameOutsideScopes(String name) {
		SYMBOL_TABLE_ENTRY e;// = find("SCOPE-BOUNDARY");

		// reach Global Scope
		for (e = table[hash("SCOPE-BOUNDARY")]; e != null; e = e.next) {
			if (e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES) {
				TYPE_FOR_SCOPE_BOUNDARIES scope = (TYPE_FOR_SCOPE_BOUNDARIES) e.type;
				if (scope.scopeType == ScopeType.GLOBAL_SCOPE)
					break;
			}
		}
		// search through all declaired before global scope
		while (e != null) {
			if (e.name.equals(name)) {
				return true;
			}
			e = e.prevtop;
		}

		return false;
	}

	/***********************************************/
	/* Get the type of current scope */
	/***********************************************/
	public ScopeType getCurrentScopeType() {
		SYMBOL_TABLE_ENTRY e = top;
		TYPE_FOR_SCOPE_BOUNDARIES scope;

		while (e.name != "SCOPE-BOUNDARY") { // instanceof TYPE_FOR_SCOPE_BOUNDARIES)) {
			e = e.prevtop;
		}

		if (e != null && e.name == "SCOPE-BOUNDARY") { // e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES) {
			scope = (TYPE_FOR_SCOPE_BOUNDARIES) e.type;
			return scope.scopeType;
		}
		return ScopeType.ERROR_SCOPE;
	}

	/*****************************************************************/
	/* Get the latest scope in stack of the given scopeType parameter */
	/*****************************************************************/
	public TYPE_FOR_SCOPE_BOUNDARIES getLastScopeOfType(ScopeType scopeType) {

		SYMBOL_TABLE_ENTRY e = top;

		for (; e != null; e = e.prevtop) {
			if (e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES)
				if (((TYPE_FOR_SCOPE_BOUNDARIES) e.type).scopeType == scopeType)
					return (TYPE_FOR_SCOPE_BOUNDARIES) e.type;
		}

		return null;
	}

	public TYPE_FUNCTION getLastFunctionWithName(String name) {

		SYMBOL_TABLE_ENTRY e = top;

		for (; e != null; e = e.prevtop) {
			if (e.type instanceof TYPE_FUNCTION)
				if (name.equals(((TYPE_FUNCTION)e.type).name))
					return (TYPE_FUNCTION) e.type;
		}

		return null;
	}

	/***************************************************************************/
	/* begine scope = Enter the <SCOPE-BOUNDARY> element to the data structure */
	/***************************************************************************/
	public void beginScope(ScopeType scopeType) {
		/************************************************************************/
		/* Though <SCOPE-BOUNDARY> entries are present inside the symbol table, */
		/* they are not really types. In order to be able to debug print them, */
		/* a special TYPE_FOR_SCOPE_BOUNDARIES was developed for them. This */
		/* class only contain their type name which is the bottom sign: _|_ */
		/************************************************************************/
		enter("SCOPE-BOUNDARY", new TYPE_FOR_SCOPE_BOUNDARIES(scopeType));

		/*********************************************/
		/* Print the symbol table after every change */
		/*********************************************/
		PrintMe();
	}

	// for class scopes
	public void beginScope(ScopeType scopeType, String name) {

		enter("SCOPE-BOUNDARY", new TYPE_FOR_SCOPE_BOUNDARIES(scopeType, name));

		/*********************************************/
		/* Print the symbol table after every change */
		/*********************************************/
		PrintMe();
	}

	// for function scopes
	public void beginScope(ScopeType scopeType, String name, TYPE returnType) {

		enter("SCOPE-BOUNDARY", new TYPE_FOR_SCOPE_BOUNDARIES(scopeType, name, returnType));

		/*********************************************/
		/* Print the symbol table after every change */
		/*********************************************/
		PrintMe();
	}

	/********************************************************************************/
	/* end scope = Keep popping elements out of the data structure, */
	/*
	 * from most recent element entered, until a <NEW-SCOPE> element is encountered
	 */
	/********************************************************************************/
	public void endScope() {
		/**************************************************************************/
		/* Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit */
		/**************************************************************************/
		System.out.println("**Now ending scope and Popping**");
		while (top.name != "SCOPE-BOUNDARY") {
			table[top.index] = top.next;
			top_index = top_index - 1;
			System.out.println("now popping: " + top.name);
			top = top.prevtop;
		}
		/**************************************/
		/* Pop the SCOPE-BOUNDARY sign itself */
		/**************************************/
		System.out.println("now popping: " + top.name);
		table[top.index] = top.next;
		top_index = top_index - 1;
		top = top.prevtop;

		/*********************************************/
		/* Print the symbol table after every change */
		/*********************************************/
		PrintMe();
	}

	public static int n = 0;

	public void PrintMe() {
		int i = 0;
		int j = 0;
		String dirname = "./FOLDER_5_OUTPUT/";
		String filename = String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt", n++);

		try {
			/*******************************************/
			/* [1] Open Graphviz text file for writing */
			/*******************************************/
			PrintWriter fileWriter = new PrintWriter(dirname + filename);

			/*********************************/
			/* [2] Write Graphviz dot prolog */
			/*********************************/
			fileWriter.print("digraph structs {\n");
			fileWriter.print("rankdir = LR\n");
			fileWriter.print("node [shape=record];\n");

			/*******************************/
			/* [3] Write Hash Table Itself */
			/*******************************/
			fileWriter.print("hashTable [label=\"");
			for (i = 0; i < hashArraySize - 1; i++) {
				fileWriter.format("<f%d>\n%d\n|", i, i);
			}
			fileWriter.format("<f%d>\n%d\n\"];\n", hashArraySize - 1, hashArraySize - 1);

			/****************************************************************************/
			/* [4] Loop over hash table array and print all linked lists per array cell */
			/****************************************************************************/
			for (i = 0; i < hashArraySize; i++) {
				if (table[i] != null) {
					/*****************************************************/
					/* [4a] Print hash table array[i] -> entry(i,0) edge */
					/*****************************************************/
					fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n", i, i);
				}
				j = 0;
				for (SYMBOL_TABLE_ENTRY it = table[i]; it != null; it = it.next) {
					/*******************************/
					/* [4b] Print entry(i,it) node */
					/*******************************/
					fileWriter.format("node_%d_%d ", i, j);
					fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n", it.name, it.type.name,
							it.prevtop_index);

					if (it.next != null) {
						/***************************************************/
						/* [4c] Print entry(i,it) -> entry(i,it.next) edge */
						/***************************************************/
						fileWriter.format("node_%d_%d -> node_%d_%d [style=invis,weight=10];\n", i, j, i, j + 1);
						fileWriter.format("node_%d_%d:f3 -> node_%d_%d:f0;\n", i, j, i, j + 1);
					}
					j++;
				}
			}
			fileWriter.print("}\n");
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static SYMBOL_TABLE instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected SYMBOL_TABLE() {
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static SYMBOL_TABLE getInstance() {
		if (instance == null) {
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new SYMBOL_TABLE();

			/*****************************************/
			/* [1] Enter primitive types int, string */
			/*****************************************/
			instance.enter("int", TYPE_INT.getInstance());
			instance.enter("string", TYPE_STRING.getInstance());
			instance.enter("nill", TYPE_NILL.getInstance());
			instance.enter("void", TYPE_VOID.getInstance());

			/*************************************/
			/* [2] How should we handle void ??? */
			/*************************************/

			/***************************************/
			/* [3] Enter library function PrintInt */
			/***************************************/
			instance.enter("PrintInt", new TYPE_FUNCTION(TYPE_VOID.getInstance(), "PrintInt",
					new TYPE_LIST(TYPE_INT.getInstance(), null)));
			instance.enter("PrintString", new TYPE_FUNCTION(TYPE_VOID.getInstance(), "PrintString",
					new TYPE_LIST(TYPE_STRING.getInstance(), null)));
			instance.enter("PrintTrace", new TYPE_FUNCTION(TYPE_VOID.getInstance(), "PrintTrace", null));

		}
		return instance;
	}
}
