package com.rapidminer.lcm.io;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.lcm.internals.transactions.RMTransactions;

/**
 * interface for different kind of "Transactions file" readers
 * 
 * @author John624
 * 
 */
public interface FIMIReader {

	/**
	 * Method to read a file;
	 */
	public void readFile();

	/**
	 * Method to get the length of the transaction which has the most number of
	 * items in it, this method is for create the array of attributes.
	 */
	public int getLengthOfLongestTransaction();
	
	/**
	 * Method to show the original data set in Rapidminer
	 */
	public ExampleSet showOriginalData(RMTransactions transactions);
}
