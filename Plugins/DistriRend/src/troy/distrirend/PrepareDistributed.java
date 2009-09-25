/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.distrirend;

import artofillusion.Plugin;
import artofillusion.ModellingTool;
import artofillusion.LayoutWindow;

/**
 * Manages menu entries, dialogs, etc
 */
public class PrepareDistributed implements ModellingTool
{
	/**
	 * for ModellingTool: name of entry in the menu
	 */
	public String getName()
	{
		return "Distributed Rendering...";
	}
	/**
	 * for ModellingTool: what happens when menu item is clicked
	 */
	public void commandSelected(LayoutWindow lw)
	{
		/*
			Remark: I had some serious problems to keep the scene available.
			If held just in an instance of DistriDlg, it has been lost as the
			user clicked "Split up". The variable was set correctly at the
			beginning, but at the time of the click, it was just reset to
			"null". No matter what I've done.
			
			The only solution was to store the scene at this point and
			directly in a static var of DistriFuncs.
		*/
		System.out.println("We're talking about this scene: " + lw.getScene());
		DistriFuncs.setScene(lw.getScene());
		new DistriDlg(lw);
	}
}
