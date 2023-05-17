package net.cubex.tripPacking.items;

import java.util.ArrayList;

public class Container extends Item {
	
	public static Container baseContainer;
	
	private ArrayList<Item> items;
	private ArrayList<Container> subContainers;
	
	public Container() {
		
		super("", null);
	}
	
	public Container(String name, Container container) {
		
		super(name, container);
		items = new ArrayList<Item>();
		subContainers = new ArrayList<Container>();
	}
	
	public ArrayList<Item> getItems() {
		
		return items;
	}
	
	public ArrayList<Container> getSubContainers() {
		
		return subContainers;
	}
	
	public void setItemContainer(Item i) {
		
		items.add(i);
		
		if(i.container != null) i.container.items.remove(i);
		i.container = this;
		
		if(i instanceof Container) {
			
			Container c = (Container)i;
			subContainers.add(c);
			if(i.container != null) c.subContainers.remove(c);
		}
	}
	
	public boolean removeItem(Item i) {
		
		boolean exists = items.remove(i);
		if(i instanceof Container) subContainers.remove((Container)i);
		
		return exists;
	}
	
	@Override
	public String getItemName() {
		
		return "Container";
	}
}
