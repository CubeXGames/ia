package net.cubex.trippacker;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import net.cubex.trippacker.items.Container;
import net.cubex.trippacker.items.Item;
import net.cubex.trippacker.items.Task;
import net.cubex.trippacker.items.TaskList;

public class TripFile {
	
	private static Gson gsonObject;
	
	static {
		
		gsonObject = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	}
	
	@Expose public ArrayList<Item> uncategorizedItems;
	@Expose public ArrayList<Task> uncategorizedTasks;
	@Expose public ArrayList<Container> baseContainers;
	@Expose public TaskList[] taskLists;
	@Expose public String[] taskListSequence;
	
	public TripFile() {
		
		uncategorizedItems = new ArrayList<Item>();
		uncategorizedTasks = new ArrayList<Task>();
		baseContainers = new ArrayList<Container>();
		taskLists = new TaskList[TaskList.DEFAULT_TASK_SEQUENCE.length];
		taskListSequence = TaskList.DEFAULT_TASK_SEQUENCE;
		
		for(int i = 0; i < taskLists.length; i++) taskLists[i] = new TaskList();
	}
	
	/**
	 * Loads a TripFile from a JSON string, and throws an IllegalStateException if some file integrity checks are not met.
	 * @return A TripFile instance.
	 */
	public static TripFile loadFromJson(String jsonString) {
		
		TripFile file = gsonObject.fromJson(jsonString, TripFile.class);
		
		//do some basic file integrity checks
		if(file.taskLists.length != file.taskListSequence.length) throw new IllegalStateException();
		for(Item i : file.uncategorizedItems) if(i.getContainer() != null) throw new IllegalStateException();
		for(Task t : file.uncategorizedTasks) if(t.taskList != null) throw new IllegalStateException();
		
		recursiveMakeContainersList(file.baseContainers);
		System.out.println(Arrays.toString(file.baseContainers.toArray()));
		return file;
	}
	
	private static void recursiveMakeContainersList(ArrayList<Container> containers) {
		
		for(Container c : containers) {
			
			TripPacker.containers.add(c);
			ArrayList<Container> subContainers = new ArrayList<Container>();
			for(Item i : c.getItems()) if(i instanceof Container) subContainers.add((Container)i);
			
			recursiveMakeContainersList(subContainers);
		}
	}
	
	public String convertToJSON() {

		System.out.println(Arrays.toString(uncategorizedItems.toArray()));
		System.out.println(Arrays.toString(baseContainers.toArray()));
		return gsonObject.toJson(this);
	}
}
