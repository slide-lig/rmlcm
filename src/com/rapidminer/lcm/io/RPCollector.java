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

public class RPCollector implements PatternsCollector {

	protected long collected = 0;
	protected long collectedLength = 0;

	private OutputPort output;
	// private ConcurrentHashMap<Integer, String> RMres = new
	// ConcurrentHashMap<Integer, String>();
	private SupportPatternObject spobj;
	private ArrayList<SupportPatternObject> res = new ArrayList<SupportPatternObject>();

	@Override
	synchronized public void collect(final int support, final int[] pattern) {
		System.out.println(Integer.toString(support) + "\t"
				+ Arrays.toString(pattern));	
		spobj=new SupportPatternObject(support, Arrays.toString(pattern));
		res.add(spobj);
		// PLCM.RMres.put(support, Arrays.toString(pattern));
		// for (Entry<Integer, String> i : PLCM.RMres.entrySet()) {
		// System.out.println("------" + i.getValue() + "-------");
		// }
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

	public ArrayList<SupportPatternObject> getRes() {
		return res;
	}

	public void setRes(ArrayList<SupportPatternObject> res) {
		this.res = res;
	}

	// synchronized public Map<Integer, String> getRMResult() {
	// return RMres;
	// }

}
