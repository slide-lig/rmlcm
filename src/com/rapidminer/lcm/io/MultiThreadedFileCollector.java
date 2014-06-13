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

import java.io.IOException;
import java.util.ArrayList;

/**
 * A thread safe PatternsCollector that will write to multiple files, one per
 * mining thread.
 */
public class MultiThreadedFileCollector implements PatternsCollector {

	private final FileCollector[] collectors;

	//private SupportPatternObject spobj;
	//private ArrayList<SupportPatternObject> res = new ArrayList<SupportPatternObject>();
	private int [] table;
	private ArrayList<int []> res = new ArrayList<int []>();
	
	/**
	 * @param prefix
	 *            filename prefix for pattern files, each thread will append
	 *            [ThreadID].dat
	 * @param maxId
	 *            higer bound on thread's getId()
	 * @throws IOException
	 */
	public MultiThreadedFileCollector(final String prefix, final int maxId)
			throws IOException {
		this.collectors = new FileCollector[maxId];
		for (int i = 0; i < maxId; i++) {
			this.collectors[i] = new FileCollector(prefix + i + ".dat");
		}
	}

	@Override
	public void collect(int support, int[] pattern) {
		
//		spobj = new SupportPatternObject(support, Arrays.toString(pattern));
//		res.add(spobj);
		
		table = this.createTransactionLine(support, pattern);
		res.add(table);
		
		this.collectors[(int) Thread.currentThread().getId()].collect(support,
				pattern);
	}

	@Override
	public long close() {
		long total = 0;

		for (FileCollector collector : this.collectors) {
			total += collector.close();
		}

		return total;
	}

	@Override
	public int getAveragePatternLength() {
		long totalLen = 0;
		long nbPatterns = 0;

		for (FileCollector collector : this.collectors) {
			totalLen += collector.getCollectedLength();
			nbPatterns += collector.getCollected();
		}

		return (int) (totalLen / nbPatterns);
	}

	@Override
	public ArrayList<int[]> getResultList() {
		return res;
	}
	
	public int[] createTransactionLine(int support, int[] pattern) {
		int[] table = new int[pattern.length+2];
		table[0] = support;
		for (int i = 1; i < table.length - 1; i++) {
			table[i] = pattern[i - 1];
		}
		return table;
	}


}
