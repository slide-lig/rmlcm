package com.rapidminer.lcm.io;

import java.util.ArrayList;
import java.util.Iterator;

import com.rapidminer.lcm.internals.TransactionReader;
import com.rapidminer.lcm.internals.transactions.RMTransactions;

public class RMAdapter implements Iterator<TransactionReader> {

	private RMTransactions dataSet;
	private Iterator<ArrayList<String>> itrTransactions;
	private int renaming[] = null;
	private LineReader lineReader = new LineReader();

	public RMAdapter(RMTransactions dataSet) {
		this.dataSet = dataSet;
		itrTransactions = dataSet.getTransactions().iterator();
	}

	public void setRenaming(int[] renaming) {
		this.renaming = renaming;
	}

	public void reset() {
		itrTransactions = dataSet.getTransactions().iterator();
	}

	@Override
	public boolean hasNext() {
		return itrTransactions.hasNext();
	}

	@Override
	public TransactionReader next() {
		ArrayList<String> transaction = itrTransactions.next();
		lineReader.reset(transaction);
		return lineReader;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void close() {
		setRenaming(null);
	}

	private class LineReader implements TransactionReader {

		private ArrayList<String> transaction;
		private Iterator<String> itrTransaction;
		private Integer next_value;

		public LineReader() {
		}

		public void reset(ArrayList<String> t) {
			this.transaction = t;
			itrTransaction = transaction.iterator();
			findNext();
		}

		@Override
		public int getTransactionSupport() {
			return 1;
		}

		@Override
		public int next() {
			int next = next_value;
			findNext();
			return next;
		}
		
		private void findNext(){
			next_value = null;
			while (itrTransaction.hasNext() && next_value == null){
				if (renaming == null) {
					next_value = Integer.valueOf(itrTransaction.next());
				} else {
					next_value = renaming[Integer.valueOf(itrTransaction.next())];
					if (next_value == -1){
						next_value=null;
					}
				}
			}
		}

		@Override
		public boolean hasNext() {
			return next_value != null;
		}
	}
}
