package com.rapidminer.lcm;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.rapidminer.lcm.io.MultiThreadedFileCollector;
import com.rapidminer.lcm.io.NullCollector;
import com.rapidminer.lcm.io.PatternSortCollector;
import com.rapidminer.lcm.io.PatternsCollector;
import com.rapidminer.lcm.io.StdOutCollector;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

public class Mainlcm extends Operator {

	// Attributes for port of rapidminer
	private OutputPort res = this.getOutputPorts().createPort("res");
	private static final String executionCommand = "key";

	// private String option=null;
	// private String inputFileLocate = null;
	// private String outputfileLocate = null;

	public Mainlcm(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		try {
			String s = this.getParameter(executionCommand);
			this.beginLcm(s.split("\\s"));
			System.out.println(s);
			System.out.println("------------------");
			System.out.println("option:"+s.split("\\s")[0]);
			System.out.println("input:"+s.split("\\s")[1]);
			System.out.println("thread:"+s.split("\\s")[2]);
			System.out.println("output:"+s.split("\\s")[3]);
		} catch (UndefinedParameterError e) {
			System.err.println("Get Parameter error");
			e.printStackTrace();
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(executionCommand,
				"Execution command", " ", false));
		return types;
	}

	public void beginLcm(String[] args) {

		int nbThreads = Runtime.getRuntime().availableProcessors();
		Options options = new Options();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			String outputPath = null;

			if (cmd.hasOption('t')) {
				nbThreads = Integer.parseInt(cmd.getOptionValue('t'));
			}

			PatternsCollector collector = instanciateCollector(cmd, outputPath,
					nbThreads);

			PLCM miner = new PLCM(collector, nbThreads);

			// PLCM plcm = new PLCM(null, applyCountAtLastExecution)

			options.addOption(
					"b",
					false,
					"Benchmark mode : patterns are not outputted at all (in which case OUTPUT_PATH is ignored)");
			options.addOption("h", false, "Show help");
			options.addOption(
					"m",
					false,
					"Give peak memory usage after mining (instanciates a watcher thread that periodically triggers garbage collection)");
			options.addOption("s", false,
					"Sort items in outputted patterns, in ascending order");
			options.addOption(
					"t",
					true,
					"How many threads will be launched (defaults to your machine's processors count)");
			options.addOption("v", false,
					"Enable verbose mode, which logs every extension of the empty pattern");
			options.addOption(
					"V",
					false,
					"Enable ultra-verbose mode, which logs every pattern extension (use with care: it may produce a LOT of output)");

			if (cmd.getArgs().length < 2 || cmd.getArgs().length > 3
					|| cmd.hasOption('h')) {
				PLCM.printMan(options);
			} else {
				PLCM.standalone(cmd, miner);
			}
		} catch (ParseException e) {
			PLCM.printMan(options);
			e.printStackTrace();
		}

	}

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
