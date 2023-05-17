package net.cubex.tripPacking.items;

public class Item {
	
	public String name;
	public Container container;
	
	public Item() {
		
		name = "";
	}
	
	public Item(String name, Container container) {
		
		this.name = name;
		this.container = container;
	}
	
	public String getItemName() {
		
		return "Item";
	}
}
