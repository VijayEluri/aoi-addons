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
import artofillusion.ui.*;
import buoy.widget.*;

/**
 * Introducing the new tool icon
 * @author TroY
 */
public class MusickPlugin implements Plugin
{
	@Override
	public void processMessage(int message, Object args[])
	{
		// New Main Window created
		if (message == Plugin.SCENE_WINDOW_CREATED)
		{
			LayoutWindow layout = (LayoutWindow)args[0];
			ToolPalette palette = layout.getToolPalette();
			addIcons(palette, layout);
		}
	}

	protected void addIcons(ToolPalette p, EditingWindow w)
	{
		// Add Tool
		p.addTool(p.getNumTools(), new MusickTool(w));
		p.toggleDefaultTool();
		p.toggleDefaultTool();
	}
}
