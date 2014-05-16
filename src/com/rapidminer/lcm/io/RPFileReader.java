package com.rapidminer.lcm.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.lcm.internals.TransactionReader;

public class RPFileReader implements Iterator<TransactionReader> {

	private ExampleSet dataSet;
	private Iterator<Example> itrTransactions;

	//private Map<Entry<String, Object>, Integer> knownItems = new HashMap<Map.Entry<String, Object>, Integer>();
	private int renaming[] = null;
	private LineReader lineReader;

	public RPFileReader(ExampleSet dataSet) {
		this.dataSet = dataSet;
		itrTransactions = dataSet.iterator();
		// System.out.println("dataSet.iterator() here is OK!");
	}

	public void reset() {
		this.itrTransactions = this.dataSet.iterator();
	}

	@Override
	public boolean hasNext() {
		return itrTransactions.hasNext();
	}

	@Override
	public TransactionReader next() {
		Example e = itrTransactions.next();
		// System.out.println(e.getAttributes().toString());
		//System.out.println("     tid   :"+ e.getId());
		lineReader = new LineReader(e);
		// lineReader = new LineReader(itrTransactions.next());
		// System.out.println("--------------------------------");
		// System.out.println(itrTransactions.next().toString());
		// System.out.println("linerReader:"+lineReader.next());
		// System.out.println("linerReader:"+lineReader.next());
		// System.out.println("--------------------------------");
		return lineReader;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void close() {
		setRenaming(null);
	}

	public void setRenaming(int[] renaming) {
		this.renaming = renaming;
	}

	public ExampleSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(ExampleSet dataSet) {
		this.dataSet = dataSet;
	}

	private final class LineReader implements TransactionReader {

		private Iterator<Integer> example;
		private ArrayList<Integer> items;
		private Integer next_value;

		public LineReader(Example example) {
			items = new ArrayList<Integer>();
			// Map<String, Object> map = new HashMap<String, Object>();
			int item = 0;
			// System.out.println("--------------" + example.getDataRow());

			// for (Attribute attribute : example.getAttributes()) {
			//
			// id = (int) example.getValue(attribute);
			// System.out.println("example.getDataRow()   "+example.getDataRow());
			// if (id > 0) {
			// items.add(id);
			// System.out.println(" value    " + id + "    cpt: " + cpt++);
			// }

			for (String key : example.keySet()) {

				if (!example.get(key).toString().equals("?")) {
					item = Integer.valueOf(example.get(key).toString());
					if (item > 0)
						items.add(item);
				}
			}

			// this.example = map.entrySet().iterator();
			this.example = items.iterator();
			// int max = 0;

			// for(int i=0;i< example.values().size();i++){

			// while (this.example.hasNext() && !example.isEmpty()) {
			// int nextValue =
			// Integer.valueOf(this.example.next().getValue().toString());
			// if ( nextValue > max)
			// max = nextValue;
			// System.out.println("max: " + max);
			// }

			// int max = Collections.max(items);
			// System.out.println("max :" + max);
			// renaming = new int[max];
			// Arrays.fill(renaming, -1);
			// this.example = example.entrySet().iterator();
		}

		@Override
		public int getTransactionSupport() {
			return 1;
		}

		@Override
		public int next() {
			// System.out.println("next_value"+next_value);
			if (renaming == null) {
				//System.out.println("test next value   " + next_value);
				return next_value;
			} else {
				// System.out.println("test array   " + renaming[next_value]);
				return renaming[next_value];
			}
		}

		@Override
		public boolean hasNext() {
			String tempString;
			Integer tempInt = null;
			if (!this.example.hasNext()) {
				//System.out.println("this.example.hasNext() == false");
				return false;
			} else {
				//System.out.println("this.example.hasNext() == true");
				tempString = this.example.next().toString();

				if (tempString.equals("?"))
					tempString = null;
				else {
					tempInt = Integer.valueOf(tempString);
				}
				if (tempString == null) {
					return false;
				} else {
					if (tempInt > 0)
						next_value = tempInt;
					return true;
				}
			}
		}
	}

}
