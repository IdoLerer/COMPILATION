/***********/
/* PACKAGE */
/***********/
package IR;

import MIPS.sir_MIPS_a_lot;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class IR
{
	private IRcommand head=null;
	private IRcommandList tail=null;

	/******************/
	/* Add IR command */
	/******************/
	public void Add_IRcommand(IRcommand cmd)
	{
		if ((head == null) && (tail == null))
		{
			this.head = cmd;
		}
		else if ((head != null) && (tail == null))
		{
			this.tail = new IRcommandList(cmd,null);
		}
		else
		{
			IRcommandList it = tail;
			while ((it != null) && (it.tail != null))
			{
				it = it.tail;
			}
			it.tail = new IRcommandList(cmd,null);
		}
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	public void MIPSme()
	{
		if (head != null) head.MIPSme();
		if (tail != null) tail.MIPSme();
		sir_MIPS_a_lot.getInstance().abort();
		sir_MIPS_a_lot.getInstance().createAccessViolation();
		sir_MIPS_a_lot.getInstance().createDivisionByZero();
		sir_MIPS_a_lot.getInstance().createInvalidPtrDref();
		sir_MIPS_a_lot.getInstance().createAbort();
	}
	
	/***********************/
	/* Print for debugging */
	/***********************/
	public void printAll()
	{
		System.out.println("><><><><><><><><><><><><><><><><><><><><><><><");
		if (head != null) head.printMe();
		if (tail != null) tail.printMe();
		System.out.println("><><><><><><><><><><><><><><><><><><><><><><><");
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static IR instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected IR() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static IR getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new IR();
		}
		return instance;
	}
}
