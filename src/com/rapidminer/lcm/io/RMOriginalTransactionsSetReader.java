package com.rapidminer.lcm.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.internals.transactions.RMStringTransaction;
import com.rapidminer.lcm.internals.transactions.RMStringTransactions;
import com.rapidminer.lcm.internals.transactions.RMTransaction;
import com.rapidminer.lcm.internals.transactions.RMTransactions;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.tools.Ontology;

public class RMOriginalTransactionsSetReader extends Operator implements
		FIMIReader {

	private static final String FILE_LOCATION = "file";

	private static final String useRegex = "Special Separator";
	private static final String regex = "regex";

	private OutputPort output = this.getOutputPorts().createPort("out");
	private OutputPort stdoutput = this.getOutputPorts().createPort(
			"orginal data");

	private RMStringTransactions transactions;
	private RMStringTransaction transaction;

	private int sizeofLongestTransaction = 0;

	public RMOriginalTransactionsSetReader(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {

		long lStartTime = System.currentTimeMillis();
		readFile();

		if (stdoutput.isConnected()) {
			stdoutput.deliver(this.showOriginalStringData(transactions));
		}
		
		output.deliver(this.transactions);

		long lEndTime = System.currentTimeMillis();

		long difference = lEndTime - lStartTime;

		System.out.println("TI reader in milliseconds: " + difference);
		// transactions = new RMStringTransactions();
	}

	@Override
	public void readFile() {

		BufferedInputStream bufferInput = null;
		try {
			File file = this.getParameterAsFile(FILE_LOCATION);
			bufferInput = new BufferedInputStream(new FileInputStream(file),
					10 * 1024 * 1024);
		} catch (UserError e) {
			this.logError("Unknown errore when get file!");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			this.logError("File not found!");
			e.printStackTrace();
		}

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(bufferInput));

		String line;

		transactions = new RMStringTransactions();
		try {

			boolean startUseRegex = this.getParameterAsBoolean(useRegex);
			String lineRegex = "\\s";

			if (startUseRegex) {
				while ((line = bufferedReader.readLine()) != null) {

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

					// tempList.add(newline);
					transaction = new RMStringTransaction(newline);

					if (transaction.size() > sizeofLongestTransaction) {
						sizeofLongestTransaction = transaction.size();
					}

					transactions.add(transaction);
					// tempList.remove(0);
				}
			} else {
				while ((line = bufferedReader.readLine()) != null) {
					String[] newline = line.split("\\t");
					transaction = new RMStringTransaction(newline);
					// lengths.add(transaction.size()-1);
					if (transaction.size() > sizeofLongestTransaction) {
						sizeofLongestTransaction = transaction.size();
					}

					transactions.add(transaction);
					// tempList.remove(0);
				}
			}

			bufferedReader.close();
		} catch (IOException e) {
			this.logError("Error when do line reading!");
			e.printStackTrace();
		} catch (UndefinedParameterError e) {
			this.logError("Undefined Parameter Error");
			e.printStackTrace();
		}

	}

	private String[] splitTransaction(String line, String lineRegex) {
		String transactionLine[];
		transactionLine = line.split(lineRegex);
		return transactionLine;
	}

	@Override
	public int getLengthOfLongestTransaction() {
		return sizeofLongestTransaction;
	}

	public ExampleSet showOriginalStringData(RMStringTransactions transactions) {
		Attribute[] attributes = new Attribute[this
				.getLengthOfLongestTransaction()];

		for (int i = 0; i < this.getLengthOfLongestTransaction(); i++) {
			attributes[i] = AttributeFactory.createAttribute("att" + i,
					Ontology.STRING);
		}

		MemoryExampleTable table = new MemoryExampleTable(attributes);

		DataRowFactory ROW_FACTORY = new DataRowFactory(0, '.');

		for (int i = 0; i < transactions.getTransactions().size(); i++) {
			String[] data = new String[attributes.length];
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
	public ExampleSet showOriginalData(RMTransactions transactions) {
		// TODO Auto-generated method stub
		return null;
	}
}
