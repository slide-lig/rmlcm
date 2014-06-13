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

		private void findNext() {
			next_value = null;
			while (itrTransaction.hasNext() && next_value == null) {
				if (renaming == null) {
					try {
						next_value = itrTransaction.next();
					} catch (Exception e) {
						new AmbiguousSeparatorException();
						//new NumberFormatException();
					}
				} else {
					try {
						next_value = renaming[itrTransaction
								.next()];
						if (next_value == -1) {
							next_value = null;
						}
					} catch (Exception e) {
						new AmbiguousSeparatorException();
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
