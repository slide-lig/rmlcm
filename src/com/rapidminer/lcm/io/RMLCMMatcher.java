package com.rapidminer.lcm.io;

import java.util.List;

import com.rapidminer.SimpleWindow;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;

public class RMLCMMatcher extends Operator{
	
	//private static final String createItemMatcher = ""
	private OutputPort output = this.getOutputPorts().createPort("out");

	public RMLCMMatcher(OperatorDescription description) {
		super(description);
	}
	
	@Override
	public void doWork() throws OperatorException {
		this.createMatchBox();
	}

	public void createMatchBox (){
		new SimpleWindow();
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		return types;
	}
}
