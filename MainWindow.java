package net.cubex.trippacker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.IconUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.cubex.trippacker.items.Container;
import net.cubex.trippacker.items.Item;

public final class MainWindow {
	
	private static JFrame frame;
	private static JMenuBar menuBar;
	
	private static JButton newItemButton;
    private static JLabel additionalItemNotesLabel;
    private static JScrollPane additionalItemNotesScrollPane;
    private static JTextArea additionalItemNotesTextArea;
    private static JLabel additionalTaskNotesLabel;
    private static JScrollPane additionalTaskNotesScrollPane;
    private static JTextArea additionalTaskNotesTextArea;
    private static JLabel containerLabel;
    private static JLabel containerNameField;
    private static JButton newContainerButton;
    public static JPanel itemEditPanel;
    private static JTextField itemNameField;
    private static JLabel itemNameLabel;
    private static JPanel itemPanel;
    private static JLabel itemTypeLabel;
    private static JPanel itemsPanel;
    private static JTree itemsTree;
    private static JButton removeItemButton;
    private static JButton removeTaskButton;
    private static JLabel sequenceLabel;
    private static JLabel sequenceNameField;
    private static JButton setContainerButton;
    private static JButton setTaskSequenceButton;
    private static JLabel taskLabel;
    private static JTextField taskNameField;
    private static JLabel taskNameLabel;
    public static JPanel tasksEditPanel;
    private static JTree tasksList;
    private static JPanel tasksListPanel;
    private static JTabbedPane tasksOrItemsTabbedPane;
    private static JPanel tasksPanel;
    private static JLabel tripPackerLabel;
    private static DefaultMutableTreeNode itemsTreeRoot;
    private static DefaultMutableTreeNode tasksTreeRoot;
    private static DefaultTreeModel itemsTreeModel;
    private static DefaultTreeModel tasksTreeModel;
	private static String fontName;
    
	private static boolean initialized;
	
