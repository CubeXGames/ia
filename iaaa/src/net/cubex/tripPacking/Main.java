package net.cubex.tripPacking;

import javax.swing.SwingUtilities;

public class Main {
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {

				try {
					
					Preferences.init();
					MainWindow.init();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});
	}
}
