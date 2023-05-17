package net.cubex.tripPacking.items;

import java.util.ArrayList;

public class TaskList {
	
	public static final String[] taskSequence = { "At Start of Packing", "Day Before", "Night Before", "Morning Of", "Immediately Before" };
	
	public ArrayList<Task> tasks;
	
	public TaskList() {
		
		tasks = new ArrayList<Task>();
	}
	
	public TaskList(ArrayList<Task> tasks) {
		
		this.tasks = tasks;
	}
	
	public int getNumNotCompleted() {
		
		int notCompletedNum = 0;
		for(Task t : tasks) {
			
			if(!t.isCompleted()) notCompletedNum++;
		}
		
		return notCompletedNum;
	}
	
	public void swapTask(Task a, Task b) throws Exception {
		
		int aIndex = tasks.indexOf(a);
		assert aIndex != -1;
		
		int bIndex = tasks.indexOf(b);
		assert aIndex != -1;
		
		tasks.set(aIndex, b);
		tasks.set(bIndex, a);
	}
}
