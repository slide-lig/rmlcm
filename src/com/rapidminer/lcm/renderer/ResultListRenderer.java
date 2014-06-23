package com.rapidminer.lcm.renderer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.lcm.obj.ResultListIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.container.MultidimensionalArraySet;

public class ResultListRenderer extends AbstractTableModelTableRenderer{

	private int length=0;
	
	@Override
	public String getName() {
		return "ResultListIOObject";
	}

	@Override
	public TableModel getTableModel(Object renderable, IOContainer ioContainer,
			boolean isReporting) {

		//final List<Pair<Integer, String>> values = new ArrayList<Pair<Integer, String>>();
		final List<MultidimensionalArraySet<Integer>> values = new ArrayList<MultidimensionalArraySet<Integer>>();
		
		
		if (renderable instanceof ResultListIOObject) {
			ResultListIOObject object = (ResultListIOObject) renderable;
//			for (Integer key : object.getHashmap().keySet()) {
//				values.add(new MultidimensionalArraySet<>(dimensions));
//			}

			for (int[] array : object.getResultlist()) {
				values.add(new MultidimensionalArraySet<Integer>(array));
				if(array.length>length){
					length = array.length;
				}
				//values.add(new MultidimensionalArraySet<>(array));
			}
			
			return new AbstractTableModel() {

				/**
			 * 
			 */
				private static final long serialVersionUID = 1L;

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					//Pair<Integer, String> pair = values.get(rowIndex);
					MultidimensionalArraySet<Integer> multidimensionalArraySet = values.get(rowIndex);
					//if (columnIndex == 0)
						//return pair.getFirst();
						//return multidimensionalArraySet.get(columnIndex);
					//return pair.getSecond();
					return multidimensionalArraySet.get(columnIndex);
				}

				@Override
				public int getColumnCount() {
					return length;
				}

				@Override
				public int getRowCount() {
					return values.size();
				}

				@Override
				public String getColumnName(int column) {
					if (column == 0)
						return "Support";
					return "item_"+column;
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
