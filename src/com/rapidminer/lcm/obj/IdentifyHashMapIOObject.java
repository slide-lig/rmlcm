package com.rapidminer.lcm.obj;

import gnu.trove.map.hash.THashMap;

import com.rapidminer.operator.ResultObjectAdapter;

public class IdentifyHashMapIOObject extends ResultObjectAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3706608715539303155L;

	private THashMap<Integer, String> hashmap;

	public IdentifyHashMapIOObject(THashMap<Integer, String> hashmap) {
		this.hashmap = hashmap;
	}

	public THashMap<Integer, String> getHashmap() {
		return hashmap;
	}

	public void setHashmap(THashMap<Integer, String> hashmap) {
		this.hashmap = hashmap;
	}

}
