package com.rapidminer.lcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.exceptions.NoMatchedPatternsException;
import com.rapidminer.lcm.exceptions.UndefinedSupportException;
import com.rapidminer.lcm.internals.transactions.RMTransactions;
import com.rapidminer.lcm.io.MultiThreadedFileCollector;
import com.rapidminer.lcm.io.PatternsCollector;
import com.rapidminer.lcm.io.RMCollector;
import com.rapidminer.lcm.obj.ResultListIOObject;
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
import com.rapidminer.tools.Ontology;

public class PlcmAlgo extends Operator {

	// Attributes for port of rapidminer
	private InputPort input = this.getInputPorts().createPort("in");

	// result of PLCM as Example Table
	private OutputPort output = this.getOutputPorts().createPort("res");

	// result of PLCM as ResultListIOObject
	private OutputPort transformerOutput = this.getOutputPorts().createPort(
			"trs");

	// execution information
	private OutputPort infoOutput = this.getOutputPorts().createPort("info");

	// private static final String executionCommand = "key";

	// private static final String operation = "operation";
	// private static final String dataset = "dataset";
	private static final String threshold = "Support";

	private static final String beginWriteFile = "Write Mining Result As File(s)";
	private static final String results = "Result File Location";

	private static final String useThread = "Thread usage";
	private static final String threads = "Number of threads";

	private static final String memoryWatch = "Peak memory usage";

	private static final String verbose = "verbose mode";

	private static final String ultraVerbose = "ultra-verbose mode";

	private Attribute[] attributes;
	private Integer[] stdTransactionline;

	// public static final String PARAMETERFREQUENCY = " frequency ";

	public PlcmAlgo(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		long lStartTime = System.currentTimeMillis();
		// @SuppressWarnings("deprecation")
		RMTransactions dataSet = input.getData(RMTransactions.class);
		// RMTransactions transcaions = dataSetOriginal;
		// loadedData.add(new Integer(1));

		try {
			// String[] arguments = new String[2];

			String support = "10";
			String outputLocation = null;

			// boolean showThread
			boolean showThreadNb = false;
			int threadsNb = 1;

			boolean startMemoryWatch = false;
			boolean verboseMode = false;
			boolean ultraVerboseMode = false;
			boolean writeFile = false;

			// boolean writeFile = false;

			// arguments[0] = this.getParameter(operation);
			// arguments[1] = this.getParameter(dataset);
			support = this.getParameter(threshold);
			if (support.equals(null) || support == "" || support == "\\s"
					|| support.isEmpty() || support == null
					|| support.equals("0")) {
				throw new UndefinedSupportException("Support Error!");
			} else {
				support = this.getParameter(threshold);
			}

			writeFile = this.getParameterAsBoolean(beginWriteFile);

			if (writeFile) {
				outputLocation = this.getParameter(results);
			}

			showThreadNb = this.getParameterAsBoolean(useThread);
			threadsNb = this.getParameterAsInt(threads);

			startMemoryWatch = this.getParameterAsBoolean(memoryWatch);
			verboseMode = this.getParameterAsBoolean(verbose);
			ultraVerboseMode = this.getParameterAsBoolean(ultraVerbose);

			// writeFile = this.getParameterAsBoolean(beginWriteFile);

			if (outputLocation != null) {
				if (outputLocation.isEmpty() || outputLocation.equals(" ")
						|| outputLocation.matches("\\s")) {
					outputLocation = null;
				}
			}

			try {
				this.doLcm(support, outputLocation, dataSet, showThreadNb,
						threadsNb, startMemoryWatch, verboseMode,
						ultraVerboseMode);

				long lEndTime = System.currentTimeMillis();

				long difference = lEndTime - lStartTime;

				System.out.println("doWork milliseconds: " + difference);
			} catch (NoMatchedPatternsException e) {
				e.errorDialog();
			}

			if (transformerOutput.isConnected()) {
				ResultListIOObject resultlist = new ResultListIOObject(
						PLCM.getResList(), Integer.valueOf(support));
				transformerOutput.deliver(resultlist);
			}
			// this.endLcm();
			// res.deliver(arguments);
		} catch (UndefinedParameterError e) {
			System.err.println("Get Parameter error");
			e.printStackTrace();
		}

	}

