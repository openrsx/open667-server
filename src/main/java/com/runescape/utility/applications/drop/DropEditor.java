package com.runescape.utility.applications.drop;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.world.entity.npc.Drop;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;
import com.runescape.utility.external.gson.resource.NPCData;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tyler
 */
public class DropEditor extends JFrame {
	
	/**
	 * Creates new form DropEditor
	 */
	public DropEditor() {
		long start = System.currentTimeMillis();
		npcData = getSortedMap(NPCDataLoader.getAllNPCData());
		System.out.println("Loaded npc data in " + (System.currentTimeMillis() - start) + " ms");
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	private void initComponents() {
		jScrollPane1 = new JScrollPane();
		npcList = new JList();
		jScrollPane2 = new JScrollPane();
		dropTable = new JTable();
		frameMenuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		editMenu = new JMenu("Edit");

		save = new JButton("Save");
		newDrops = new JButton("New");

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		npcList.addListSelectionListener(event -> {
			if (npcList.getSelectedIndex() != -1) {
				updateTable(npcList.getSelectedIndex());
			}
		});

		newDrops.addActionListener(listener -> {
			String inputDialog = JOptionPane.showInputDialog("Enter the id of the new npc you wish to add.");
			if (inputDialog == null) {
				return;
			}
			int npcId = Integer.parseInt(inputDialog);
			String npcName = NPCDefinitions.getNPCDefinitions(npcId).getName();
			if (npcData.get(npcName) != null) {
				JOptionPane.showMessageDialog(this, "There is already some drops for this npc: " + npcName);
				return;
			}
			npcData.put(npcName, new NPCData());
			npcData = getSortedMap(npcData);
			populateNPCList();
			modifiedNPCs.add(npcName);
		});

		save.addActionListener(listener -> {
			if (modifiedNPCs.size() == 0 && deletedNPCS.size() == 0) {
				JOptionPane.showMessageDialog(this, "You haven't modified anything.");
				return;
			}
			java.util.List<String> used = new ArrayList<>();
			for (Iterator<String> it$ = modifiedNPCs.iterator(); it$.hasNext(); ) {
				String name = it$.next();
				if (used.contains(name)) {
					continue;
				}
				NPCData data = npcData.get(name);
				NPCDataLoader.saveData(name, data);
				used.add(name);
				System.out.println("Saved and removed changes applied to: " + name);
				it$.remove();
			}
			for (Iterator<String> it$ = deletedNPCS.iterator(); it$.hasNext();) {
				String name = it$.next();
				if (used.contains(name)) {
					continue;
				}
				NPCDataLoader.deleteDataFile(name);
				used.add(name);
				it$.remove();
				System.out.println("Deleted npc: " + name);
			}
		});

		jScrollPane1.setViewportView(npcList);
		
		dropTable.setModel(new DefaultTableModel(new Object[][] { { }, { }, { }, { } }, new String[] { }));
		jScrollPane2.setViewportView(dropTable);

		frameMenuBar.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		frameMenuBar.add(fileMenu);
		frameMenuBar.add(editMenu);
		frameMenuBar.add(newDrops);
		frameMenuBar.add(save);
		setJMenuBar(frameMenuBar);

		populateNPCList();
		addTableMouseListener();
		addNPCListMouseListener();

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE).addGap(2, 2, 2).addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane1).addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE));
		
		pack();
	}

	/**
	 * This method populates the {@link #npcList} with all the data in the {@link #npcData} map
	 */
	private void populateNPCList() {
		AbstractListModel<String> model = new AbstractListModel<String>() {

			@Override
			public int getSize() {
				return npcData.size();
			}

			@Override
			public String getElementAt(int index) {
				Object[] keys = npcData.keySet().toArray();
				return (String) keys[index];
			}
		};
		npcList.setModel(model);
	}

	/**
	 * This method updates the {@link #dropTable} with data from the current selected value
	 *
	 * @param selectedIndex
	 * 		The current selected index
	 */
	private void updateTable(int selectedIndex) {
		NPCData data = getDataByIndex(selectedIndex);
		if (data == null) {
			System.out.println("No data at index:" + selectedIndex);
			return;
		}
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 0;
			}
		};

		model.addColumn("Name");
		model.addColumn("Id");
		model.addColumn("Chance");
		model.addColumn("Amount");

		for (Drop drop : data.getDrops()) {
			int itemId = drop.getItemId();
			model.addRow(new Object[] { ItemDefinitions.getItemDefinitions(itemId).getName(), itemId, drop.getRate(), drop.getMinAmount() + " - " + drop.getMaxAmount() });
		}

		dropTable.setModel(model);
		setTableModelListener();
		//dropTable.scrollRectToVisible(dropTable.getCellRect(dropTable.getRowCount() - 1, dropTable.getColumnCount(), true));
	}

	/**
	 * Gets the data at a certain index
	 *
	 * @param index
	 * 		The index
	 */
	private NPCData getDataByIndex(int index) {
		NPCData[] valueArray = npcData.values().toArray(new NPCData[npcData.values().size()]);
		return valueArray[index];
	}

	/**
	 * This method sets the model listener. This is called every time an item is selected in the {@link #npcList}
	 */
	private void setTableModelListener() {
		dropTable.getModel().addTableModelListener(e -> {
			if (e.getType() == TableModelEvent.UPDATE) {
				String selectedName = npcList.getSelectedValue().toString();
				if (selectedName == null) {
					System.out.println("No selected name.");
					return;
				}
				NPCData selectedData = getSelectedNPCData();
				if (selectedData == null) {
					System.out.println("Data selected was invalid.");
					return;
				}
				Drop drop = selectedData.getDrops().get(e.getFirstRow());
				if (drop == null) {
					System.out.println("No selected drop.");
					return;
				}
				String valueChangedTo = dropTable.getModel().getValueAt(e.getFirstRow(), e.getColumn()).toString();
				if (e.getColumn() == 1) {
					drop.setItemId(Integer.parseInt(valueChangedTo));
				} else if (e.getColumn() == 2) {
					drop.setRate(Double.parseDouble(valueChangedTo));
				} else if (e.getColumn() == 3) {
					String[] split = valueChangedTo.split(" - ");
					drop.setMinAmount(Integer.parseInt(split[0]));
					drop.setMaxAmount(Integer.parseInt(split[1]));
				}
				updateTable(npcList.getSelectedIndex());
				modifiedNPCs.add(selectedName);
			}
		});
	}

	/**
	 * Converts the unsorted map into a map sorted by its key
	 *
	 * @param unsortedMap
	 * 		The unsorted map we will sort
	 */
	public Map<String, NPCData> getSortedMap(Map<String, NPCData> unsortedMap) {
		return new TreeMap<>(unsortedMap);
	}

	private void addNPCListMouseListener() {
		npcList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int row = npcList.locationToIndex(e.getPoint());
					npcList.setSelectedIndex(row);
					JPopupMenu popup = new JPopupMenu();
					JMenuItem delete = new JMenuItem("Delete");

					delete.addActionListener(listener -> {
						Object selectedValue = npcList.getSelectedValue();
						if (selectedValue == null) {
							System.out.println("No selected value.");
							return;
						}
						String selectedName = selectedValue.toString();
						NPCData removed = npcData.remove(selectedName);

						deletedNPCS.add(selectedName);
						populateNPCList();
						System.out.println(removed == null ? "Didn't remove anything." : "Removed npc data for " + selectedName + " successfully.");
					});

					popup.add(delete);
					popup.show(e.getComponent(), e.getX(), e.getY());
					System.out.println("row=" + row);
				}
			}
		});
	}


	/**
	 * This method adds a mouse listener to the {@link #dropTable} for deleting rows in it
	 */
	private void addTableMouseListener() {
		dropTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem add = new JMenuItem("Add");
				add.addActionListener(listener -> {
					String selectedName = npcList.getSelectedValue().toString();
					NPCData npcData = getSelectedNPCData();
					if (selectedName == null || npcData == null) {
						System.out.println("Invalid data selected.");
						return;
					}
					addDefaultDrop(npcData);
					updateTable(npcList.getSelectedIndex());
					modifiedNPCs.add(selectedName);
				});
				popup.add(add);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		dropTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int row = dropTable.rowAtPoint(e.getPoint());
				if (row >= 0 && row < dropTable.getRowCount()) {
					dropTable.setRowSelectionInterval(row, row);
				} else {
					dropTable.clearSelection();
				}
				int selectedRow = dropTable.getSelectedRow();
				if (selectedRow < 0) {
					return;
				}
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem delete = new JMenuItem("Delete");

					delete.addActionListener(listener -> {
						int selectedItemRow = dropTable.getSelectedRow();
						String selectedName = npcList.getSelectedValue().toString();
						NPCData npcData = getSelectedNPCData();
						if (selectedName == null || npcData == null) {
							System.out.println("Invalid data selected.");
							return;
						}
						Drop drop = getDropAtIndex(npcData, selectedItemRow);
						if (drop == null) {
							System.out.println("No drop...");
							return;
						}
						removeDrop(npcData, drop);
						updateTable(npcList.getSelectedIndex());
						modifiedNPCs.add(selectedName);
					});

					popup.add(delete);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	/**
	 * Adds a default drop to the {@code NPCData}
	 *
	 * @param data
	 * 		The data to add a drop to
	 */
	private void addDefaultDrop(NPCData data) {
		data.getDrops().add(new Drop(1, 100, 1, 1));
	}

	/**
	 * Removes the drop from the npc data
	 *
	 * @param data
	 * 		The data
	 * @param drop
	 * 		The drop to remove
	 */
	private void removeDrop(NPCData data, Drop drop) {
		String selectedValue = npcList.getSelectedValue().toString();
		data.getDrops().remove(drop);
		npcData.put(selectedValue, data);
	}

	/**
	 * Gets the {@code NPCData} {@code Object} of the selected value
	 */
	private NPCData getSelectedNPCData() {
		String selectedValue = npcList.getSelectedValue().toString();
		return npcData.get(selectedValue);
	}

	/***
	 * Gets a drop at the index in a npc data
	 *
	 * @param data
	 * 		The data
	 * @param dropIndex
	 * 		The index
	 */
	private Drop getDropAtIndex(NPCData data, int dropIndex) {
		if (data == null) {
			return null;
		}
		return data.getDrops().get(dropIndex);
	}

	/**
	 * @param args
	 * 		the command line arguments
	 */
	public static void main(String args[]) {
		try {
			Cache.init();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			EventQueue.invokeLater(() -> new DropEditor().setVisible(true));
		} catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}

	private java.util.List<String> deletedNPCS = new ArrayList<>();

	private java.util.List<String> modifiedNPCs = new ArrayList<>();
	
	private JList npcList;
	
	private JMenu fileMenu;
	
	private JMenu editMenu;
	
	private JMenuBar frameMenuBar;

	private JButton newDrops;

	private JButton save;
	
	private JScrollPane jScrollPane1;
	
	private JScrollPane jScrollPane2;

	private JTable dropTable;

	private Map<String, NPCData> npcData;
}