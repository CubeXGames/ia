package net.cubex.trippacker.items;

public class ItemMovementTask extends Task {
	
	public Item item;
	public Container targetContainer;
	
	public ItemMovementTask(String name, String additionalNotes, Item item, Container targetContainer, TaskList taskList) {
		
		super(name, additionalNotes, taskList);
		this.item = item;
	}
	
	@Override
	public void setCompleted(boolean completed) {
		
		if(completed) {
			
			item.setContainer(targetContainer);
		} else item.setContainer(null);
		
		super.setCompleted(completed);
	}
}
