package net.cubex.tripPacking;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.attribute.BasicFileAttributeView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MainWindow {
	
	private static JFrame frame;
	private static JMenuBar menuBar;
	
	protected static void init() throws Exception {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		frame = new JFrame("IA Project");
		frame.setMinimumSize(new Dimension(178, 100));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new FlowLayout());
		
		menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				TripPacker.createNewFile();
			}
		});
		
		JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				
				String previousFilePath = Preferences.getPreference(Preferences.OPEN_PREVIOUS_FILE_PATH, System.getProperty("user.home"));
				fileChooser.setCurrentDirectory(new File(previousFilePath));
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileFilter(new FileNameExtensionFilter("XML Files", ".xml"));
				
				System.out.println(previousFilePath);
				
				int result = fileChooser.showOpenDialog(frame);
				if(result == JFileChooser.APPROVE_OPTION) {
					
					Preferences.setPreference(Preferences.OPEN_PREVIOUS_FILE_PATH, previousFilePath);
					File f = fileChooser.getSelectedFile();
					TripPacker.loadFile(f);
				}
			}
		});
		
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		menuBar.add(fileMenu);
		
		frame.setJMenuBar(menuBar);
		
		JLabel label = new JLabel("hi muffin");
		frame.add(label);
		
		JButton button = new JButton();
		button.setText("boop");
		button.setBounds(40, 40, 100, 30);
		
		frame.add(button);
		
		frame.pack();
		frame.setVisible(true);
	}
}
