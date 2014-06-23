package com.rapidminer.lcm.internals.transactions;

import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;

/**
 * Class for transaction in Rapidminer
 * 
 * @author John624
 * 
 */
public class RMStringTransaction extends ResultObjectAdapter {

	/**
	 * Save a transaction as a arrayList
	 */
	private static final long serialVersionUID = -8324221398050250166L;

	private ArrayList<String> transaction;

	public RMStringTransaction(String[] line) {
		// constructorid = 2;
		// this.line = line;
		transaction = new ArrayList<String>();
		for (int i = 0; i < line.length; i++) {
			transaction.add(line[i]);
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

	public ArrayList<String> getTransaction() {
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

	public void setTransaction(ArrayList<String> transaction) {
		this.transaction = transaction;
	}

	/**
	 * add a item in the transaction
	 * 
	 * @return
	 */
	public void add(String item) {
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
}
