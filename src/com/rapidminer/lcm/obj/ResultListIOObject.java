package com.rapidminer.lcm.obj;

import java.util.ArrayList;

import com.rapidminer.operator.ResultObjectAdapter;

public class ResultListIOObject extends ResultObjectAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4757579767660123618L;
	private ArrayList<int[]> resultlist;
	private int support;
	
	public ResultListIOObject(ArrayList<int[]> resultlist, int support) {
		this.resultlist = resultlist;
		this.support=support;
	}

	public ArrayList<int[]> getResultlist() {
		return resultlist;
	}

	public void setResultlist(ArrayList<int[]> resultlist) {
		this.resultlist = resultlist;
	}

	public int getSupport() {
		return support;
	}

	public void setSupport(int support) {
		this.support = support;
	}
}
