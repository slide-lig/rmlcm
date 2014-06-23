package com.rapidminer.lcm.exceptions;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NoMatchedPatternsException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7083004533965946000L;

	public NoMatchedPatternsException(String errorMessage) {
		super(errorMessage);
	}
	
	public void errorDialog(){
		JFrame errorframe = new JFrame();
		JOptionPane.showMessageDialog(errorframe,
				"No such corresponding results as the support which you input, please input a smaller support", "Too big support error",
				JOptionPane.ERROR_MESSAGE);
	}
}
