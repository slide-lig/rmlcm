package com.rapidminer.lcm.io;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIterator;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Iterator;

import com.rapidminer.lcm.exceptions.AmbiguousSeparatorException;
import com.rapidminer.lcm.internals.TransactionReader;
import com.rapidminer.lcm.internals.transactions.RMTransactions;

/**
 * This class offer methods of iterator to ExplorationStep
 * @author John624
 *
 */
public class RMAdapter implements Iterator<TransactionReader> {

	private RMTransactions dataSet;
	private Iterator<TIntArrayList> itrTransactions;
	//private TIterator itrTransactions;
	private int renaming[] = null;
	private LineReader lineReader = new LineReader();

	public RMAdapter(RMTransactions dataSet) {
		this.dataSet = dataSet;
		itrTransactions = dataSet.getTransactions().iterator();
		//itrTransactions = dataSet.getTransactions().iterator();
	}

	
	/**
	 * reset renaming table
	 * @param renaming
	 */
	public void setRenaming(int[] renaming) {
		this.renaming = renaming;
	}

	
	/**
	 * reset Iterator of transactions as init state
	 */
	public void reset() {
		itrTransactions = dataSet.getTransactions().iterator();
	}

	@Override
	public boolean hasNext() {
		return itrTransactions.hasNext();
	}

	@Override
	public TransactionReader next() {
		TIntArrayList transaction = itrTransactions.next();
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

		private TIntArrayList transaction;
		private TIntIterator itrTransaction;
		private Integer next_value;

		public LineReader() {
		}

		public void reset(TIntArrayList t) {
			this.transaction = t;
			itrTransaction = transaction.iterator();
			try {
				findNext();
			} catch (AmbiguousSeparatorException e) {
				e.errorDialog();
				e.printStackTrace();
			}
		}

		@Override
		public int getTransactionSupport() {
			return 1;
		}

		@Override
		public int next() {
			int next = next_value;
			try {
				findNext();
			} catch (AmbiguousSeparatorException e) {
				e.errorDialog();
				e.printStackTrace();
			}
			return next;
		}

		private void findNext() throws AmbiguousSeparatorException {
			next_value = null;
			while (itrTransaction.hasNext() && next_value == null) {
				if (renaming == null) {
					next_value = itrTransaction.next();
				} else {
					next_value = renaming[itrTransaction
							.next()];
					if (next_value == -1) {
						next_value = null;
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
