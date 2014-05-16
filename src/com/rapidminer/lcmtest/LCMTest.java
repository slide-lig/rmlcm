package com.rapidminer.lcmtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;

public class LCMTest extends Operator {

	private static final String PARAMETER_FREQUENCY = "frequency";
	private InputPort prinput = this.getInputPorts().createPort("inp");
	private OutputPort proutput = this.getOutputPorts().createPort("outp");
	private OutputPort resout = this.getOutputPorts().createPort("res");

	private ArrayList<Double> allElements;
	private HashMap<Double, Integer> counterItemMap;

	public LCMTest(OperatorDescription description) {
		super(description);

		// add precondition for a input port,If we insert the operator into a
		// process without connecting an exampleset output port with our input
		// port, an error is shown.
		allElements = new ArrayList<Double>();
		counterItemMap = new HashMap<Double, Integer>();

		ExampleSetPrecondition inputPreCondition = new ExampleSetPrecondition(
				prinput, Ontology.ATTRIBUTE_VALUE);
		prinput.addPrecondition(inputPreCondition);
	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet exampleSet = prinput.getData();

		Attributes attributes = exampleSet.getAttributes();

		Attribute sourceAttribute = attributes.get("label");

		// name of new column produced;
		String counterName = "count row(" + sourceAttribute.getName() + ")";
		// generate a new column
		Attribute countAttribute = AttributeFactory.createAttribute(
				counterName, Ontology.INTEGER);

		System.out.println("counterName: " + sourceAttribute.getName());

		countAttribute.setTableIndex(sourceAttribute.getTableIndex());
		attributes.addRegular(countAttribute);
		attributes.remove(sourceAttribute);
		System.out
				.println("===================================================");
		int cpt = 0;
		for (Example example : exampleSet) {
			System.out.println(cpt);
			for (Attribute attribute : attributes) {
				allElements.add(example.getValue(attribute));
			}
			cpt++;
		}
		System.out
				.println("===================================================");


		System.out.println("$$$$$$$$$$$$$$$$$$$");

		Attribute[] newAttributes = new Attribute[2];

		newAttributes[0] = AttributeFactory.createAttribute("Item",
				Ontology.INTEGER);
		newAttributes[1] = AttributeFactory.createAttribute("Frequency",
				Ontology.INTEGER);

		MemoryExampleTable table = new MemoryExampleTable(newAttributes);

		DataRowFactory ROW_FACTORY = new DataRowFactory(0);

		Integer[] integers = new Integer[2];

		for (Entry<Double, Integer> mapValueCpt : this.countItems(allElements)
				.entrySet()) {
			integers[0] = mapValueCpt.getKey().intValue();
			integers[1] = mapValueCpt.getValue();
			DataRow row = ROW_FACTORY.create(integers, newAttributes);
			table.addDataRow(row);
		}

		ExampleSet newExampleSet = table.createExampleSet();

		System.out.println("$$$$$$$$$$$$$$$$$$$");
		proutput.deliver(exampleSet);
		resout.deliver(newExampleSet);
	}

	// get all item set and put each item in a list
	public ArrayList<Double> getItems(ArrayList<Double> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = list.size() - 1; j > i; j--) {
				if (list.get(j).equals(list.get(i)) || list.get(j).isNaN()) {
					list.remove(j);
				}
			}
		}
		return list;
	}

	// count the frequency of each item, put the item and its frequency in a
	// hashmap
	public HashMap<Double, Integer> countItems(ArrayList<Double> entireSet) {
		// HashSet<Double> hs = new HashSet<Double>();
		for (Double ele : entireSet) {
			if (!ele.isNaN()) {
				counterItemMap.put(ele, counterItemMap.get(ele) == null ? 1
						: counterItemMap.get(ele) + 1);
			}
		}
		return counterItemMap;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(
				PARAMETER_FREQUENCY,
				"this parameter defines the number of seconds between the start of two subsequent subprocess executions.",
				1, Integer.MAX_VALUE, 5, false));
		return types;
	}

}
