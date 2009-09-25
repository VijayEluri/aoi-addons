/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

/*
	Implements "Plugin"
	
	TroY, June 2008
 */


package troy.measure;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.widget.*;

/**
 * Manages palettes and menus
 * @author TroY
 */
public class MeasurePlugin implements Plugin
{
	/**
	 * Process messages sent to plugin by AoI (see AoI API description)
	 *
	 * @param  message  The message
	 * @param  args     Arguments depending on the message
	 */
	public void processMessage(int message, Object args[])
	{
		// AoI's main window
		if (message == Plugin.SCENE_WINDOW_CREATED)
		{
			// **************************************************************
			// add a palette tool item at the end
			LayoutWindow layout = (LayoutWindow)args[0];
			ToolPalette palette = layout.getToolPalette();
			addIcons(palette, layout);
			
			// **************************************************************
			// add menu items to "Object"
			BMenuItem menuItem = Translate.menuItem("measure:curveLength", new MenuHandler(layout), "doMeasureCurveLength");
			BMenuItem menuItem2 = Translate.menuItem("measure:curveLengthSmoothed", new MenuHandler(layout), "doMeasureCurveLengthSmoothed");
			
			// first: find "Object" menu ...
			BMenu objectMenu = layout.getObjectMenu();
			
			// If it has been found (should be), add the new entry
			if (objectMenu != null)
			{
				// add it before the first separator -> under "Convert to Actor..."
				int position = 0;
				boolean added = false;
				for (position = 0; !added && position < objectMenu.getChildCount(); position++)
				{
					MenuWidget mw = objectMenu.getChild(position);
					if (mw instanceof BSeparator)
					{
						objectMenu.add(menuItem2, position);
						objectMenu.add(menuItem, position);
						added = true;
					}
				}
			}
		}
		// ObjectEditorWindow
		else if (message == Plugin.OBJECT_WINDOW_CREATED)
		{
			// **************************************************************
			// add a palette tool item at the end
			ObjectEditorWindow oew = (ObjectEditorWindow)args[0];
			ToolPalette palette = oew.getToolPalette();
			addIcons(palette, oew);
		}
	}
	
	/** add all icons at the end of an arbitrary toolbar */
	protected void addIcons(ToolPalette palette, EditingWindow layout)
	{
		palette.addTool(palette.getNumTools(), new MeasureTool(layout));
		palette.addTool(palette.getNumTools(), new AngleTool(layout));
		palette.toggleDefaultTool();
		palette.toggleDefaultTool();
	}
}
