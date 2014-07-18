package com.rapidminer.lcm.io;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.lcm.internals.transactions.RMStringTransactions;
import com.rapidminer.lcm.internals.transactions.RMTransaction;
import com.rapidminer.lcm.internals.transactions.RMTransactions;
import com.rapidminer.lcm.obj.IdentifyHashMapIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.Ontology;

public class RMDecomposer extends Operator {

	private InputPort input = this.getInputPorts().createPort("in");

	private OutputPort intTransactionOutput = this.getOutputPorts().createPort(
			"out");
	private OutputPort matchedOutput = this.getOutputPorts()
			.createPort("map");
	private OutputPort stdoutput = this.getOutputPorts().createPort(
			"orginal data");

	private int key = 1;

	private THashMap<String, Integer> map = new THashMap<String, Integer>();
	private THashMap<Integer, String> idmap = new THashMap<Integer, String>();

	private RMTransactions transactions;

	private int sizeofLongestTransaction = 0;

	public RMDecomposer(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		long lStartTime = System.currentTimeMillis();
		RMStringTransactions data = input.getData(RMStringTransactions.class);

		createIdentifyMap(data);
		transformTransactions(data);

		IdentifyHashMapIOObject identifyHashMap = new IdentifyHashMapIOObject(
				idmap);

		matchedOutput.deliver(identifyHashMap);

		if (intTransactionOutput.isConnected()) {
			intTransactionOutput.deliver(transactions);
		}
		
		if (stdoutput.isConnected()) {
			stdoutput.deliver(showOriginalData(transactions));
		}

		long lEndTime = System.currentTimeMillis();

		long difference = lEndTime - lStartTime;

		System.out.println("Composer in milliseconds: " + difference);

		// Iterator<Entry<String, Integer>> itr = map.entrySet().iterator();
		//
		// while (itr.hasNext()) {
		// Entry<String, Integer> entry = itr.next();
		//
		// System.out.println("KEY " + entry.getKey() + " : VALUE "
		// + entry.getValue());
		// }
		//
		//
		// for (int i = 0; i < transactions.getTransactions().size(); i++) {
		// System.out.print("transaction : ");
		// for (int j = 0; j < transactions.getTransactions().get(i).size();
		// j++) {
		// System.out.print(" "+transactions.getTransactions().get(i).get(j));
		// }
		// }
	}

	private void transformTransactions(RMStringTransactions data) {

		transactions = new RMTransactions();
		for (ArrayList<String> stringTransaction : data.getTransactions()) {
			TIntArrayList transaction = new TIntArrayList();
			for (int i = 0; i < stringTransaction.size(); i++) {
				transaction.add(map.get(stringTransaction.get(i)));
			}

			if (stringTransaction.size() > sizeofLongestTransaction) {
				sizeofLongestTransaction = stringTransaction.size();
			}

			RMTransaction intTransaction = new RMTransaction(transaction);
			transactions.add(intTransaction);
		}
	}

	public THashMap<Integer, String> createIdentifyMap(RMStringTransactions data) {
		// map.clear();

		for (ArrayList<String> stringTransaction : data.getTransactions()) {

			for (int i = 0; i < stringTransaction.size(); i++) {
				if (!map.containsKey(stringTransaction.get(i))) {
					// map.put(i, value)
					map.put(stringTransaction.get(i), key++);
					// map.put(i + 1, stringTransaction.get(i));
				}
			}
		}

		for (Entry<String, Integer> element : map.entrySet()) {
			idmap.put(element.getValue(), element.getKey());
		}
		return idmap;
	}

	public ExampleSet showOriginalData(RMTransactions transactions) {
		Attribute[] attributes = new Attribute[this
				.getLengthOfLongestTransaction()];

		for (int i = 0; i < this.getLengthOfLongestTransaction(); i++) {
			attributes[i] = AttributeFactory.createAttribute("att" + i,
					Ontology.INTEGER);
		}

		// create table
		MemoryExampleTable table = new MemoryExampleTable(attributes);

		DataRowFactory ROW_FACTORY = new DataRowFactory(0, '.');
		// fill table (here : only integer values )
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

	public int getLengthOfLongestTransaction() {
		return sizeofLongestTransaction;
	}
}
