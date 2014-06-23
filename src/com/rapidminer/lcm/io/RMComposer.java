package com.rapidminer.lcm.io;

import gnu.trove.map.hash.THashMap;

import java.util.Arrays;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.obj.IdentifyHashMapIOObject;
import com.rapidminer.lcm.obj.ResultListIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.tools.Ontology;

public class RMComposer extends Operator {

	private InputPort matcherinput = this.getInputPorts().createPort(
			"identify map input");
	// The input can be RMTransactions or ResultListIOObject(output of PLCM)
	private InputPort tpinput = this.getInputPorts().createPort(
			"data of transactions or results");
	// The result of lcm in the format of String
	private OutputPort output = this.getOutputPorts().createPort(
			"result in string");

	public RMComposer(OperatorDescription description) {
		super(description);
		tpinput.addPrecondition(new SimplePrecondition(tpinput, new MetaData(
				ResultListIOObject.class)));
	}

	public void doWork() throws OperatorException {
		// tpinput.checkPreconditions();
		
		//if(tpinput.getAnyDataOrNull() instanceof ResultListIOObject)
		//	System.out.println("it is");
		
		long lStartTime = System.currentTimeMillis();
		ResultListIOObject resultList = tpinput
				.getData(ResultListIOObject.class);
		IdentifyHashMapIOObject identifyMap = matcherinput
				.getData(IdentifyHashMapIOObject.class);

		output.deliver(this.transformResultTransactionsAsString(resultList,
				identifyMap));
		long lEndTime = System.currentTimeMillis();
		
		long difference = lEndTime - lStartTime;
		 
		System.out.println("Composer in milliseconds: " + difference);
	}

	public ExampleSet transformResultTransactionsAsString(
			ResultListIOObject resultList, IdentifyHashMapIOObject identifyMap) {

		Attribute[] attributes = new Attribute[this
				.getLengthOfLongestPattern(resultList)];

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

		boolean isSupport;

		for (int[] pattern : resultList.getResultlist()) {
			isSupport = true;
			if (pattern[0] >= resultList.getSupport()) {
				for (int i = 0; i < pattern.length - 1; i++) {
					if (isSupport) {
						data[i] = String.valueOf(pattern[i]);
						// System.out.print("Support: " + data[i]);
					} else {
						data[i] = identifyMap.getHashmap().get(pattern[i]);
						// data[i] =
						// this.getKeyByValue(identifyMap.getHashmap(),
						// pattern[i]);
						// System.out.print(" item: " + data[i]);
					}
					isSupport = false;
				}
				DataRow dataRow = ROW_FACTORY.create(data, attributes);
				table.addDataRow(dataRow);
			}
			Arrays.fill(data, null);
		}
		ExampleSet newExampleSet = table.createExampleSet();
		return newExampleSet;
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

	@SuppressWarnings("hiding")
	public <String, Integer> String getKeyByValue(
			THashMap<String, Integer> map, Integer value) {
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
