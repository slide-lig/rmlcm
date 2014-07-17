package com.rapidminer.lcm.io;

import gnu.trove.map.hash.THashMap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.obj.ResultListIOObject;
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

public class RMReaderWithComposer extends Operator {

	// private static final String createItemMatcher = ""
	private OutputPort output = this.getOutputPorts().createPort("out");
	private InputPort input = this.getInputPorts().createPort("in");

	private static final String FILE = "Identifier-File";

	private static final String USE_REGEX = "Special separator (Default: blank space)";
	private static final String REGEX = "File-Separator ";

	private boolean useregex = false;

	private THashMap<Integer, String> map = new THashMap<Integer, String>();

	public RMReaderWithComposer(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		// RMTransactions data = input.getData(RMTransactions.class);
		ResultListIOObject data = input.getData(ResultListIOObject.class);
		
		
		this.readIdentifierFile();
		output.deliver(this.getMatchedTable(data, map));
	}

	public void readIdentifierFile() {
	
		
		BufferedInputStream bufferInput = null;
		
		
		
		try {
			File file = this.getParameterAsFile(FILE);
			
			useregex = this.getParameterAsBoolean(USE_REGEX);
			bufferInput = new BufferedInputStream(new FileInputStream(file),
					10 * 1024 * 1024);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					bufferInput));

			String line;

			String regex = "\\s";
			if (this.useregex) {
				regex = this.getParameter(REGEX);
			}

			if (regex.equals(null) || regex.equals(null) || regex.endsWith(" ")
					|| regex == null) {
				regex = "\\s";
			}

			while ((line = input.readLine()) != null) {
				String[] newline = this.splitIDName(line, regex);
				int key = Integer.valueOf(newline[0]);
				map.put(key, newline[1]);
				// System.out.println("KEY " + key + "  VALUE " + newline[1]);
			}

			input.close();

		} catch (FileNotFoundException e) {
			System.err.println("file not find!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("read line error!");
			e.printStackTrace();
		} catch (UndefinedParameterError e) {
			System.err.println("Undefined Parameter Error!");
			e.printStackTrace();
		} catch (UserError e) {
			System.err.println("User Error!");
			e.printStackTrace();
		}
	}

	/**
	 * split the line in file by "regex" to a transaction array
	 * 
	 * @param dataLine
	 * @param regex
	 * @return
	 */
	public String[] splitIDName(String dataLine, String regex) {
		String[] identify = new String[2];
		String[] identifyTmp = dataLine.split(regex);
		identify[0] = identifyTmp[0];

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < identifyTmp.length; i++) {
			sb.append(identifyTmp[i]);
		}

		identify[1] = sb.toString();
		return identify;
	}

	public int getLengthOfLongestPattern(ResultListIOObject data) {
		int length = 0;
		for (int[] item : data.getResultlist()) {
			if (item.length > length) {
				length = item.length;
			}
		}
		return length;
	}

	public ExampleSet getMatchedTable(ResultListIOObject result,
			THashMap<Integer, String> map) {

		Attribute[] attributes = new Attribute[this
				.getLengthOfLongestPattern(result)];

		// System.out.println("att length : "+ attributes.length);

		attributes[0] = AttributeFactory.createAttribute("Support",
				Ontology.INTEGER);
		for (int i = 1; i < attributes.length; i++) {
			attributes[i] = AttributeFactory.createAttribute("item " + i,
					Ontology.STRING);
		}

		MemoryExampleTable table = new MemoryExampleTable(attributes);
		DataRowFactory ROW_FACTORY = new DataRowFactory(0, ',');
		String data[] = new String[attributes.length];
		Arrays.fill(data, null);
		// System.out.println("length "+ data.length);
		// TIntArrayList line = new TIntArrayList();

		boolean isSupport;
		for (int[] pattern : result.getResultlist()) {
			if (pattern[0] >= result.getSupport()) {
				isSupport = true;
				for (int i = 0; i < pattern.length - 1; i++) {
					// data[i]=pattern[i];
					if (isSupport) {
						data[i] = String.valueOf(pattern[i]);
						//System.out.print("Support: " + data[i]);
					} else {
						data[i] = map.get(pattern[i]);
						//System.out.print(" item: " + data[i]);
					}
					isSupport = false;
				}
				//System.out.println(" ");
				DataRow dataRow = ROW_FACTORY.create(data, attributes);
				table.addDataRow(dataRow);
			}
			Arrays.fill(data, null);
		}

		ExampleSet newExampleSet = table.createExampleSet();
		return newExampleSet;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(FILE,
				"The file to identify each item ", "txt", false));

		ParameterType regexMatcher = new ParameterTypeString(REGEX,
				"the regex of separators which you want to match", "\\s", false);

		types.add(new ParameterTypeBoolean(USE_REGEX,
				"Use special separator for this file", false, false));

		regexMatcher.registerDependencyCondition(new BooleanParameterCondition(
				this, USE_REGEX, true, true));

		types.add(regexMatcher);
		return types;
	}
}
