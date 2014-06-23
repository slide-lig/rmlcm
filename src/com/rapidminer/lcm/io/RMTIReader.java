package com.rapidminer.lcm.io;

import gnu.trove.list.array.TIntArrayList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.internals.transactions.RMTransaction;
import com.rapidminer.lcm.internals.transactions.RMTransactions;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.tools.Ontology;

/**
 * @author John624
 * 
 *         This Reader is also for reading Frequent Itemset Mining Dataset, but
 *         the format is different form a general DataSet for exemple:
 * 
 *         Tid item
 * 
 *         1 3
 * 
 *         1 4
 * 
 *         2 5
 * 
 *         3 9
 * 
 *         3 10
 * 
 *         this DataSet equals to the format like:
 * 
 *         transaction 1: 3 4
 * 
 *         transaction 2: 5
 * 
 *         transaction 3: 9 10
 */
public class RMTIReader extends Operator implements FIMIReader {

	private OutputPort output = this.getOutputPorts().createPort("out");
	private OutputPort res = this.getOutputPorts().createPort("res");

	private final String FILE_LOCATION = "file";

	// private ArrayList<String> list = new ArrayList<String>();

	private static final String useRegex = "Special Separator";
	private static final String regex = "regex";

	private RMTransactions transactions;
	private RMTransaction transaction = new RMTransaction(new TIntArrayList());

	// private List<Integer> lengths;
	private boolean endline = true;
	private int sizeofLongestTransaction = 0;

	public RMTIReader(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		long lStartTime = System.currentTimeMillis();

		readFile();
		
		if (res.isConnected()) {
			res.deliver(this.showOriginalData(this.transactions));
		}
		output.deliver(transactions);

		long lEndTime = System.currentTimeMillis();

		long difference = lEndTime - lStartTime;

		System.out.println("TI reader in milliseconds: " + difference);
	}

	public void readFile() {

		BufferedInputStream bufferInput = null;
		// lengths = new ArrayList<Integer>();

		try {
			File file = this.getParameterAsFile(FILE_LOCATION);
			bufferInput = new BufferedInputStream(new FileInputStream(file),
					10 * 1024 * 1024);
		} catch (FileNotFoundException e) {
			System.err.println("input file error !");
			e.printStackTrace();
		} catch (UserError e) {
			System.err.println("input file error !");
			e.printStackTrace();
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(
				bufferInput));

		String line;
		transactions = new RMTransactions();

		boolean startUseRegex = this.getParameterAsBoolean(useRegex);
		String lineRegex = "\\s";

		try {

			if (startUseRegex) {
				while ((line = input.readLine()) != null) {

					if (lineRegex.isEmpty() || lineRegex.equals(null)
							|| lineRegex == null) {
						lineRegex = "\\s";
					} else {
						lineRegex = this.getParameterAsString(regex);
						if (lineRegex.isEmpty() || lineRegex.equals(null)
								|| lineRegex == null) {
							lineRegex = "\\s";
						}
					}

					String[] newline = this.splitTransaction(line, lineRegex);

					// String[] newline = line.split("\\s");
					// System.out.println(newline[0]+"  "+newline[1]);
					bulidTransaction(transaction, newline);
				}

				if (endline) {
					// lengths.add(transaction.size() - 1);
					if (transaction.size() > sizeofLongestTransaction) {
						sizeofLongestTransaction = transaction.size() - 1;

					}
					transaction.remove(0);
					transactions.add(transaction);
					transaction = new RMTransaction(new TIntArrayList());
					// transaction.clear();
					endline = false;
				}
			} else {

				while ((line = input.readLine()) != null) {

					// String[] newline = this.splitTransaction(line,
					// lineRegex);

					String[] newline = line.split("\\s");
					// System.out.println(newline[0]+"  "+newline[1]);
					bulidTransaction(transaction, newline);
				}

				if (endline) {
					// lengths.add(transaction.size() - 1);
					if (transaction.size() > sizeofLongestTransaction) {
						sizeofLongestTransaction = transaction.size() - 1;

					}
					transaction.remove(0);
					transactions.add(transaction);
					transaction = new RMTransaction(new TIntArrayList());
					// transaction.clear();
					endline = false;
				}

			}
			input.close();

		} catch (IOException e) {
			System.err.println("can't read this line!");
			e.printStackTrace();
		} catch (UndefinedParameterError e) {
			System.err.println("Undefined Parameter Error!");
			e.printStackTrace();
		}

		// for (TIntArrayList list : transactions.getTransactions()) {
		// for (String string : list) {
		// System.out.print(string + " ");
		// }
		// System.out.println(" ");
		// }
	}

