package net.cubex.tripPacking.items;

public class Task {
	
	public String name;
	private boolean completed;
	
	public Task(String name) {
		
		this.name = name;
	}
	
	public void complete() {
		
		completed = true;
	}
	
	public boolean isCompleted() {
		
		return completed;
	}
}
