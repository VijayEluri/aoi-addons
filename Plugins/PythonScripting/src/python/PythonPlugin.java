/*
	Copyright (C) 2010 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	General Public License for more details.
*/


package python;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import buoy.widget.*;

import javax.script.*;
import java.awt.*;
import java.util.*;

public class PythonPlugin implements ModellingTool
{
	private static int counter = 1;

	/**
	 * See if you can find an engine that handles "python" or "jython".
	 */
	public boolean pythonAvailable()
	{
		java.util.List<ScriptEngineFactory> fac =
			new ScriptEngineManager().getEngineFactories();

		for (ScriptEngineFactory f : fac)
		{
			for (String name : f.getNames())
			{
				name = name.toLowerCase();
				if (name.equals("python") || name.equals("jython"))
					return true;
			}
		}

		return false;
	}

	/**
	 * Creates a new object and sets undo records.
	 */
	@Override
	public void commandSelected(LayoutWindow theWindow)
	{
		if (!pythonAvailable())
		{
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					String msg = "Python Scripting support could not be found.\n";
					msg += "Are you sure that \"jython.jar\" is in your classpath?";
					new BStandardDialog("Error", msg,
						BStandardDialog.ERROR).showMessageDialog(null);
				}
			});
			return;
		}

		Scene theScene = theWindow.getScene();

		String cstr = "Python " + (counter++);

		Object3D nobj = new PythonScript("");

		UndoRecord undo = new UndoRecord(theWindow, false);
		undo.addCommandAtBeginning(UndoRecord.SET_SCENE_SELECTION,
				new Object[] { theWindow.getSelectedIndices() });
		theWindow.addObject(nobj, new CoordinateSystem(), cstr, undo);
		theWindow.setUndoRecord(undo);

		theWindow.setSelection(theScene.getNumObjects() - 1);
		theWindow.updateImage();
	}

	@Override
	public String getName()
	{
		return "Add a scripted object (Python)";
	}
}

/* vim: set ts=2 sw=2 : */
