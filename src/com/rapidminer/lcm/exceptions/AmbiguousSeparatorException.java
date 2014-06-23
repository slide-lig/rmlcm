package com.rapidminer.lcm.exceptions;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * When user chose a wrong or ambiguous separator for a "Transactions file", the
 * file reader will throw a Exception as a dialog to user.
 * 
 * @author John624
 * 
 */
public class AmbiguousSeparatorException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -564959151258662914L;

	public AmbiguousSeparatorException(String errorMessage) {
		super(errorMessage);
	}
	
	public void errorDialog(){
		JFrame errorframe = new JFrame();
		JOptionPane.showMessageDialog(errorframe,
				"Separator error, please check the special separator which you used", "Regex error",
				JOptionPane.ERROR_MESSAGE);
	}
}
