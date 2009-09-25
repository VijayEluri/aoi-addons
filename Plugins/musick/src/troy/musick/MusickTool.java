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
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

/**
 * Class which manages adding of new music controllers
 * @author TroY
 */
public class MusickTool extends EditingTool
{
	public static int objectCounter = 1;

	/** Common constructor, set tool button icon */
	public MusickTool(EditingWindow parent)
	{
		super(parent);
		initButton("musick:musick");
	}
	
	/** Return tool tip */
	@Override
	public String getToolTipText()
	{
		return Translate.text("musick:musick.toolTipText");
	}
	
	/** What to do when the user activates the tool: Set the window's help text */
	@Override
	public void activate()
	{
		super.activate();
		theWindow.setHelpText(Translate.text("musick:musick.toolHelpText"));
	}
	
	/** Tell AoI which clicks we want to catch */
	@Override
	public int whichClicks()
	{
		return ALL_CLICKS;
	}
	
	/** Mouse click - add a new controller */
	@Override
	public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view) 
	{
		Scene theScene = theWindow.getScene();
		Camera cam  = view.getCamera();
		
		// The objects position and all this stuff doesn't matter, it's never drawn
		Vec3 origin = new Vec3(0.0, 0.0, 0.0);

		ObjectInfo toAdd = new ObjectInfo(new MusickObject(), 
								new CoordinateSystem(origin, new Vec3(0.0, 0.0, 1.0),
								new Vec3(0.0, 1.0, 0.0)),
								"MusickController " + MusickTool.objectCounter++);
		
		// all the undo-record-stuff
		// -------------------------
		UndoRecord undo = new UndoRecord(theWindow, false);
		undo.addCommandAtBeginning(UndoRecord.SET_SCENE_SELECTION, new Object [] {((LayoutWindow) theWindow).getSelectedIndices()});
		((LayoutWindow) theWindow).addObject(toAdd, undo);
		theWindow.setUndoRecord(undo);
		((LayoutWindow) theWindow).setSelection(theScene.getNumObjects()-1);
		theWindow.updateImage();
		// -------------------------
	}
}
