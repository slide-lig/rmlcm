package com.rapidminer.lcm.io;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is offered for collecting result of PLCM calculate.
 * 
 * @author John624
 * 
 */
public class RMCollector implements PatternsCollector {

	protected long collected = 0;
	protected long collectedLength = 0;
	private int[] table;

	// private SupportPatternObject spobj;
	// private SupportPatternObject spRMobj = new SupportPatternObject(null, new
	// int [0]);

	private ArrayList<int[]> res = new ArrayList<int[]>();

	@Override
	synchronized public void collect(final int support, final int[] pattern) {
		// System.out.println(Integer.toString(support) + "\t"
		// + Arrays.toString(pattern));
		// spobj = new SupportPatternObject(support, Arrays.toString(pattern));
		// res.add(spobj);
		table = this.createTransactionLine(support, pattern);
			res.add(table);
		this.collected++;
		this.collectedLength += pattern.length;
	}

	@Override
	public long close() {
		return this.collected;
	}

	@Override
	public int getAveragePatternLength() {
		if (this.collected == 0) {
			return 0;
		} else {
			return (int) (this.collectedLength / this.collected);
		}
	}

	@Override
	public ArrayList<int[]> getResultList() {
		return this.res;
	}

	/**
	 * Link the support and the pattern corresponding array to a new array
	 * 
	 * @param support
	 * @param pattern
	 * @return
	 */
	public int[] createTransactionLine(final int support, final int[] pattern) {
		// System.out.println("test support is "+support);
		int[] table = new int[pattern.length + 2];
		table[0] = support;
		for (int i = 1; i < table.length - 1; i++) {
			table[i] = pattern[i - 1];
		}
		return table;
	}

	public void setRes(ArrayList<int[]> res) {
		this.res = res;
	}
}
