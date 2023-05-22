package net.cubex.trippacker;

public class Preferences {
	
	public static final String OPEN_PREVIOUS_FILE_PATH = "openPrevFilePath";
	
	private static java.util.prefs.Preferences preferences;
	private static boolean initialized;
	
	public static void init() {
		
		if(initialized) return;
		
		preferences = java.util.prefs.Preferences.userNodeForPackage(MainWindow.class);
		initialized = true;
	}
	
	public static String getPreference(String key, String defaultValue) {
		
		if(!initialized) throw new IllegalStateException();
		return preferences.get(key, defaultValue);
	}
	
	public static void setPreference(String key, String value) {
		
		if(!initialized) throw new IllegalStateException();
		preferences.put(key, value);
	}
	
	public static void resetAllPreferences() {
		
		preferences.remove(OPEN_PREVIOUS_FILE_PATH);
	}
}
