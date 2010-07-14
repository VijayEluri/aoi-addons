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
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import javax.script.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class PythonPlugin implements Plugin
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

	/**
	 * Execute a script (click on menu item).
	 */
	public void executeScriptCommand(CommandEvent ev)
	{
		// TODO: Replace dummy.
		System.out.println(ev.getActionCommand());
	}

	/**
	 * Recursively add python scripts in the tool-scripts-directory.
	 */
	public void addScriptsToMenu(BMenu menu, File dir)
	{
		String files[] = dir.list();
		if (files == null)
			return;
		Arrays.sort(files, Collator.getInstance(Translate.getLocale()));
		for (String file : files)
		{
			File f = new File(dir, file);
			if (f.isDirectory())
			{
				BMenu m = new BMenu(file);
				menu.add(m);
				addScriptsToMenu(m, f);
			}
			else if (file.endsWith(".py") && file.length() > 3)
			{
				BMenuItem item = new BMenuItem(file.substring(0, file.length() - 3));
				item.setActionCommand(f.getAbsolutePath());
				item.addEventLink(CommandEvent.class, this, "executeScriptCommand");
				menu.add(item);
			}
		}
	}

	/**
	 * Add menu items.
	 */
	@Override
	public void processMessage(int message, Object args[])
	{
		if (message == Plugin.SCENE_WINDOW_CREATED
				&& args[0] instanceof LayoutWindow)
		{
			// Add "Create scripted object (Python) ..." to new layout
			// windows. TODO: Translate.
			final LayoutWindow layout = (LayoutWindow)args[0];
			final PythonPlugin me = this;
			BMenu tools = layout.getToolsMenu();
			BMenuItem menuItem = new BMenuItem("Create Scripted Object (Python)...");
			menuItem.setActionCommand("newpythonobjectscript");
			menuItem.addEventLink(CommandEvent.class,
					new Object() {
						public void doClick()
						{
							me.commandSelected(layout);
						}
					}, "doClick");

			// Add tool scripts. TODO: Translate.
			BMenu scripts = new BMenu("Scripts (Python)");
			File dir = new File(ArtOfIllusion.TOOL_SCRIPT_DIRECTORY);
			addScriptsToMenu(scripts, dir);

			// Add both of them.
			for (int p = 0; p < tools.getChildCount(); p++)
			{
				MenuWidget mw = tools.getChild(p);
				if (mw instanceof BSeparator)
				{
					tools.add(scripts, p + 1);
					tools.add(menuItem, p + 1);
					break;
				}
			}
		}
	}
}

/* vim: set ts=2 sw=2 : */
