package net.cubex.trippacker.items;

import net.cubex.trippacker.TripPacker;

import java.util.Random;

import javax.swing.tree.DefaultMutableTreeNode;

public class Item {
	
	private Random random = new Random();
	
	public String name;
	public String additionalNotes;
	private Container container;
	public transient DefaultMutableTreeNode node;
	public final long id;
	
	public Item(DefaultMutableTreeNode node) {
		
		name = "New Item";
		this.node = node;

		setContainer(container);
		id = random.nextLong();
	}
	
	public Item(String name, Container container, DefaultMutableTreeNode node) {
		
		this.name = name;
		this.node = node;

		setContainer(container);
		id = random.nextLong();
	}
	
	public String getItemType() {
		
		return "Item";
	}
	
	public Container getContainer() {
		
		return container;
	}
	
	/**
	 * Sets the container of an item and updates all the related references.
	 * @param container The container to put the item in. If container is null, the item will be uncategorized.
	 */
	public void setContainer(Container container) {
		
		if(this.container == container) return;
		
		if(this.container == null) {
			
			assert TripPacker.currentFile.uncategorizedItems.contains(this);
			TripPacker.currentFile.uncategorizedItems.remove(this);
		} else this.container.removeItem(this);
		
		if(container == null) TripPacker.currentFile.uncategorizedItems.add(this);
		else container.addItem(this);
		
		this.container = container;
	}
	
	@Override
	public String toString() {
		
		return name;
	}
}
