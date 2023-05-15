package ia;

public class Task {
	
	public String name;
	public boolean completed;
	
	public Task(String name) {
		
		this.name = name;
	}
	
	public void complete() {
		
		completed = true;
	}
}
