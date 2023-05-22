package net.cubex.trippacker.items;

import java.util.Random;

public class Task {
	
	private static Random random = new Random();
	
	public String name;
	public String additionalNotes;
	public TaskList taskList;
	public final long id;
	private boolean completed;
	
	public Task(String name, String additionalNotes, TaskList taskList) {
		
		this.name = name;
		this.additionalNotes = additionalNotes;
		this.taskList = taskList;
		id = random.nextLong();
	}
	
	public void setCompleted(boolean completed) {
		
		this.completed = completed;
	}
	
	public boolean isCompleted() {
		
		return completed;
	}
	
	@Override
	public String toString() {
		
		return name + (completed ? " (Completed)" : "");
	}
}
