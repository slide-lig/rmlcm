package com.rapidminer.lcm.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReadTester {

	private ArrayList<String> transaction;
	private ArrayList<ArrayList<String>> transactions;

	public ReadTester(String address) {
		// transactions = new ArrayList<RMTransaction>();
		transaction = new ArrayList<String>();

		transactions = new ArrayList<ArrayList<String>>();

		File file = new File(address);
		BufferedInputStream bufferInput = null;
		try {
			bufferInput = new BufferedInputStream(new FileInputStream(file),
					10 * 1024 * 1024);
		} catch (FileNotFoundException e) {
			System.err.println("no such file!");
			e.printStackTrace();
		}
		BufferedReader input = new BufferedReader(new InputStreamReader(
				bufferInput));

		String line;

		try {
			while ((line = input.readLine()) != null) {
				// Pattern pattern = Pattern.compile();
				String[] newline = line.split("\\s");

				for (int i = 0; i < newline.length; i++) {
					transaction.add(newline[i]);
					// System.out.println(newline[i]);
				}
				// System.out.println(newline + ",   end");
				transactions.add(transaction);
			}

		} catch (IOException e) {
			System.err.println("can't read this line!");
			e.printStackTrace();
		}
	}

	public ArrayList<String> getTransaction() {
		return transaction;
	}

	public void setTransaction(ArrayList<String> transaction) {
		this.transaction = transaction;
	}

	public ArrayList<ArrayList<String>> getTransactions() {
		return transactions;
	}

	public void setTransactions(ArrayList<ArrayList<String>> transactions) {
		this.transactions = transactions;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ads = "C:\\Users\\John624\\Documents\\Intership\\data\\datasets\\SubgroupMining\\Retail\\retail50.dat";
		ReadTester rt = new ReadTester(ads);

		ArrayList<ArrayList<String>> test = rt.getTransactions();

		for (ArrayList<String> arrayList : test) {
			for (String string : arrayList) {
				System.out.println(string);
			}

		}

		System.out.println(test.size() + " size +si");
	}
}
