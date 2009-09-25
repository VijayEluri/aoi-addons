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
	
	TroY, April 2008
 */


package troy.advcurves;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.widget.*;

/**
 * Manages palette and menus
 * @author TroY
 */
public class AdvCurvesPlugin implements Plugin
{
	/**
	 * Process messages sent to plugin by AoI (see AoI API description)
	 *
	 * @param  message  The message
	 * @param  args     Arguments depending on the message
	 */
	public void processMessage(int message, Object args[])
	{
		if (message == Plugin.SCENE_WINDOW_CREATED)
		{
			// **************************************************************
			// add a palette tool item at the end
			LayoutWindow layout = (LayoutWindow)args[0];
			ToolPalette palette = layout.getToolPalette();
			palette.addTool(palette.getNumTools(), new CreateAdvCurve(layout));
			palette.toggleDefaultTool();
			palette.toggleDefaultTool();
			
			// **************************************************************
			// add menu items to "Object"
			BMenuItem menuItem = Translate.menuItem("advcurves:curveConversion", new MenuHandler(layout), "doConvert");
			
			// first: get the "Object" menu ...
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
						objectMenu.add(menuItem, position);
						added = true;
					}
				}
			}
			
			layout.layoutChildren();
		}
	}
}
