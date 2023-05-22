package net.cubex.trippacker.items;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

public class Container extends Item {
	
	private ArrayList<Item> items;
	
	public Container(DefaultMutableTreeNode node) {
		
		super("New Container", null, node);
		items = new ArrayList<Item>();
	}
	
	public Container(String name, Container container, DefaultMutableTreeNode node) {
		
		super(name, container, node);
		items = new ArrayList<Item>();
	}
	
	public ArrayList<Item> getItems() {
		
		return items;
	}
	
	public boolean removeItem(Item i) {
		
		boolean exists = items.remove(i);
		return exists;
	}
	
	public void addItem(Item i) {
		
		items.add(this);
	}
	
	@Override
	public String getItemType() {
		
		return "Container";
	}
}
