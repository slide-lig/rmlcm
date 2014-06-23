package com.rapidminer.lcm.renderer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.lcm.obj.IdentifyHashMapIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.container.Pair;

public class IdentifyHashMapRenderer extends AbstractTableModelTableRenderer {

	@Override
	public String getName() {
		return "identifyHashMap";
	}

	@Override
	public TableModel getTableModel(Object renderable, IOContainer ioContainer,
			boolean isReporting) {

		final List<Pair<Integer, String>> values = new ArrayList<Pair<Integer, String>>();

		if (renderable instanceof IdentifyHashMapIOObject) {
			IdentifyHashMapIOObject object = (IdentifyHashMapIOObject) renderable;
			for (Integer key : object.getHashmap().keySet()) {
				values.add(new Pair<Integer, String>(key, object.getHashmap()
						.get(key)));
			}

			return new AbstractTableModel() {

				/**
			 * 
			 */
				private static final long serialVersionUID = 1L;

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					Pair<Integer, String> pair = values.get(rowIndex);
					if (columnIndex == 0)
						return pair.getFirst();
					return pair.getSecond();
				}

				@Override
				public int getColumnCount() {
					return 2;
				}

				@Override
				public int getRowCount() {
					return values.size();
				}

				@Override
				public String getColumnName(int column) {
					if (column == 0)
						return "No.";
					return "Item";
				}
			};
		}
		return new DefaultTableModel();
	}

	@Override
	public boolean isSortable() {
		return true;
	}
}
