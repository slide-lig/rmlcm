package com.rapidminer.lcm.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.rapidminer.lcm.PLCM;
import com.rapidminer.lcm.obj.SupportPatternObject;
import com.rapidminer.operator.ports.OutputPort;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

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
		System.out.println(Integer.toString(support) + "\t"
				+ Arrays.toString(pattern));
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
	public ArrayList<int[]> getRes() {
		return this.res;
	}

	public int[] createTransactionLine(int support, int[] pattern) {
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
