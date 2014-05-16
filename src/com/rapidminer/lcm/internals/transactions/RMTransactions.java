package com.rapidminer.lcm.internals.transactions;

import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;

public class RMTransactions extends ResultObjectAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6501697292978253573L;

	//private RMTransaction transaction;
	private ArrayList<ArrayList<String>> transcations;

	public RMTransactions() {
		transcations = new ArrayList<ArrayList<String>>();
		// transcations.add(transaction.getTransaction());
	}

	public void add(RMTransaction transaction) {
		transcations.add(transaction.getTransaction());
	}
	
	public void remove(int i){
		transcations.remove(i);
	}

	public ArrayList<ArrayList<String>> getTransactions() {
		return transcations;
	}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		for (ArrayList<String> list : this.transcations) {
			for (String string : list) {
				//System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
				//System.out.println(string);
				out.append(string);
				out.append("\n");
			}
		}
		return out.toString();
	}
}
