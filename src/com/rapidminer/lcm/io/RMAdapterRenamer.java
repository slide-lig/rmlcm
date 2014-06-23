package com.rapidminer.lcm.io;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.rapidminer.lcm.internals.TransactionReader;
import com.rapidminer.lcm.internals.transactions.RMTransactions;

public class RMAdapterRenamer  implements Iterator<TransactionReader> {

	private ArrayList<Integer> filteredTransaction;
	private ArrayListReader transactionReader = new ArrayListReader();
	private Iterator<TIntArrayList> transactions;
	private int[] renaming;
	
	public RMAdapterRenamer(RMTransactions dataSet, int[] renaming) {
		transactions = dataSet.getTransactions().iterator();
		this.renaming = renaming;
		findNext();
	}

	@Override
	public boolean hasNext() {
		return filteredTransaction != null;
	}
	
	private void findNext() {
		if (transactions.hasNext()) {
			filteredTransaction = new ArrayList<Integer>();
			TIntIterator items = transactions.next().iterator();
			while (items.hasNext()) {
				int item = renaming[items.next()];
				if (item >= 0) {
					filteredTransaction.add(item);
				}
			}
			Collections.sort(filteredTransaction);
		} else {
			filteredTransaction = null;
		}
	}

	@Override
	public TransactionReader next() {
		ArrayList<Integer> next = this.filteredTransaction;
		findNext();
		transactionReader.recycle(next.iterator());
		return transactionReader;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
	private static final class ArrayListReader implements TransactionReader {
		
		private Iterator<Integer> iterator;

		public void recycle(Iterator<Integer> iterator) {
			this.iterator = iterator;
		}

		@Override
		public int getTransactionSupport() {
			return 1;
		}

		@Override
		public int next() {
			return this.iterator.next();
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}
		
	}
}
