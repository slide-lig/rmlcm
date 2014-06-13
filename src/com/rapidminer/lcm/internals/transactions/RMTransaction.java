package com.rapidminer.lcm.internals.transactions;

import gnu.trove.list.array.TIntArrayList;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;

/**
 * Class for transaction in Rapidminer
 * 
 * @author John624
 * 
 */
public class RMTransaction extends ResultObjectAdapter {

	/**
	 * Save a transaction as a arrayList
	 */
	private static final long serialVersionUID = -8324221398050250166L;

	private int[] line;
	// private ArrayList<String> transaction;
	private TIntArrayList transaction;

	//private int constructorid;

	public RMTransaction(int[] line) {

		//constructorid = 1;

		this.line = line;
		this.transaction = new TIntArrayList();
		for (int i = 0; i < line.length; i++) {
			this.transaction.add(line[i]);
			// if (isInteger(line[i]))
			// try {
			// this.transaction.add(Integer.valueOf(line[i]));
			// } catch (Exception e) {
			// new NumberFormatException();
			// }
		}
	}

	public RMTransaction(TIntArrayList list) {
		
		//constructorid = 3;
		
		this.transaction = list;
	}

	/**
	 * verify a item in transaction is "Integer" or not
	 * 
	 * @param value
	 * @return
	 */
	public boolean isInteger(String value) {
		if (value == null || value.trim().equals("")) {
			return false;
		} else {
			boolean isInt = value.trim().matches("^\\d+$");
			return isInt;
		}
	}

	/**
	 * @return the number of items in a transaction
	 */
	public int size() {

		return transaction.size();
	}

	/**
	 * @return the current transaction
	 */

	public TIntArrayList getTransaction() {
		return transaction;
	}

	/**
	 * remove the 'index'th element in the transaction
	 * 
	 * @return
	 */
	public void remove(int index) {
		transaction.remove(index);
	}

	public void setTransaction(TIntArrayList transaction) {
		this.transaction = transaction;
	}

	/**
	 * add a item in the transaction
	 * 
	 * @return
	 */
	public void add(int item) {
		transaction.add(item);
	}

	/**
	 * reset the transaction
	 * 
	 * @return
	 */
	public void clear() {
		transaction.clear();
	}

	/**
	 * test which constructor is using
	 * 
	 * @return 1 if using RMTransaction(int[] line)
	 * @return 2 if using RMTransaction(String[] line)
	 */
	public int usingConstructor() {
		Constructor[] allConstructors = this.getClass().getConstructors();
		for (Constructor constructor : allConstructors) {
			Class<?>[] pType = constructor.getParameterTypes();
			for (int i = 0; i < pType.length; i++) {
				if (pType[i].equals(int[].class)) {
					return 1;
				} else {
					return 2;
				}
			}
		}
		return 1;
	}
}
