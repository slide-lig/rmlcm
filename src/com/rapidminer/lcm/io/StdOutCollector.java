/*
	This file is part of jLCM
	
	Copyright 2013 Martin Kirchgessner, Vincent Leroy, Alexandre Termier, Sihem Amer-Yahia, Marie-Christine Rousset, Université Joseph Fourier and CNRS

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	 http://www.apache.org/licenses/LICENSE-2.0
	 
	or see the LICENSE.txt file joined with this program.

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

package com.rapidminer.lcm.io;

import java.util.ArrayList;
import java.util.Arrays;

public class StdOutCollector implements PatternsCollector {

	protected long collected = 0;
	protected long collectedLength = 0;
	private int[] table;
	private ArrayList<int[]> res = new ArrayList<int[]>();

	// private SupportPatternObject spobj;
	// private ArrayList<SupportPatternObject> res = new
	// ArrayList<SupportPatternObject>();

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

	public int test() {
		return 1;
	}

	@Override
	public ArrayList<int[]> getResultList() {
		return res;
	}

	public int[] createTransactionLine(int support, int[] pattern) {
		int[] table = new int[pattern.length + 2];
		table[0] = support;
		for (int i = 1; i < table.length - 1; i++) {
			table[i] = pattern[i - 1];
		}
		return table;
	}

}
