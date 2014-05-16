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

package com.rapidminer.lcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.internals.ExplorationStep;
import com.rapidminer.lcm.internals.transactions.RMTransactions;
import com.rapidminer.lcm.io.MultiThreadedFileCollector;
import com.rapidminer.lcm.io.NullCollector;
import com.rapidminer.lcm.io.PatternSortCollector;
import com.rapidminer.lcm.io.PatternsCollector;
import com.rapidminer.lcm.io.RPCollector;
import com.rapidminer.lcm.io.StdOutCollector;
import com.rapidminer.lcm.obj.ExecuteInformationsIOObject;
import com.rapidminer.lcm.obj.SupportPatternObject;
import com.rapidminer.lcm.util.MemoryPeakWatcherThread;
import com.rapidminer.lcm.util.ProgressWatcherThread;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.Ontology;

/**
 * LCM implementation, based on UnoAUA04 :
 * "An Efficient Algorithm for Enumerating Closed Patterns in Transaction Databases"
 * by Takeaki Uno el. al.
 */
public class PLCM {
	final List<PLCMThread> threads;
	private ProgressWatcherThread progressWatch;
	protected static long chrono;

	private final PatternsCollector collector;

	private final long[] globalCounters;

	public static ConcurrentHashMap<Integer, String> RMres = new ConcurrentHashMap<Integer, String>();;

	// private static boolean startMemoryWatch;

	public PLCM(PatternsCollector patternsCollector, int nbThreads) {
		if (nbThreads < 1) {
			throw new IllegalArgumentException(
					"nbThreads has to be > 0, given " + nbThreads);
		}
		this.collector = patternsCollector;
		this.threads = new ArrayList<PLCMThread>(nbThreads);
		this.createThreads(nbThreads);
		this.globalCounters = new long[PLCMCounters.values().length];
		this.progressWatch = new ProgressWatcherThread();
		// this.startMemoryWatch=startMemoryWatch;
	}

	void createThreads(int nbThreads) {
		for (int i = 0; i < nbThreads; i++) {
			this.threads.add(new PLCMThread(i));
		}
	}

	public final void collect(int support, int[] pattern) {
		this.collector.collect(support, pattern);
	}

	void initializeAndStartThreads(final ExplorationStep initState) {
		for (PLCMThread t : this.threads) {
			t.init(initState);
			t.start();
		}
	}