	// get parameters that user inputed in the area of Parameters in Rapidminer
	// GUI
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
				1, 4, 1, true);

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

		types.add(new ParameterTypeString(threshold, "threshold", true));

		types.add(new ParameterTypeBoolean(
				beginWriteFile,
				"if checked, you can input a location and name of file(s) for mining results, the number of file depend the number of thread that you used",
				false, false));

		ParameterType outFileType = new ParameterTypeString(results,
				"Location of output file(s) that you want to generate", true);

		outFileType.registerDependencyCondition(new BooleanParameterCondition(
				this, beginWriteFile, true, true));

		threadsType.registerDependencyCondition(new BooleanParameterCondition(
				this, useThread, true, true));

		types.add(outFileType);
		types.add(threadsType);

		return types;
	}

	// Go into the PCLM algorithm and show the result as a table in the
	// Rapidminer
	// public void doLcm(String[] args, RMTransactions dataSet,
	public void doLcm(String support, String outputLocation,
			RMTransactions dataSet, boolean showThreadNb, int threadsNb,
			boolean startMemoryWatch, boolean verboseMode,
			boolean ultraVerboseMode) throws NoMatchedPatternsException {

		int nbThreads = Runtime.getRuntime().availableProcessors();
		// Options options = new Options();
		// options.addOption(
		// "b",
		// false,
		// "Benchmark mode : patterns are not outputted at all (in which case OUTPUT_PATH is ignored)");
		// options.addOption("h", false, "Show help");

		// CommandLine cmd;

		// cmd = parser.parse(options, args);
		String outputPath = null;
		outputPath = outputLocation;
		nbThreads = threadsNb;

		// System.out.println(outputPath);

		PatternsCollector collector = initCollector(outputPath, nbThreads);
		// PatternsCollector collector = initCollector(null, nbThreads);

		PLCM miner = new PLCM(collector, nbThreads);

		// PLCM plcm = new PLCM(null, applyCountAtLastExecution)

		PLCM.standalone(support, outputLocation, dataSet, miner, output,
				infoOutput, showThreadNb, startMemoryWatch, verboseMode,
				ultraVerboseMode);

		if (PLCM.getResList().size() < 1) {
			throw new NoMatchedPatternsException("Too big support exception");
		}

		else {
			long lStartTime = System.currentTimeMillis();
			createAttributes(PLCM.getResList());
			if (output.isConnected()) {
				createExampleTable(attributes, output);
			}
			long lEndTime = System.currentTimeMillis();
			long difference = lEndTime - lStartTime;
			System.out.println("Elapsed milliseconds: " + difference);
		}

		// PLCM.getResConsole(miner);
		// PLCM.printMan(options);
	}

	/**
	 * According to the "output path" and "number of Threads" which user used
	 * for generating a result collector
	 * 
	 * @param outputPath
	 * @param nbThreads
	 * @return
	 */
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
			collector = new RMCollector();
		// collector = new StdOutCollector();
		return collector;
	}

	/**
	 * Create attributes for all items in data set, for example: transaction A:
	 * 1 4 7 transaction B: 3 8
	 * 
	 * this method will create a item list with content like:
	 * "item1 item2 item3"
	 * 
	 * @param transactionsList
	 */
	public void createAttributes(ArrayList<int[]> transactionsList) {
		int lengthOflongestTransaction = 0;
		for (int i = 0; i < transactionsList.size(); i++) {
			if (transactionsList.get(i).length > lengthOflongestTransaction) {
				lengthOflongestTransaction = transactionsList.get(i).length;
			}
		}
		attributes = new Attribute[lengthOflongestTransaction - 1];
		attributes[0] = AttributeFactory.createAttribute("Support",
				Ontology.INTEGER);
		for (int i = 1; i < attributes.length; i++) {
			attributes[i] = AttributeFactory.createAttribute("item " + i,
					Ontology.INTEGER);
		}
	}

	/**
	 * create result as a Example Table to deliver to the result perspective,
	 * this method uses the array of attributes that generated before.
	 * 
	 * @param attributes
	 * @param output
	 */
	public void createExampleTable(Attribute[] attributes, OutputPort output) {
		stdTransactionline = new Integer[attributes.length];
		Arrays.fill(stdTransactionline, null);

		MemoryExampleTable table = new MemoryExampleTable(attributes);
		DataRowFactory ROW_FACTORY = new DataRowFactory(0, ',');

		for (int[] transaction : PLCM.getResList()) {

			// if (transaction[0] >= this.getParameterAsInt(threshold)) {
			for (int i = 0; i < transaction.length - 1; i++) {
				stdTransactionline[i] = transaction[i];
			}
			DataRow dataRow = ROW_FACTORY
					.create(stdTransactionline, attributes);
			table.addDataRow(dataRow);
			// }
			Arrays.fill(stdTransactionline, null);
		}
		ExampleSet resultExampleSet = table.createExampleSet();

		if (output.isConnected()) {
			output.deliver(resultExampleSet);
		}
	}
}
