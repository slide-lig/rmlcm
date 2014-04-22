/**
 * 
 */
package com.rapidminer;

import com.rapidminer.gui.MainFrame;

/**
 * This class provides hooks for initialization
 * 
 */
public class PluginInitTemplate {
	
	/**
	 * This method will be called directly after the extension is initialized.
	 * This is the first hook during start up. No initialization of the operators
	 * or renderers has taken place when this is called.
	 */
	public static void initPlugin() {
		System.out.println("I am alive e2e2 ...");
	}
	
	/**
	 * This method is called during start up as the second hook. It is 
	 * called before the gui of the mainframe is created. The Mainframe is 
	 * given to adapt the gui.
	 * The operators and renderers have been registered in the meanwhile.
	 */
	public static void initGui(MainFrame mainframe) {
		mainframe.getDockingDesktop().registerDockable(new SimpleWindow());		
	}
	
	/**
	 * The last hook before the splash screen is closed. Third in the row.
	 */
	public static void initFinalChecks() {}

	/**
	 * Will be called as fourth method, directly before the UpdateManager is used
	 * for checking updates. Location for exchanging the UpdateManager. 
	 * The name of this method unfortunately is a result of a historical typo, so it's
	 * a little bit misleading.
	 */
	public static void initPluginManager() {}
}