	/**
	 * Initial invocation
	 */
	public final void lcm(final ExplorationStep initState) {
		if (initState.pattern.length > 0) {
			this.collector.collect(initState.counters.transactionsCount,
					initState.pattern);
		}

		this.initializeAndStartThreads(initState);

		this.progressWatch.setInitState(initState);
		this.progressWatch.start();

		for (PLCMThread t : this.threads) {
			try {
				t.join();
				for (int i = 0; i < t.counters.length; i++) {
					this.globalCounters[i] += t.counters[i];
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		this.progressWatch.interrupt();
	}

	public Map<PLCMCounters, Long> getCounters() {
		HashMap<PLCMCounters, Long> map = new HashMap<PLCMCounters, Long>();

		PLCMCounters[] counters = PLCMCounters.values();

		for (int i = 0; i < this.globalCounters.length; i++) {
			map.put(counters[i], this.globalCounters[i]);
		}

		return map;
	}

	public String toString(Map<String, Long> additionalCounters) {
		StringBuilder builder = new StringBuilder();

		builder.append("{\"name\":\"PLCM\", \"threads\":");
		builder.append(this.threads.size());

		PLCMCounters[] counters = PLCMCounters.values();

		for (int i = 0; i < this.globalCounters.length; i++) {
			PLCMCounters counter = counters[i];

			builder.append(", \"");
			builder.append("\n");
			builder.append(counter.toString());
			builder.append("\":");
			builder.append(this.globalCounters[i]);
		}

		if (additionalCounters != null) {
			for (Entry<String, Long> entry : additionalCounters.entrySet()) {
				builder.append(", \"");
				builder.append("\n");
				builder.append(entry.getKey());
				builder.append("\":");
				builder.append(entry.getValue());
			}
		}

		builder.append('}');

		return builder.toString();
	}

	@Override
	public String toString() {
		return this.toString(null);
	}

	ExplorationStep stealJob(PLCMThread thief) {
		// here we need to readlock because the owner thread can write
		for (PLCMThread victim : this.threads) {
			if (victim != thief) {
				ExplorationStep e = stealJob(thief, victim);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}

	static ExplorationStep stealJob(PLCMThread thief, PLCMThread victim) {
		victim.lock.readLock().lock();
		for (int stealPos = 0; stealPos < victim.stackedJobs.size(); stealPos++) {
			ExplorationStep sj = victim.stackedJobs.get(stealPos);
			ExplorationStep next = sj.next();

			if (next != null) {
				thief.init(sj);
				victim.lock.readLock().unlock();
				return next;
			}
		}
		victim.lock.readLock().unlock();
		return null;
	}

	/**
	 * Some classes in EnumerationStep may declare counters here. see references
	 * to PLCMCounters.counters
	 */
	public enum PLCMCounters {
		ExplorationStepInstances, ExplorationStepCatchedWrongFirstParents, FirstParentTestRejections, TransactionsCompressions
	}

	public class PLCMThread extends Thread {
		public final long[] counters;
		final ReadWriteLock lock;
		final List<ExplorationStep> stackedJobs;
		protected final int id;

		public PLCMThread(final int id) {
			super("PLCMThread" + id);
			this.stackedJobs = new ArrayList<ExplorationStep>();
			this.id = id;
			this.lock = new ReentrantReadWriteLock();
			this.counters = new long[PLCMCounters.values().length];
		}

		void init(ExplorationStep initState) {
			this.lock.writeLock().lock();
			this.stackedJobs.add(initState);
			this.lock.writeLock().unlock();
		}

		@Override
		public long getId() {
			return this.id;
		}

		@Override
		public void run() {
			// no need to readlock, this thread is the only one that can do
			// writes
			boolean exit = false;
			while (!exit) {
				ExplorationStep sj = null;
				if (!this.stackedJobs.isEmpty()) {
					sj = this.stackedJobs.get(this.stackedJobs.size() - 1);

					ExplorationStep extended = sj.next();
					// iterator is finished, remove it from the stack
					if (extended == null) {
						this.lock.writeLock().lock();

						this.stackedJobs.remove(this.stackedJobs.size() - 1);
						this.counters[PLCMCounters.ExplorationStepInstances
								.ordinal()]++;
						this.counters[PLCMCounters.ExplorationStepCatchedWrongFirstParents
								.ordinal()] += sj
								.getCatchedWrongFirstParentCount();

						this.lock.writeLock().unlock();
					} else {
						this.lcm(extended);
					}

				} else { // our list was empty, we should steal from another
							// thread

					ExplorationStep stolj = stealJob(this);

					if (stolj == null) {
						exit = true;
					} else {
						lcm(stolj);
					}
				}
			}
		}

		private void lcm(ExplorationStep state) {
			collect(state.counters.transactionsCount, state.pattern);

			this.lock.writeLock().lock();
			this.stackedJobs.add(state);
			this.lock.writeLock().unlock();

		}
	}

	public static void printMan(Options options) {
		String syntax = "java fr.liglab.mining.PLCM [OPTIONS] INPUT_PATH MINSUP [OUTPUT_PATH]";
		String header = "\nIf OUTPUT_PATH is missing, patterns are printed to standard output.\nOptions are :";
		String footer = "\nFor advanced tuning you may also set properties : "
				+ ExplorationStep.KEY_LONG_TRANSACTIONS_THRESHOLD + ", "
				+ ExplorationStep.KEY_VIEW_SUPPORT_THRESHOLD;

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(80, syntax, header, options, footer);
	}

	/**
	 * It should be 3 arguments for the execution command line 1. input path 2.
	 * support 3. output path
	 * 
	 * but I replaced the input path as a exampleSet which was read already by a
	 * Operator of rapidminer so I add the third parameter 'ExampleSet
	 * exampleSet'
	 * 
	 * @param output
	 * */
	// public static void standalone(CommandLine cmd, RMTransactions dataSet,
	public static void standalone(String support, String fileLocation,
			RMTransactions dataSet, PLCM miner, OutputPort output,
			OutputPort consoleOutpout, boolean showThreadNb,
			boolean startMemoryWatch, boolean verboseMode,
			boolean ultraVerboseMode) {
		// String[] args = cmd.getArgs();
		int nbThreads = 0;

		// int minsup = Integer.parseInt(args[1]);
		int minsup = Integer.parseInt(support);

		MemoryPeakWatcherThread memoryWatch = null;

		// String outputPath = null;
		// if (args.length >= 3) {
		// outputPath = args[2];
		// }
		// String outputPath = fileLocation;

		if (startMemoryWatch) {
			memoryWatch = new MemoryPeakWatcherThread();
			memoryWatch.start();
		}

		chrono = System.currentTimeMillis();
		// TODO
		/*
		 * Change new ExplorationStep(minsup, args[0])-> new
		 * ExplorationStep(minsup, exampleset) *
		 */
		// ExplorationStep initState = new ExplorationStep(minsup, args[0]);

		// OK
		// for (ArrayList<String> ts : dataSet.getTransactions()) {
		// for (String string : ts) {
		// System.out.println(string+"                   test");
		// }
		// }
		//

		ExplorationStep initState = new ExplorationStep(minsup, dataSet);

		long loadingTime = System.currentTimeMillis() - chrono;
		System.err.println("Dataset loaded in " + loadingTime + "ms");

		// PatternsCollector collector = new RPCollector(); // TODO new
		// PatternsCollector for RapidMiner
		// utiliser initState.counters.getReverseRenaming()

		// PatternsCollector collector = new StdOutCollector();
		// initState.counters.getReverseRenaming();
		// PLCM miner = new PLCM(collector, 1);
		if (ultraVerboseMode) {
			ExplorationStep.verbose = true;
			ExplorationStep.ultraVerbose = true;
		} else if (verboseMode) {
			ExplorationStep.verbose = true;
		}

		// PLCM miner = new PLCM(collector, nbThreads);

		chrono = System.currentTimeMillis();
		miner.lcm(initState);
		chrono = System.currentTimeMillis() - chrono;

		Map<String, Long> additionalCounters = new HashMap<String, Long>();
		additionalCounters.put("miningTime", chrono);
		additionalCounters.put("outputtedPatterns", miner.collector.close());

		additionalCounters.put("loadingTime", loadingTime);
		additionalCounters.put("avgPatternLength",
				(long) miner.collector.getAveragePatternLength());

		if (memoryWatch != null) {
			memoryWatch.interrupt();
			additionalCounters.put("maxUsedMemory",
					memoryWatch.getMaxUsedMemory());
		}

		// (RPCollector)collector
		System.err.println(miner.toString(additionalCounters));

		// PatternsCollector pc=miner.collector;

		resSubConsole(nbThreads, miner.toString(additionalCounters),
				consoleOutpout);

		// RPCollector rp = (RPCollector) miner.collector;
		// rp.showResultView(output);
		resConsole(miner, output);
	}

	public static void resSubConsole(Integer nbThreads, String info,
			OutputPort consoleOutput) {
		ArrayList<String> verboseConsoles = null;

		ExecuteInformationsIOObject executeInfo = collectExecuteInformations(
				nbThreads, info, verboseConsoles);

		consoleOutput.deliver(executeInfo);
		// collectExecuteInformations(null, null);
	}

	public static void resConsole(PLCM miner, OutputPort output) {

		if (miner.collector instanceof RPCollector) {
			PatternsCollector rpc;
			rpc = (RPCollector) miner.collector;
		}
		
		Attribute[] newAttributes = new Attribute[2];

		newAttributes[0] = AttributeFactory.createAttribute("support",
				Ontology.INTEGER);
		newAttributes[1] = AttributeFactory.createAttribute("Pattern",
				Ontology.STRING);

		MemoryExampleTable table = new MemoryExampleTable(newAttributes);

		DataRowFactory ROW_FACTORY = new DataRowFactory(0, '.');
		// DataRowFactory row = new DataRowFactory(type, decimalPointCharacter)

		String[] patterns = new String[2];
		for (SupportPatternObject knowing : ((RPCollector) miner.collector).getRes()) {
			// for (Entry<Integer, String> knowing : RMres.entrySet()) {
			patterns[0] = knowing.getSupport().toString();
			patterns[1] = knowing.getPattern();
			DataRow row = ROW_FACTORY.create(patterns, newAttributes);
			table.addDataRow(row);
		}

		ExampleSet resultExampleSet = table.createExampleSet();

		output.deliver(resultExampleSet);
		// return rpc.getRMResult();
	}

	public static ExecuteInformationsIOObject collectExecuteInformations(
			Integer nbThreads, String info, ArrayList<String> verboseConsoles) {
		return new ExecuteInformationsIOObject(nbThreads, info, verboseConsoles);
	}

	/**
	 * Parse command-line arguments to instantiate the right collector
	 * 
	 * @param nbThreads
	 */

	// changed private -> public
	private static PatternsCollector instanciateCollector(CommandLine cmd,
			String outputPath, int nbThreads) {

		PatternsCollector collector = null;

		if (cmd.hasOption('b')) {
			collector = new NullCollector();
		} else {
			if (outputPath != null) {
				try {
					collector = new MultiThreadedFileCollector(outputPath,
							nbThreads);
				} catch (IOException e) {
					e.printStackTrace(System.err);
					System.err.println("Aborting mining.");
					System.exit(1);
				}
			} else {
				collector = new StdOutCollector();
			}

			if (cmd.hasOption('s')) {
				collector = new PatternSortCollector(collector);
			}
		}

		return collector;
	}
}
