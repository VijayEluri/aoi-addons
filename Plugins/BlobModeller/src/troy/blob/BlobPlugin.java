/*
	Copyright (C) 2009 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	General Public License for more details.
*/


package troy.blob;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;

public class BlobPlugin implements ModellingTool
{
	private static int counter = 1;

	/**
	 * Creates a new object and sets undo records.
	 */
	@Override
	public void commandSelected(LayoutWindow theWindow)
	{
		Scene theScene = theWindow.getScene();
		String cstr = "Blob " + (counter++);

		Object3D blob = new Blob();

		UndoRecord undo = new UndoRecord(theWindow, false);
		undo.addCommandAtBeginning(UndoRecord.SET_SCENE_SELECTION,
				new Object[] { theWindow.getSelectedIndices() });
		theWindow.addObject(blob, new CoordinateSystem(), cstr, undo);
		theWindow.setUndoRecord(undo);

		theWindow.setSelection(theScene.getNumObjects() - 1);
		theWindow.updateImage();
	}

	@Override
	public String getName()
	{
		return "Add a new blob";
	}
}