	public void bulidTransaction(RMTransaction rmtransaction, String[] newline) {
		if (newline.length != 2) {
			System.err.println("not match the file format of this reader!");
		} else {

			int tid = Integer.valueOf(newline[0]);
			int item = Integer.valueOf(newline[1]);

			if (rmtransaction.getTransaction().isEmpty()) {
				// for (int i = 0; i < 2; i++) {
				// rmtransaction.getTransaction().add(
				// Integer.valueOf(newline[i]));
				// }
				rmtransaction.getTransaction().add(tid);
				rmtransaction.getTransaction().add(item);
			} else {
				if (rmtransaction.getTransaction().get(0) == tid) {
					rmtransaction.getTransaction().add(item);
				} else {
					// this.list = new ArrayList<String>();
					this.transaction = new RMTransaction(new TIntArrayList());
					// for (int i = 0; i < 2; i++) {
					transaction.add(tid);
					transaction.add(item);
					// }

					if (rmtransaction.size() > sizeofLongestTransaction) {
						sizeofLongestTransaction = rmtransaction.size() - 1;
					}
					// lengths.add(rmtransaction.size() - 1);
					rmtransaction.remove(0);
					transactions.add(rmtransaction);

				}
			}
		}
	}

	/**
	 * split the line in file by "regex" to a transaction array
	 * 
	 * @param dataLine
	 * @param regex
	 * @return
	 */
	public String[] splitTransaction(String dataLine, String regex) {
		String transactionLine[];
		transactionLine = dataLine.split(regex);
		return transactionLine;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeBoolean(useRegex,
				"Use sepecial regex of separator", false));

		ParameterType regexMatcher = new ParameterTypeString(regex,
				"the regex of separators which you want to match", null, false);

		types.add(new ParameterTypeFile(
				FILE_LOCATION,
				"this parameter defines the location of file which you want to read.",
				"txt", false));

		regexMatcher.registerDependencyCondition(new BooleanParameterCondition(
				this, useRegex, true, true));

		types.add(regexMatcher);

		return types;
	}

	@Override
	public int getLengthOfLongestTransaction() {
		return sizeofLongestTransaction + 1;
	}

	@Override
	public ExampleSet showOriginalData(RMTransactions transactions) {

		System.out.println("LengthOfLongestTransaction  "
				+ this.getLengthOfLongestTransaction());

		Attribute[] attributes = new Attribute[this
				.getLengthOfLongestTransaction()];

		attributes[0] = AttributeFactory.createAttribute("Transaction No.",
				Ontology.INTEGER);
		for (int i = 1; i < this.getLengthOfLongestTransaction(); i++) {
			attributes[i] = AttributeFactory.createAttribute("att" + i,
					Ontology.INTEGER);
		}

		MemoryExampleTable table = new MemoryExampleTable(attributes);
		DataRowFactory ROW_FACTORY = new DataRowFactory(0, '.');
		for (int i = 0; i < transactions.getTransactions().size(); i++) {
			Integer[] data = new Integer[attributes.length];
			Arrays.fill(data, null);
			for (int j = 0; j < transactions.getTransactions().get(i).size(); j++) {
				data[j] = transactions.getTransactions().get(i).get(j);
			}
			DataRow dataRow = ROW_FACTORY.create(data, attributes);
			table.addDataRow(dataRow);
		}
		ExampleSet resultExampleSet = table.createExampleSet();

		return resultExampleSet;
	}
}
