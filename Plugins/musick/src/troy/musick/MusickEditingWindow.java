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

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.script.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * The editing window for a MusickObject
 * @author TroY
 */
public class MusickEditingWindow extends BDialog
{
	protected JTable table = null;
	protected ObjectInfo toEdit = null;

	public MusickEditingWindow(LayoutWindow parent, ObjectInfo info, Runnable callback)
	{
		super(parent, "Editing MusickController '" + info.getName() + "'", true);

		addEventLink(WindowClosingEvent.class, this, "cancel");

		// we're going to use a fancy jtable - sorry for this ugly mix of swing and bouy
		String[] columns = {"Object", "File", "Send Scope",
			"# Samples", "Channels", "Time offset"};

		// collect all objects in the scene
		//Object[][] data = new Object[parent.getScene().getNumObjects()][];
		Vector<Object[]> data = new Vector<Object[]>();

		// only the classes in this array are editable in the table
		Class[] editable = new Class[] {ScriptedObject.class};

		for (int i = 0; i < parent.getScene().getNumObjects(); i++)
		{
			boolean sendScope     = false;
			int     scopeLen      = 1470;
			String  channels      = new MusickObject.ControlledObject().getDefaultChannelString();
			double timeoffset     = 0.0;
			String  reader        = "";
			ObjectInfo thisoi     = parent.getScene().getObject(i);

			// filter
			if (doFilter(thisoi.getObject().getClass(), editable))
			{
				// ok, let's get the current status and cycle through it
				Vector<MusickObject.ControlledObject> targets
					= ((MusickObject)info.getObject()).getTargets();

				for (MusickObject.ControlledObject oneTarget : targets)
				{
					if (oneTarget.info.getId() == thisoi.getId())
					{
						// this object is already controlled, so let's get its settings
						sendScope     = oneTarget.sendScope;
						scopeLen      = oneTarget.scopeLen;
						timeoffset    = oneTarget.timeOffset;
						channels      = oneTarget.getStringForChannel(oneTarget.channels);
						reader        = oneTarget.reader.getFile();
					}
				}

				//data[i] = new Object[] {new ObjectInfoWrapper(thisoi), reader,
				data.add(new Object[] {new ObjectInfoWrapper(thisoi), reader,
					new Boolean(sendScope),
					new Integer(scopeLen), channels, new Double(timeoffset)});
			}
		}

		//System.out.println(data.size());

		// passing "editable" here is kind of redundant ...
		table = new JTable(new MEWTableModel(data, columns, editable));
		//table.setPreferredScrollableViewportSize(new Dimension(700, 70));
		table.setFillsViewportHeight(true);
		JScrollPane pane = new JScrollPane(table);

		// combo box - actually, the READER would have to build this one
		TableColumn channelC = table.getColumnModel().getColumn(4);
		JComboBox box = new JComboBox(MusickObject.ControlledObject.enumerateChannelStrings());
		channelC.setCellEditor(new DefaultCellEditor(box));

		// build buoy containers
		ColumnContainer ccMain  = new ColumnContainer();
		GridContainer gcButtons = new GridContainer(2, 1);
		
		BButton bOK = new BButton("OK");
		BButton bNo = new BButton("Cancel");

		gcButtons.add(bOK, 0, 0);
		gcButtons.add(bNo, 1, 0);

		ccMain.add(new AWTWidget(pane));
		ccMain.add(gcButtons);

		this.setContent(ccMain);

		pack();

		// add buoy links
		bOK.addEventLink(CommandEvent.class, this, "ok");
		bNo.addEventLink(CommandEvent.class, this, "cancel");

		// keep track of edited object
		toEdit = info;
	}

	protected boolean doFilter(Class c, Class[] valid)
	{
		for (Class ok : valid)
		{
			//System.out.println("CHECK: " + c.getName() + " VS " + ok.getName());
			if (c.equals(ok))
				return true;
		}

		return false;
	}

	/** Save new stuff! */
	protected void ok()
	{
		int rows = table.getRowCount();
		int cols = table.getColumnCount();

		for (int i = 0; i < rows; i++)
		{
			MusickObject.ControlledObject co = new MusickObject.ControlledObject();
			co.info = ((ObjectInfoWrapper)table.getValueAt(i, 0)).getItem();
			co.sendScope = (Boolean)table.getValueAt(i, 2);
			co.scopeLen = (Integer)table.getValueAt(i, 3);
			co.timeOffset = (Double)table.getValueAt(i, 5);

			String chanstr = (String)table.getValueAt(i, 4);
			co.channels = co.getChannelForString(chanstr);

			String path = (String)table.getValueAt(i, 1);
			co.reader = MusickReaderFactory.getReaderForFile(path);

			((MusickObject)toEdit.getObject()).updateOneTarget(co);
		}

		dispose();
	}

	protected void cancel()
	{
		dispose();
	}
}
