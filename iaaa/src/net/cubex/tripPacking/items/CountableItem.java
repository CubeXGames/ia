package net.cubex.tripPacking.items;

public class CountableItem extends Item {
	
	public int count;
	
	public CountableItem(String name, int count, Container container) {
		
		super(name, container);
		this.count = count;
	}
	
	@Override
	public String getItemName() {
		
		return "Item w/ Count";
	}
}
