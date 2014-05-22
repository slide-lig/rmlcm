package com.rapidminer.lcm.internals.transactions;

import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.operator.learner.associations.gsp.Transaction;

public class RMTransaction extends ResultObjectAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8324221398050250166L;

	private String[] line;
	private ArrayList<String> transaction;

	public RMTransaction(String[] line) {
		this.line = line;
		this.transaction = new ArrayList<String>();
		for (int i = 0; i < line.length; i++) {
			if (isInteger(line[i]))
				this.transaction.add(line[i]);
		}
	}

	public RMTransaction(ArrayList<String> list) {
		this.transaction = list;
	}

	public boolean isInteger(String value) {
		if (value == null || value.trim().equals("")) {
			return false;
		} else {
			boolean isInt = value.trim().matches("^\\d+$");
			return isInt;
		}
	}

	public int size() {
		return transaction.size();
	}

	public ArrayList<String> getTransaction() {
		return transaction;
	}

	public RMTransaction remove(int index) {
		transaction.remove(index);
		return new RMTransaction(transaction);
	}

	public void setTransaction(ArrayList<String> transaction) {
		this.transaction = transaction;
	}

	public void add(String string) {
		transaction.add(string);
	}

	public void clear() {
		transaction.clear();
	}
}
