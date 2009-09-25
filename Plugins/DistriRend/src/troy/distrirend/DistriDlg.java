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

import artofillusion.LayoutWindow;
import artofillusion.ui.UIUtilities;
import artofillusion.Scene;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SceneCamera;
import buoy.event.CommandEvent;
import buoy.event.WindowClosingEvent;
import buoy.widget.ColumnContainer;
import buoy.widget.GridContainer;
import buoy.widget.LayoutInfo;
import buoy.widget.BDialog;
import buoy.widget.BButton;
//import buoy.widget.BCheckBox;
import buoy.widget.BComboBox;
import buoy.widget.BTextField;
import buoy.widget.BLabel;
import buoy.widget.BorderContainer;
import javax.swing.BorderFactory;
import java.awt.Insets;

/**
 * Options dialog for AutoBackup
 */
public class DistriDlg extends BDialog
{
	private BTextField targetDir = new BTextField();
	
	private BComboBox existingCams = new BComboBox();
	
	private BButton bOK = new BButton("Split up");
	private BButton bCancel = new BButton("Cancel");

	/**
	 * This constructor is called by PrepareDistributed.commandSelected(...) (user clicks on menu entry)
	 */
	public DistriDlg(LayoutWindow parent)
	{
		super(parent, "Distributed Rendering", true); // modal
		
		LayoutInfo grow       = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.HORIZONTAL, null, null);
		LayoutInfo growInsets = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(10, 10, 10, 10), null);
		LayoutInfo growCenter = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, null, null);
		
		// where everything sits in
		ColumnContainer main  = new ColumnContainer();
		// gc for the text-field and label
		GridContainer texts   = new GridContainer(2, 1);
		// gc for the combobox and label
		GridContainer camsel  = new GridContainer(2, 1);
		// gc for the two buttons
		GridContainer buttons = new GridContainer(2, 1);

		// label and textfield go into first gc
		texts.add(new BLabel("Target directory:"), 0, 0, growInsets);
		texts.add(targetDir, 1, 0, growInsets);
		
		// camera selection
		int max = parent.getScene().getNumObjects();
		for (int i = 0; i < max; i++)
		{
			ObjectInfo one = parent.getScene().getObject(i);
			if (one.object instanceof SceneCamera)
			{
				existingCams.add(new CamInfos(one, i));
			}
		}
		
		camsel.add(new BLabel("Camera:"), 0, 0, growInsets);
		camsel.add(existingCams, 1, 0, growInsets);
		
		// short "HALP!" text
		main.add(new BLabel("Split the scene into two separate files for distributed rendering."), growInsets);
		main.add(new BLabel("These files will be named \"multi-0.aoi\" and \"multi-1.aoi\"."), growInsets);
		//main.add(new BLabel("which can be rendered simultaneously on two PCs"), growInsets);
		
		main.add(camsel, growCenter);
		main.add(texts, growCenter);
		
		// the two buttons go into our second gc
		buttons.add(bOK, 0, 0, growInsets);
		buttons.add(bCancel, 1, 0, growInsets);
		
		main.add(buttons, growCenter);
		
		// restore targetDir - if already set.
		String tmp = DistriFuncs.getTargetDir();
		tmp = (tmp.equals("") ? parent.getScene().getDirectory() : tmp);
		targetDir.setText(tmp);
		
		
		// add some events ..
		bOK.addEventLink(CommandEvent.class, this, "clickOK");
		bCancel.addEventLink(CommandEvent.class, this, "clickCancel");
		
		addEventLink(WindowClosingEvent.class, this, "clickCancel");
		
		this.setContent(main);
		this.pack();

		UIUtilities.centerDialog(this, parent);
		
		this.setResizable(false);
		this.setVisible(true);
	}
	
	/**
	 * click on cancel button, also called by WindowClosingEvent. dismiss everything.
	 */
	private void clickCancel()
	{
		DistriFuncs.setTargetDir(targetDir.getText());
		
		dispose();
	}
	
	/**
	 * click on ok button
	 */
	private void clickOK()
	throws Exception
	{
		DistriFuncs.setTargetDir(targetDir.getText());
		DistriFuncs.distribute(((CamInfos)existingCams.getSelectedValue()).getObjectInfo());
		
		dispose();
	}
}
