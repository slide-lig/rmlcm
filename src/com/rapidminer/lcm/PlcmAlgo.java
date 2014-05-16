package com.rapidminer.lcm;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rapidminer.lcm.internals.transactions.RMTransactions;
import com.rapidminer.lcm.io.MultiThreadedFileCollector;
import com.rapidminer.lcm.io.NullCollector;
import com.rapidminer.lcm.io.PatternSortCollector;
import com.rapidminer.lcm.io.PatternsCollector;
import com.rapidminer.lcm.io.RPCollector;
import com.rapidminer.lcm.io.StdOutCollector;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;

public class PlcmAlgo extends Operator {

	// Attributes for port of rapidminer
	private InputPort input = this.getInputPorts().createPort("in");
	private OutputPort output = this.getOutputPorts().createPort("res");
	private OutputPort infoOutput = this.getOutputPorts().createPort("info");

	// private static final String executionCommand = "key";

	// private static final String operation = "operation";
	// private static final String dataset = "dataset";
	private static final String threshold = "Support";

	private static final String results = "Result file location";

	private static final String useThread = "Thread usage";
	private static final String threads = "Threads number";

	private static final String memoryWatch = "Peak memory usage";

	private static final String verbose = "verbose mode";

	private static final String ultraVerbose = "ultra-verbose mode";

	// public static final String PARAMETERFREQUENCY = " frequency ";

	public PlcmAlgo(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {

		@SuppressWarnings("deprecation")
		RMTransactions dataSet = input.getData();
		// RMTransactions transcaions = dataSetOriginal;
		// loadedData.add(new Integer(1));

		try {
			// String[] arguments = new String[2];

			String support = "1";
			String outputLocation = null;

			// boolean showThread
			boolean showThreadNb = false;
			int threadsNb = 1;

			boolean startMemoryWatch = false;
			boolean verboseMode = false;
			boolean ultraVerboseMode = false;

			// arguments[0] = this.getParameter(operation);
			// arguments[1] = this.getParameter(dataset);
			support = this.getParameter(threshold);
			outputLocation = this.getParameter(results);

			showThreadNb = this.getParameterAsBoolean(useThread);
			threadsNb = this.getParameterAsInt(threads);

			startMemoryWatch = this.getParameterAsBoolean(memoryWatch);
			verboseMode = this.getParameterAsBoolean(verbose);
			ultraVerboseMode = this.getParameterAsBoolean(ultraVerbose);

			if (outputLocation.isEmpty() || outputLocation.equals(" ")
					|| outputLocation.matches("\\s")) {
				outputLocation = null;
			}

			this.doLcm(support, outputLocation, dataSet, showThreadNb,
					threadsNb, startMemoryWatch, verboseMode, ultraVerboseMode);

			// this.endLcm();
			// res.deliver(arguments);
		} catch (UndefinedParameterError e) {
			System.err.println("Get Parameter error");
			e.printStackTrace();
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeBoolean(
				useThread,
				"if checked, you can input the number of thread which you want to use for your work",
				false, false));

		ParameterType threadsType = new ParameterTypeInt(
				threads,
				"How many threads will be launched (defaults to your machine's processors count)",
				1, 4, 1, false);

		types.add(new ParameterTypeBoolean(
				memoryWatch,
				"Give peak memory usage after mining (instanciates a watcher thread that periodically triggers garbage collection)",
				false, false));

		types.add(new ParameterTypeBoolean(
				verbose,
				"Enable verbose mode, which logs every extension of the empty pattern",
				false, false));
		types.add(new ParameterTypeBoolean(
				ultraVerbose,
				"Enable ultra-verbose mode, which logs every pattern extension (use with care: it may produce a LOT of output)",
				false, false));

		types.add(new ParameterTypeString(threshold, "threshold", "1", false));

		types.add(new ParameterTypeString(results, "Location of output files",
				" ", false));

		threadsType.registerDependencyCondition(new BooleanParameterCondition(
				this, useThread, true, true));

		types.add(threadsType);

		return types;
	}

	// public void doLcm(String[] args, RMTransactions dataSet,
	public void doLcm(String support, String outputLocation,
			RMTransactions dataSet, boolean showThreadNb, int threadsNb,
			boolean startMemoryWatch, boolean verboseMode,
			boolean ultraVerboseMode) {

		int nbThreads = Runtime.getRuntime().availableProcessors();
//		Options options = new Options();
//		options.addOption(
//				"b",
//				false,
//				"Benchmark mode : patterns are not outputted at all (in which case OUTPUT_PATH is ignored)");
//		options.addOption("h", false, "Show help");

		// CommandLine cmd;

		// cmd = parser.parse(options, args);
		String outputPath = null;
		outputPath = outputLocation;
		nbThreads = threadsNb;

		// PatternsCollector collector = initCollector(outputPath, nbThreads);
		PatternsCollector collector = initCollector(null, nbThreads);

		PLCM miner = new PLCM(collector, nbThreads);

		// PLCM plcm = new PLCM(null, applyCountAtLastExecution)

		PLCM.standalone(support, outputLocation, dataSet, miner, output,
				infoOutput, showThreadNb, startMemoryWatch, verboseMode,
				ultraVerboseMode);
		// PLCM.getResConsole(miner);
		// PLCM.printMan(options);
	}

	private static PatternsCollector initCollector(String outputPath,
			int nbThreads) {
		PatternsCollector collector = null;
		if (outputPath != null) {
			try {
				collector = new MultiThreadedFileCollector(outputPath,
						nbThreads);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Aborting mining.");
				System.exit(1);
			}
		} else
			collector = new RPCollector();
		// collector = new StdOutCollector();
		return collector;
	}
}
