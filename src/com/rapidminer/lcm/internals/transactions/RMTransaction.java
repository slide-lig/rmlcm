package com.rapidminer.lcm.internals.transactions;

import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;

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

	public boolean isInteger(String value) {
		if (value == null || value.trim().equals("")) {
			return false;
		} else {
			boolean isInt = value.trim().matches("^\\d+$");
			return isInt;
		}
	}

	public int size(){
		return transaction.size();
	}
	
	public ArrayList<String> getTransaction() {
		return transaction;
	}

	public void setTransaction(ArrayList<String> transaction) {
		this.transaction = transaction;
	}
}
