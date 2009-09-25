/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.musick;

import artofillusion.object.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

/**
 * Implements an own table model in order to get a drop-down-chooser
 * @author TroY
 */
public class MEWTableModel extends AbstractTableModel
{
	public static final long serialVersionUID = 0L;

	private String[] columnNames = null;
	private Object[][] data = null;
	private Class[] editable = null;

	/** @param editable contains all classes which are editable */
	public MEWTableModel(Object[][] data, String[] columnNames, Class[] editable)
	{
		this.columnNames = columnNames;
		this.data = data;
		this.editable = editable;
	}

	public MEWTableModel(Vector<Object[]> data, String[] columnNames, Class[] editable)
	{
		this.columnNames = columnNames;
		this.data = data.toArray(new Object[1][]);
		this.editable = editable;
	}

	@Override
	public int getRowCount()
	{
		if (data == null)
			return 0;

		return data.length;
	}

	@Override
	public int getColumnCount()
	{
		if (columnNames == null)
			return 0;

		return columnNames.length;
	}

	@Override
	public String getColumnName(int col)
	{
		if (col >= columnNames.length)
			return "<invalid>";

		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		if (data == null)
			return null;

		if (row >= data.length || data[row] == null || col >= data[row].length)
			return null;

		return data[row][col];
	}

	@Override
	public void setValueAt(Object value, int row, int col)
	{
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	@Override
	public Class getColumnClass(int c)
	{
		Object o = getValueAt(0, c);
		if (o != null)
			return o.getClass();

		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col)
	{
		// first cell never editable
		if (col == 0)
			return false;

		for (Class a : editable)
		{
			Object thisraw = getValueAt(row, 0);
			if (thisraw instanceof ObjectInfoWrapper)		// just to be sure ...
			{
				ObjectInfoWrapper thiswrap = (ObjectInfoWrapper)thisraw; 
				Object3D thisobj = thiswrap.getItem().getObject();

				//System.out.println("Testing " + a + " against " + thisobj.getClass());

				if (a.equals(thisobj.getClass()))
					return true;
			}
		}

		return false;
	}
}
