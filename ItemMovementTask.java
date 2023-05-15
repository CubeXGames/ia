package ia;

public class ItemMovementTask extends Task {
	
	public Item itemToBeMoved;
	public Container targetContainer;
	
	public ItemMovementTask(String name, Item itemToBeMoved, Container targetContainer) {
		
		super(name);
		this.itemToBeMoved = itemToBeMoved;
		this.targetContainer = targetContainer;
	}
	
	@Override
	public void complete() {
		
		itemToBeMoved.container.items.remove(itemToBeMoved);
		itemToBeMoved.container = targetContainer;
		targetContainer.items.add(itemToBeMoved);
	}
}
