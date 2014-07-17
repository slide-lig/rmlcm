package com.rapidminer.lcm.exceptions;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.rapidminer.operator.OperatorException;

public class WrongDatabasePathException extends OperatorException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8108412045450746159L;

	public WrongDatabasePathException(String errorMessage) {
		super(errorMessage);
	}

	public void errorDialog() {
		JFrame errorframe = new JFrame();
		JOptionPane
				.showMessageDialog(
						errorframe,
						"Wrong Database Path Exception",
						"Please check your database path!", JOptionPane.ERROR_MESSAGE);
	}
}
