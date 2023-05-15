package ia;

import java.util.ArrayList;

public class TaskSequence {
	
	public ArrayList<Task> tasks;
	
	public TaskSequence() {
		
		tasks = new ArrayList<Task>();
	}
	
	public TaskSequence(ArrayList<Task> tasks) {
		
		this.tasks = tasks;
	}
	
	public int getNumNotCompleted() {
		
		int notCompletedNum = 0;
		for(Task t : tasks) {
			
			if(!t.completed) notCompletedNum++;
		}
		
		return notCompletedNum;
	}
	
	public void swap(Task a, Task b) throws Exception {
		
		int aIndex = tasks.indexOf(a);
		if(aIndex == -1) throw new Exception("this shouldn't happen");
		
		int bIndex = tasks.indexOf(b);
		if(bIndex == -1) throw new Exception("this shouldn't happen");
		
		tasks.set(aIndex, b);
		tasks.set(bIndex, a);
	}
}
