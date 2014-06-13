package com.rapidminer.lcm.internals.transactions;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;

/**
 * Class for all transactions
 * @author John624
 *
 */
public class RMTransactions extends ResultObjectAdapter {

	/**
	 * Save all transactions as a arrayList
	 */
	private static final long serialVersionUID = -6501697292978253573L;

	//private RMTransaction transaction;
	private ArrayList<TIntArrayList> transcations;

	public RMTransactions() {
		transcations = new ArrayList<TIntArrayList>();
	}

	/**
	 * add a transaction in transactions
	 * 
	 * @param transaction
	 */
	public void add(RMTransaction transaction) {
		transcations.add(transaction.getTransaction());
	}
	
	/**
	 * remove a transaction in transactions
	 * 
	 * @return transaction
	 */
	public void remove(int i){
		transcations.remove(i);
	}

	/** 
	 * @return all transactions as a list
	 */
	public ArrayList<TIntArrayList> getTransactions() {
		return transcations;
	}

	/** 
	 * show result in the result perspective
	 * @return content of transactions as string
	 */
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		for (TIntArrayList list : this.transcations) {
//			for (String string : list) {
//				out.append(string);
//				out.append("\n");
//			}
			for (int i = 0; i < list.size(); i++) {
				out.append(list.get(i));
				out.append("\n");
			}
		}
		return out.toString();
	}
}
