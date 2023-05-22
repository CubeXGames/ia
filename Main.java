package net.cubex.trippacker;

import javax.swing.SwingUtilities;

public class Main {
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(() -> {

			try {
				
				Preferences.init();
				TripPacker.init();
				MainWindow.init();
			} catch (Exception e) {

				e.printStackTrace();
			}
		});
	}
}
