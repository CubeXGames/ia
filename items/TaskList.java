package net.cubex.trippacker.items;

import java.util.ArrayList;

public class TaskList {
	
	public static final String[] DEFAULT_TASK_SEQUENCE = { "At Start of Packing", "Day Before",
			"Night Before", "Morning Of", "Immediately Before" };
	
	public String sequence;
	public ArrayList<Task> tasks;
	
	public TaskList() {
		
		tasks = new ArrayList<Task>();
		sequence = "";
	}
	
	public TaskList(String sequence, ArrayList<Task> tasks) {
		
		this.sequence = sequence;
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
	
	@Override
	public String toString() {
		
		return sequence;
	}
}
