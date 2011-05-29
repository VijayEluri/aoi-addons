/*
	Based on OBJTranslator.java, which is Copyright (C) 2002-2004 by Peter Eastman

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	General Public License for more details.
*/

package artofillusion.translators;

import artofillusion.*;
import artofillusion.ui.*;
import artofillusion.object.*;

import java.io.*;

import buoy.widget.*;

public class Sketch3DTranslator implements Translator
{
	public String getName()
	{
		return "Sketch3D (.sk)";
	}

	public boolean canImport()
	{
		return false;
	}

	public boolean canExport()
	{
		return true;
	}

	public void importFile(BFrame parent)
	{
	}

	public void exportFile(BFrame parent, Scene theScene)
	{
		// Export whole scene or just selected objects?
		BComboBox exportChoice = new BComboBox(new String [] {
				Translate.text("exportWholeScene"),
				Translate.text("selectedObjectsOnly")
		});
		ComponentsDialog dlg = new ComponentsDialog(parent,
				Translate.text("sketch3dtranslator:exportToSketch3D"),
				new Widget [] {exportChoice},
				new String [] {null});
		if (!dlg.clickedOk())
			return;

		// Select target file.
		BFileChooser fc = new BFileChooser(BFileChooser.SAVE_FILE,
				Translate.text("sketch3dtranslator:exportToSketch3D"));
		fc.setSelectedFile(new File("Untitled.sk"));
		if (ArtOfIllusion.getCurrentDirectory() != null)
			fc.setDirectory(new File(ArtOfIllusion.getCurrentDirectory()));
		if (!fc.showDialog(parent))
			return;
		File dir = fc.getDirectory();
		File f = fc.getSelectedFile();
		ArtOfIllusion.setCurrentDirectory(dir.getAbsolutePath());

		// Now export.
		if (!(parent instanceof LayoutWindow))
		{
			System.err.println("Something is terribly broken. "
					+ "'parent' is not a LayoutWindow.");
			return;
		}
		LayoutWindow layout = (LayoutWindow)parent;
		if (exportChoice.getSelectedIndex() == 0)
			Sketch3DExporter.exportObject(theScene.getAllObjects(), f);
		else
			Sketch3DExporter.exportObject(layout.getSelectedObjects(), f);
	}
}
