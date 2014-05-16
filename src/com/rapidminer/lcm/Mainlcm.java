package com.rapidminer.lcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.lcm.io.MultiThreadedFileCollector;
import com.rapidminer.lcm.io.NullCollector;
import com.rapidminer.lcm.io.PatternSortCollector;
import com.rapidminer.lcm.io.PatternsCollector;
import com.rapidminer.lcm.io.RPCollector;
import com.rapidminer.lcm.io.StdOutCollector;
import com.rapidminer.lcm.obj.ExecuteInformationsIOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

public class Mainlcm extends OperatorChain {

	// Attributes for port of rapidminer
	private InputPort input = this.getInputPorts().createPort("in");
	private OutputPort output = this.getOutputPorts().createPort("res");

	private OutputPort innerThreadsSource = this.getSubprocess(0)
			.getInnerSources().createPort("thread");
	private InputPort innerThreadsSink = this.getSubprocess(0).getInnerSinks()
			.createPort("thread");
	private OutputPort infoOutput = this.getOutputPorts().createPort("info");

	// private static final String executionCommand = "key";

	// private static final String operation = "operation";
	// private static final String dataset = "dataset";
	private static final String threshold = "threshold";
	private static final String results = "Result file location";

	private static final String threads = "Threads";

	private static final String memoryWatch = "Peak memory usage";

	private static final String verbose = "verbose mode";

	private static final String ultraVerbose = "ultra-verbose mode";

	private List<Integer> loadedData = new LinkedList<Integer>();

	// public static final String PARAMETERFREQUENCY = " frequency ";

	public Mainlcm(OperatorDescription description) {
		super(description, "Property Extration");

		this.getTransformer().addGenerationRule(innerThreadsSource,
				ExecuteInformationsIOObject.class);
		this.getTransformer().addRule(
				new SubprocessTransformRule(this.getSubprocess(0)));
		this.getTransformer().addGenerationRule(infoOutput, ExampleSet.class);
	}

	@Override
	public void doWork() throws OperatorException {

		@SuppressWarnings("deprecation")
		ExampleSet dataSetOriginal = input.getData();

		// loadedData.add(new Integer(1));

		try {
			String[] arguments = new String[2];

			boolean showThreadNb = false;
			boolean startMemoryWatch = false;
			boolean verboseMode = false;
			boolean ultraVerboseMode = false;

			// arguments[0] = this.getParameter(operation);
			// arguments[1] = this.getParameter(dataset);
			arguments[0] = this.getParameter(threshold);
			arguments[1] = this.getParameter(results);

			showThreadNb = this.getParameterAsBoolean(threads);

			startMemoryWatch = this.getParameterAsBoolean(memoryWatch);
			verboseMode = this.getParameterAsBoolean(verbose);
			ultraVerboseMode = this.getParameterAsBoolean(ultraVerbose);

			this.doLcm(arguments, dataSetOriginal, showThreadNb,
					startMemoryWatch, verboseMode, ultraVerboseMode);

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
				threads,
				"How many threads will be launched (defaults to your machine's processors count)",
				false, false));

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

		// ParameterType type = new ParameterTypeInt(PARAMETERFREQUENCY,
		// "the number of seconds between the start o f two subsequent",
		// 1, 5);
		//
		// type.registerDependencyCondition(new BooleanParameterCondition(this,
		// threads, true, true));

		// types.add(new ParameterTypeString(operation, "operation", " ",
		// false));
		// types.add(new ParameterTypeString(dataset, "dataset", " ",
		// false));
		// types.add(type);

		types.add(new ParameterTypeString(threshold, "threshold", "1", false));
		types.add(new ParameterTypeString(results, "results", " ", false));
		return types;
	}

	public void doLcm(String[] args, ExampleSet dataSetOriginal,
			boolean showThreadNb, boolean startMemoryWatch,
			boolean verboseMode, boolean ultraVerboseMode) {

		int nbThreads = Runtime.getRuntime().availableProcessors();
		Options options = new Options();

		CommandLineParser parser = new PosixParser();

		options.addOption(
				"b",
				false,
				"Benchmark mode : patterns are not outputted at all (in which case OUTPUT_PATH is ignored)");
		options.addOption("h", false, "Show help");
		// options.addOption(
		// "m",
		// false,
		// "Give peak memory usage after mining (instanciates a watcher thread that periodically triggers garbage collection)");
		// options.addOption("s", false,
		// "Sort items in outputted patterns, in ascending order");
		// options.addOption(
		// "t",
		// true,
		// "How many threads will be launched (defaults to your machine's processors count)");
		// options.addOption("v", false,
		// "Enable verbose mode, which logs every extension of the empty pattern");
		// options.addOption(
		// "V",
		// false,
		// "Enable ultra-verbose mode, which logs every pattern extension (use with care: it may produce a LOT of output)");

		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
			String outputPath = null;

			if (cmd.hasOption('t')) {
				nbThreads = Integer.parseInt(cmd.getOptionValue('t'));
			}

			// if (showThreadNb) {
			// nbThreads = Integer.parseInt(cmd.getOptionValue('t'));
			// ExecuteInformationsIOObject rmNbThreads = new
			// ExecuteInformationsIOObject(nbThreads);

			// loadedData.add(new Integer(nbThreads));

			// innerThreadsSource.deliver(rmNbThreads);

			// try {
			//
			// ExampleSet resultSet = null;
			// getSubprocess(0).execute();
			//
			// NbThreadsIOObject result = innerThreadsSink.getData();
			//
			// if (resultSet == null) {
			// resultSet = createInitialExampleSet(rmNbThreads);
			// } else {
			// entendExampleSet(resultSet, result);
			// }

			// infoOutput.deliver(executeInfo);

			// this.infoOutput.deliver();
			// }
			// PatternsCollector collector = instanciateCollector(cmd,
			// outputPath,
			// nbThreads);

			PatternsCollector collector = initCollector(outputPath, nbThreads);

			PLCM miner = new PLCM(collector, nbThreads);

			// PLCM plcm = new PLCM(null, applyCountAtLastExecution)

			if (cmd.getArgs().length < 2 || cmd.getArgs().length > 3
					|| cmd.hasOption('h')) {
				PLCM.printMan(options);
			} else {
//				PLCM.standalone(cmd, dataSetOriginal, miner, output,
//						infoOutput, showThreadNb, startMemoryWatch,
//						verboseMode, ultraVerboseMode);
				// PLCM.getResConsole(miner);
			}
		} catch (ParseException e) {
			PLCM.printMan(options);
			e.printStackTrace();
		}
	}

	// private void entendExampleSet(ExampleSet resultSet, NbThreadsIOObject
	// result) {
	// // TODO Auto-generated method stub
	// }
	//
	// private ExampleSet createInitialExampleSet(NbThreadsIOObject nbthreads) {
	// // TODO Auto-generated method stub
	// return null;
	// }

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
		return collector;
	}

	/**
	 * Parse command-line arguments to instantiate the right collector
	 * 
	 * @param nbThreads
	 */

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
				// collector.
			}

			if (cmd.hasOption('s')) {
				collector = new PatternSortCollector(collector);
			}
		}

		return collector;
	}
}
