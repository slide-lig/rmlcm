package com.rapidminer.lcm.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.rapidminer.operator.ResultObjectAdapter;

/**
 * This class if for present the execution informations of PLCM in Rapidminer as a result perspective
 * @author John624
 *
 */
public class ExecuteInformationsIOObject extends ResultObjectAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8036365048524859536L;

	private int nbThread;
	private String info;

	private ArrayList<String> verboseConsoles;

	private Map<String, Integer> valueMap = new HashMap<String, Integer>();

	public ExecuteInformationsIOObject(int nbThread, String info,
			ArrayList<String> verboseConsoles) {
		this.nbThread = nbThread;
		this.info = info;
		this.verboseConsoles = verboseConsoles;
	}

	public void setValue(String identifier, Integer value) {
		valueMap.put(identifier, value);
	}

	public Map<String, Integer> getValueMap() {
		return valueMap;
	}

	public int getNbThread() {
		return nbThread;
	}

	public void setNbThread(int nbThread) {
		this.nbThread = nbThread;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public ArrayList<String> getVerboseConsoles() {
		return verboseConsoles;
	}

	public void setVerboseConsoles(ArrayList<String> verboseConsoles) {
		this.verboseConsoles = verboseConsoles;
	}

	@Override
	public String toResultString() {
		StringBuffer res = new StringBuffer();

		if (nbThread != 0) {
			res.append("Threads:" + "\t");
			res.append(nbThread);
		}

		if (info != null) {
			res.append("\n");
			res.append("General execution information:" + "\n");
			res.append(info);
		}

		if (!(verboseConsoles == null)) {
			res.append("\n");
			for (String verboseConsole : verboseConsoles) {
				res.append(verboseConsole);
				res.append("\n");
			}
		}
		return res.toString();
	}
}
