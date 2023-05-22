package net.cubex.trippacker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.cubex.trippacker.items.Container;
import net.cubex.trippacker.items.Item;
import net.cubex.trippacker.items.Task;
import net.cubex.trippacker.items.TaskList;

public class TripPacker {

	private TripPacker() {}
	
	public static final String APP_TITLE = "Trip Packer";
	private static final String APP_TITLE_NOT_SAVED = "Trip Packer*";
	private static final String FILE_OPENED_WINDOW_TITLE_START = "Trip Packer (";
	private static final char FILE_NOT_SAVED_CHAR = '*';
	public static final String JSON_EXTENSION = ".json";
	
	public static ArrayList<Container> containers;
	
	public static Item currentItem;
	public static Task currentTask;
	
	public static TripFile currentFile;
	public static Path filePath;
	public static boolean fileSaved;
	
	private static boolean initialized;
	
	protected static void init() {
		
		if(initialized) return;
		
		currentFile = new TripFile();
		filePath = null;
		fileSaved = true;
		initialized = true;
		containers = new ArrayList<Container>();
	}
	
	public static void createNewFile() throws IOException {
		
		if(checkCurrentFileSaved()) {
			
			currentFile = new TripFile();
			filePath = null;
			fileSaved = false;
			containers = new ArrayList<Container>();
		}
	}
	
	public static void openFile(File file) {
			
		try {
			
			currentFile = loadFile(file);
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		//construct trees
		DefaultTreeModel model = MainWindow.getItemsTreeModel();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		model.setRoot(root);
		MainWindow.setItemsTreeRoot(root);
		initializeTreeRoot(root, model);
		MainWindow.updateItemsTree();
		
		filePath = file.toPath();
		fileSaved = true;
		updateWindowTitle();
	}
	
	private static void initializeTreeRoot(DefaultMutableTreeNode root, DefaultTreeModel model) {
		
		for(int i = 0; i < currentFile.uncategorizedItems.size(); i++) {
			
			System.out.println("asdfghjkl");
			Item item = currentFile.uncategorizedItems.get(i);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
			item.node = node;
			model.insertNodeInto(node, root, 0);
		}
		
		for(Container c : currentFile.baseContainers) recursiveInitializeTreeRoot(root, model, c);
	}
	
	private static void recursiveInitializeTreeRoot(DefaultMutableTreeNode root, DefaultTreeModel model,
			Container container) {
		
		System.out.println("hi");
		
		DefaultMutableTreeNode containerNode = new DefaultMutableTreeNode(container);
		container.node = containerNode;
		model.insertNodeInto(containerNode, root, 0);
		
		for(int i = 0; i < container.getItems().size(); i++) {
			
			Item item = container.getItems().get(i);
			if(item instanceof Container) recursiveInitializeTreeRoot(containerNode, model, (Container)item);
			else {
				
				DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item);
				item.node = itemNode;
				model.insertNodeInto(itemNode, containerNode, 0);
			}
		}
	}
	
	private static TripFile loadFile(File file) throws IOException {
		
		String json = Files.readString(file.toPath());
		return TripFile.loadFromJson(json);
	}
	
	public static boolean saveFile() throws IOException {
		
		assert currentFile != null;
		if(fileSaved) return true;
		
		if(filePath == null) {
			
			JFileChooser fileChooser = new JFileChooser();
			
			String previousFilePath = Preferences.getPreference(Preferences.OPEN_PREVIOUS_FILE_PATH, System.getProperty("user.home"));
			fileChooser.setCurrentDirectory(new File(previousFilePath));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(false);
			FileFilter filter = new FileNameExtensionFilter("JSON Files (*.json)", "json");
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setAcceptAllFileFilterUsed(true);
			
			int result = fileChooser.showSaveDialog(MainWindow.getFrame());
			if(result == JFileChooser.APPROVE_OPTION) {
				
				File file = fileChooser.getSelectedFile();
				if(!file.getAbsolutePath().toLowerCase().endsWith(JSON_EXTENSION)) file = new File(file.getAbsolutePath() + JSON_EXTENSION);
				
				String path = file.getParent();
				if(path == null) path = "";
				
				Preferences.setPreference(Preferences.OPEN_PREVIOUS_FILE_PATH, path);
				filePath = file.toPath();
				
			} else return false;
		}
		
		String json = currentFile.convertToJSON();
		Files.writeString(filePath, json, StandardOpenOption.CREATE);
		
		fileSaved = true;
		updateWindowTitle();
		return true;
	}
	
	public static void resetCompletedStatuses() {
		
		for(TaskList tl : currentFile.taskLists) {
			
			for(Task t : tl.tasks) {
				
				t.setCompleted(false);
			}
		}
		
		for(Container c : currentFile.baseContainers) recursiveMakeItemsUncategorized(c);
		
		setFileNotSaved();
	}
	
	protected static void recursiveMakeItemsUncategorized(Container container) {
		
		ArrayList<Container> subContainers = new ArrayList<Container>();
		for(Item i : container.getItems()) {
			
			i.setContainer(null);
			MainWindow.getItemsTreeModel().removeNodeFromParent(i.node);
			if(i instanceof Container) subContainers.add((Container)i);
		}
		
		for(Container c : subContainers) recursiveMakeItemsUncategorized(c);
	}
	
	public static boolean checkCurrentFileSaved() throws IOException {
		
		if(!fileSaved) {
			
			int shouldSave = JOptionPane.showConfirmDialog(MainWindow.getFrame(), "Save current trip file?");
			
			if(shouldSave == JOptionPane.CANCEL_OPTION) return false;
			else if(shouldSave == JOptionPane.OK_OPTION) { return saveFile(); }
			else if(shouldSave == JOptionPane.NO_OPTION) return true;
			else return false;
		} else return true;
	}
	
	protected static void updateWindowTitle() {
		
		if(filePath != null) {

			StringBuilder builder = new StringBuilder();
			builder.append(FILE_OPENED_WINDOW_TITLE_START);
			builder.append(filePath);
			builder.append(')');
			
			if(!fileSaved) builder.append(FILE_NOT_SAVED_CHAR);
			
			MainWindow.setWindowTitle(builder.toString());
		} else {
			
			if(fileSaved) MainWindow.setWindowTitle(APP_TITLE);
			else MainWindow.setWindowTitle(APP_TITLE_NOT_SAVED);
		}
	}
	
	public static void setFileNotSaved() {
		
		fileSaved = false;
		updateWindowTitle();
	}
}