	protected static void init() throws Exception {
		
		if(initialized) return;
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		JLabel testLabel = new JLabel();
		fontName = testLabel.getFont().getName();
		if(fontName == null) throw new Exception();
		
		frame = new JFrame("Trip Packer");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent event) {
				
				if(!TripPacker.fileSaved) {
					
					try {
						
						if(TripPacker.checkCurrentFileSaved()) {
							
							frame.dispose();
							System.exit(0);
						}
					} catch (IOException e) {
						
						e.printStackTrace();
						frame.dispose();
						System.exit(0);
					}
				} else {
					
					frame.dispose();
					System.exit(0);
				}
			}
		});
		
		menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					TripPacker.createNewFile();
				} catch (IOException ex) {

					ex.printStackTrace();
				}
			}
		});
		
		JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					if(TripPacker.checkCurrentFileSaved()) {
						
						JFileChooser fileChooser = new JFileChooser();
						
						String previousFilePath = Preferences.getPreference(Preferences.OPEN_PREVIOUS_FILE_PATH, System.getProperty("user.home"));
						fileChooser.setCurrentDirectory(new File(previousFilePath));
						fileChooser.setMultiSelectionEnabled(false);
						fileChooser.setAcceptAllFileFilterUsed(false);
						FileFilter filter = new FileNameExtensionFilter("JSON Files (*.json)", "json");
						fileChooser.addChoosableFileFilter(filter);
						fileChooser.setAcceptAllFileFilterUsed(true);
						
						int result = fileChooser.showOpenDialog(frame);
						if(result == JFileChooser.APPROVE_OPTION) {
							
							File f = fileChooser.getSelectedFile();
							String path = f.getParent();
							if(path == null) path = "";
							Preferences.setPreference(Preferences.OPEN_PREVIOUS_FILE_PATH, path);
							TripPacker.openFile(f);
						}
					}
				} catch (IOException ex) {

					ex.printStackTrace();
				}
			}
		});
		
		JMenuItem saveFileItem = new JMenuItem("Save", KeyEvent.VK_S);
		saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFileItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					TripPacker.saveFile();
				} catch (IOException ex) {
					
					ex.printStackTrace();
				}
			}
		});
		
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveFileItem);
		menuBar.add(fileMenu);
		
		JMenu tripMenu = new JMenu("Trip");
		tripMenu.setMnemonic(KeyEvent.VK_T);
		
		JMenuItem resetListItem = new JMenuItem("Reset Trip", KeyEvent.VK_R);
		resetListItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		resetListItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				int confirm = JOptionPane.showConfirmDialog(frame, "Reset trip?");
				if(confirm == JOptionPane.YES_OPTION) {
					
					TripPacker.resetCompletedStatuses();
				}
			}
		});
		
		tripMenu.add(resetListItem);
		menuBar.add(tripMenu);
		
		frame.setJMenuBar(menuBar);
		
		//ugly ui code (yay)
		
		tripPackerLabel = new JLabel();
        tasksOrItemsTabbedPane = new JTabbedPane();
        itemPanel = new JPanel();
        itemsPanel = new JPanel();
        itemsTree = new JTree();
        itemEditPanel = new JPanel();
        itemTypeLabel = new JLabel();
        itemNameLabel = new JLabel();
        itemNameField = new JTextField();
        containerLabel = new JLabel();
        containerNameField = new JLabel();
        additionalItemNotesLabel = new JLabel();
        additionalItemNotesScrollPane = new JScrollPane();
        additionalItemNotesTextArea = new JTextArea();
        removeItemButton = new JButton();
        setContainerButton = new JButton();
        tasksPanel = new JPanel();
        tasksListPanel = new JPanel();
        tasksList = new JTree();
        tasksEditPanel = new JPanel();
        taskLabel = new JLabel();
        taskNameLabel = new JLabel();
        taskNameField = new JTextField();
        sequenceLabel = new JLabel();
        sequenceNameField = new JLabel();
        additionalTaskNotesLabel = new JLabel();
        additionalTaskNotesScrollPane = new JScrollPane();
        additionalTaskNotesTextArea = new JTextArea();
        removeTaskButton = new JButton();
        setTaskSequenceButton = new JButton();
        newItemButton = new JButton();
        newContainerButton = new JButton();

        tripPackerLabel.setFont(new Font(fontName, 0, 24));
        tripPackerLabel.setText("Trip Packer");
        tripPackerLabel.setVerticalAlignment(SwingConstants.TOP);

        tasksOrItemsTabbedPane.setTabPlacement(JTabbedPane.LEFT);

        itemPanel.setLayout(new GridLayout(1, 2));

        itemsPanel.setLayout(new BorderLayout());

        itemsTreeRoot = new DefaultMutableTreeNode();
        itemsTreeModel = new DefaultTreeModel(itemsTreeRoot);
        itemsTree.setModel(itemsTreeModel);
        itemsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        itemsTree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) itemsTree.getLastSelectedPathComponent();
				
				if(node == null) return;
				else {

					if(node.getUserObject() instanceof Item) {
						
						Item item = (Item)node.getUserObject();
						TripPacker.currentItem = item;
						updateItemEditPanel();
						
						TripPacker.fileSaved = false;
						TripPacker.updateWindowTitle();
					} else System.out.println("this shouldn't happen");
				}
			}
		});
        
        itemsTree.setRootVisible(false);
		itemsTree.putClientProperty("JTree.collapsedIcon", new IconUIResource(new NodeIcon('+')));
		itemsTree.putClientProperty("JTree.expandedIcon",  new IconUIResource(new NodeIcon('-')));
        itemsPanel.add(itemsTree, BorderLayout.CENTER);

        itemPanel.add(itemsPanel);

        itemEditPanel.setBackground(new Color(226, 226, 226));
        itemEditPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        itemTypeLabel.setFont(new Font(fontName, 0, 18)); 
        itemTypeLabel.setText("Item Type");
        itemTypeLabel.setVerticalAlignment(SwingConstants.TOP);
        itemTypeLabel.setVerticalTextPosition(SwingConstants.TOP);

        itemNameLabel.setFont(new Font(fontName, 1, 12)); 
        itemNameLabel.setText("Name");

        itemNameField.addActionListener(new ActionListener() {
        	
            public void actionPerformed(ActionEvent evt) {
                
            	if(TripPacker.currentItem != null) {
            		
            		if(itemNameField.getText() != "" && itemNameField.getText() != null) {
	            		
	            		TripPacker.currentItem.name = itemNameField.getText();
	            		updateItemsTree();

						TripPacker.fileSaved = false;
						TripPacker.updateWindowTitle();
            		} else {
            			
            			itemNameField.setText("New Item");
            			TripPacker.currentItem.name = itemNameField.getText();
	            		updateItemsTree();

						TripPacker.fileSaved = false;
						TripPacker.updateWindowTitle();
            		}
            	}
            }
        });

        containerLabel.setFont(new Font(fontName, 1, 12)); 
        containerLabel.setText("Container");
        
        containerNameField.setText("Container");

        additionalItemNotesLabel.setFont(new Font(itemNameLabel.getFont().getName(), 1, 12)); 
        additionalItemNotesLabel.setText("Additional Notes");

        additionalItemNotesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        additionalItemNotesTextArea.setColumns(20);
        additionalItemNotesTextArea.setRows(5);
        additionalItemNotesTextArea.setFont(new Font(fontName, 0, 12));
        additionalItemNotesScrollPane.setViewportView(additionalItemNotesTextArea);

        removeItemButton.setText("Remove");
        removeItemButton.addActionListener(new ActionListener() {
        	
            public void actionPerformed(ActionEvent evt) {
                
            	if(TripPacker.currentItem instanceof Container) {
            		
            		if(JOptionPane.showConfirmDialog(frame, "Remove container? Everything inside will be uncategorized.") == JOptionPane.OK_OPTION) {
            			
            			if(TripPacker.currentItem.getContainer() == null) TripPacker.currentFile.uncategorizedItems.remove(TripPacker.currentItem);
	            		else TripPacker.currentItem.getContainer().removeItem(TripPacker.currentItem);
	            		
            			Container c = (Container)TripPacker.currentItem;
            			TripPacker.recursiveMakeItemsUncategorized(c);
            			
            			itemsTreeModel.removeNodeFromParent(TripPacker.currentItem.node);
	            		TripPacker.currentItem = null;
	            		
	            		updateItemEditPanel();
	            		updateItemsTree();

						TripPacker.fileSaved = false;
						TripPacker.updateWindowTitle();
            		}
            	} else {
            	
	            	if(JOptionPane.showConfirmDialog(frame, "Remove item?") == JOptionPane.OK_OPTION) {
	            		
	            		if(TripPacker.currentItem.getContainer() == null) TripPacker.currentFile.uncategorizedItems.remove(TripPacker.currentItem);
	            		else TripPacker.currentItem.getContainer().removeItem(TripPacker.currentItem);
	            		
	            		TripPacker.currentItem.node.removeFromParent();
	            		TripPacker.currentItem = null;
	            		
	            		updateItemEditPanel();
	            		updateItemsTree();

						TripPacker.fileSaved = false;
						TripPacker.updateWindowTitle();
	            	}
            	}
            }
        });

        setContainerButton.setText("Set Container");
        setContainerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	
            	if(TripPacker.currentItem == null) {
            		
            		//this shouldn't happen, but just in case
            		JOptionPane.showMessageDialog(frame, "Must select an item to set its container!");
            	} else {
            		
	            	Container[] containers = TripPacker.containers.toArray(new Container[TripPacker.containers.size()]);
	            	Container c = (Container)JOptionPane.showInputDialog(frame, "Select container:", "Select Container", JOptionPane.QUESTION_MESSAGE,
	            			null, containers, containers[0]);
	            	
	            	TripPacker.currentItem.setContainer(c);
	            	itemsTreeModel.removeNodeFromParent(TripPacker.currentItem.node);
	            	itemsTreeModel.insertNodeInto(TripPacker.currentItem.node, c.node, 0);
	            	
	            	updateItemsTree();
	            	updateItemEditPanel();

					TripPacker.fileSaved = false;
					TripPacker.updateWindowTitle();
            	}
            }
        });

        GroupLayout itemEditPanelLayout = new GroupLayout(itemEditPanel);
        itemEditPanel.setLayout(itemEditPanelLayout);
        itemEditPanelLayout.setHorizontalGroup(
            itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemEditPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(itemEditPanelLayout.createSequentialGroup()
                        .addComponent(itemNameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(itemNameField))
                    .addComponent(additionalItemNotesScrollPane, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .addGroup(itemEditPanelLayout.createSequentialGroup()
                        .addGroup(itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(itemTypeLabel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                            .addComponent(additionalItemNotesLabel)
                            .addGroup(itemEditPanelLayout.createSequentialGroup()
                                .addComponent(containerLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(containerNameField))
                            .addGroup(itemEditPanelLayout.createSequentialGroup()
                                .addComponent(setContainerButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeItemButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        
        itemEditPanelLayout.setVerticalGroup(
            itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemEditPanelLayout.createSequentialGroup()
                .addComponent(itemTypeLabel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(itemNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemNameLabel, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(containerLabel)
                    .addComponent(containerNameField))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalItemNotesLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalItemNotesScrollPane, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemEditPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(removeItemButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                    .addComponent(setContainerButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        itemPanel.add(itemEditPanel);

        tasksOrItemsTabbedPane.addTab("Items", itemPanel);

        tasksPanel.setLayout(new GridLayout(1, 2));

        tasksListPanel.setLayout(new BorderLayout());

        tasksTreeRoot = new DefaultMutableTreeNode();
        tasksTreeModel = new DefaultTreeModel(tasksTreeRoot);
        tasksList.setModel(tasksTreeModel);
        tasksList.setRootVisible(false);
		tasksList.putClientProperty("JTree.collapsedIcon", new IconUIResource(new NodeIcon('+')));
		tasksList.putClientProperty("JTree.expandedIcon",  new IconUIResource(new NodeIcon('-')));
        tasksListPanel.add(tasksList, BorderLayout.CENTER);

        tasksPanel.add(tasksListPanel);

        tasksEditPanel.setBackground(new Color(226, 226, 226));
        tasksEditPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        tasksEditPanel.setVisible(false);

        taskLabel.setFont(new Font(fontName, 0, 18)); 
        taskLabel.setText("Task");
        taskLabel.setVerticalAlignment(SwingConstants.TOP);
        taskLabel.setVerticalTextPosition(SwingConstants.TOP);

        taskNameLabel.setFont(new Font(fontName, 1, 12)); 
        taskNameLabel.setText("Name");

        taskNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //taskNameFieldActionPerformed(evt);
            }
        });

        sequenceLabel.setFont(new Font(fontName, 1, 12));
        sequenceLabel.setText("Sequence");

        sequenceNameField.setText("Sequence");

        additionalTaskNotesLabel.setFont(new Font(itemNameLabel.getFont().getName(), 1, 12)); 
        additionalTaskNotesLabel.setText("Additional Notes");

        additionalTaskNotesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        additionalTaskNotesTextArea.setColumns(20);
        additionalTaskNotesTextArea.setRows(5);
        additionalTaskNotesScrollPane.setViewportView(additionalTaskNotesTextArea);

        removeTaskButton.setText("Remove Task");
        removeTaskButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //removeTaskButtonActionPerformed(evt);
            }
        });

        setTaskSequenceButton.setText("Set Task Sequence");
        setTaskSequenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //setTaskSequenceButtonActionPerformed(evt);
            }
        });

        GroupLayout tasksEditPanelLayout = new GroupLayout(tasksEditPanel);
        tasksEditPanel.setLayout(tasksEditPanelLayout);
        tasksEditPanelLayout.setHorizontalGroup(
            tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tasksEditPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(tasksEditPanelLayout.createSequentialGroup()
                        .addComponent(taskNameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(taskNameField))
                    .addComponent(additionalTaskNotesScrollPane, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .addGroup(tasksEditPanelLayout.createSequentialGroup()
                        .addGroup(tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(taskLabel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                            .addComponent(additionalTaskNotesLabel)
                            .addGroup(tasksEditPanelLayout.createSequentialGroup()
                                .addComponent(sequenceLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sequenceNameField))
                            .addGroup(tasksEditPanelLayout.createSequentialGroup()
                                .addComponent(setTaskSequenceButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeTaskButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        
        tasksEditPanelLayout.setVerticalGroup(
            tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tasksEditPanelLayout.createSequentialGroup()
                .addComponent(taskLabel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(taskNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(taskNameLabel, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sequenceLabel)
                    .addComponent(sequenceNameField))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalTaskNotesLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalTaskNotesScrollPane, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tasksEditPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(removeTaskButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                    .addComponent(setTaskSequenceButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );
        
        tasksPanel.add(tasksEditPanel);
        
        tasksOrItemsTabbedPane.addTab("Tasks", tasksPanel);
        
        newItemButton.setText("New Item");
        newItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                
            	tasksOrItemsTabbedPane.setSelectedIndex(0);
            	DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            	Item newItem = new Item(node);
            	node.setUserObject(newItem);
            	itemsTreeModel.insertNodeInto(node, itemsTreeRoot, 0);
            	itemsTreeRoot.add(node);
            	itemsTree.setSelectionPath(new TreePath(node.getPath()));
            	TripPacker.currentItem = newItem;
            	
            	updateItemsTree();
            	updateItemEditPanel();

				TripPacker.fileSaved = false;
				TripPacker.updateWindowTitle();
            }
        });

        newContainerButton.setText("New Container");
        newContainerButton.addActionListener(new ActionListener() {
        	
            public void actionPerformed(ActionEvent evt) {
            	
            	tasksOrItemsTabbedPane.setSelectedIndex(0);
            	DefaultMutableTreeNode node = new DefaultMutableTreeNode();
            	Container newContainer = new Container(node);
            	node.setUserObject(newContainer);
            	itemsTreeModel.insertNodeInto(node, itemsTreeRoot, 0);
            	TripPacker.currentItem = newContainer;
            	TripPacker.containers.add(newContainer);
            	
            	updateItemsTree();
            	updateItemEditPanel();

				TripPacker.fileSaved = false;
				TripPacker.updateWindowTitle();
            }
        });

        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tripPackerLabel, GroupLayout.PREFERRED_SIZE, 310, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(newContainerButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newItemButton)
                .addContainerGap())
            .addComponent(tasksOrItemsTabbedPane)
        );
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(tripPackerLabel, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                           .addComponent(newContainerButton)
                            .addComponent(newItemButton))))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tasksOrItemsTabbedPane))
        );
		
        updateItemEditPanel();
        
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		
		TripPacker.updateWindowTitle();
		initialized = true;
	}
	
	public static void updateItemEditPanel() {
		
		if(TripPacker.currentItem == null) {
			
			itemTypeLabel.setText("No Item Selected");
			itemNameField.setText("");
			itemNameField.setEditable(false);
			containerNameField.setText("None");

			additionalItemNotesTextArea.setText("");
			additionalItemNotesTextArea.setEditable(false);

			setContainerButton.setEnabled(false);
			removeItemButton.setEnabled(false);
		} else {
			
			itemTypeLabel.setText(TripPacker.currentItem.getItemType());
			itemNameField.setText(TripPacker.currentItem.name);
			itemNameField.setEditable(true);
			
			if(TripPacker.currentItem.getContainer() == null) containerNameField.setText("None");
			else containerNameField.setText(TripPacker.currentItem.getContainer().name);
			
			additionalItemNotesTextArea.setText(TripPacker.currentItem.additionalNotes);
			additionalItemNotesTextArea.setEditable(true);
			
			setContainerButton.setEnabled(true);
			removeItemButton.setEnabled(true);
		}
	}
	
	public static void setWindowTitle(String title) {
		
		frame.setTitle(title);
	}
	
	protected static JFrame getFrame() {
		
		return frame;
	}
	
	public static DefaultMutableTreeNode getItemsTreeRoot() {
		
		return itemsTreeRoot;
	}
	
	public static DefaultMutableTreeNode getTasksTreeRoot() {
		
		return tasksTreeRoot;
	}
	
	public static void setItemsTreeRoot(DefaultMutableTreeNode root) {
		
		itemsTreeRoot = root;
	}
	
	public static void setTasksTreeRoot(DefaultMutableTreeNode root) {
		
		tasksTreeRoot = root;
	}
	
	public static DefaultTreeModel getItemsTreeModel() {
		
		return itemsTreeModel;
	}
	
	public static DefaultTreeModel getTasksTreeModel() {
		
		return tasksTreeModel;
	}
	
	public static JPanel getItemEditPanel() {
		
		return itemEditPanel;
	}
	
	public static JPanel getTaskEditPanel() {
		
		return tasksEditPanel;
	}
	
	public static void updateItemsTree() {
		
		itemsTreeModel.reload();
	}
	
	public static void updateTasksTree() {
		
		tasksTreeModel.reload();
	}
}
