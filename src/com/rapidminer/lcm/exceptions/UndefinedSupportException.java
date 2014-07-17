package com.rapidminer.lcm.exceptions;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.rapidminer.operator.OperatorException;

public class UndefinedSupportException extends OperatorException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8108412045450746159L;

	public UndefinedSupportException(String errorMessage) {
		super(errorMessage);
	}

	public void errorDialog() {
		JFrame errorframe = new JFrame();
		JOptionPane
				.showMessageDialog(
						errorframe,
						"Undefined Support",
						"Support can't to be vide or 0", JOptionPane.ERROR_MESSAGE);
	}
}
