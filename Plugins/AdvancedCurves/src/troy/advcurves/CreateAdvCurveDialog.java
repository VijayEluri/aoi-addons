/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.advcurves;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import javax.swing.BorderFactory;
import java.awt.Insets;

/**
 * Options dialog for CreateAdvCurve
 */
public class CreateAdvCurveDialog extends BDialog
{
	private CreateAdvCurve callbackObject = null;
	
	BComboBox smList = null;

	/**
	 * This constructor is called by a double click on the icon
	 */
	public CreateAdvCurveDialog(CreateAdvCurve callbackObject, LayoutWindow parent)
	{
		super(parent, Translate.text("advcurves:createToolDialog.title"), true); // modal
		
		this.callbackObject = callbackObject;
		
		LayoutInfo growInsets = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(10, 10, 10, 10), null);
		LayoutInfo growButton = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(0, 10, 5, 10), null);
		
		// where everything sits in
		ColumnContainer main  = new ColumnContainer();
		
		
		// *** SMOOTHING METHOD ***
		smList = new BComboBox();
		GridContainer gcSM = new GridContainer(2, 1);
		
		smList.add(new SMInfos(Translate.text("advcurves:createToolDialog.smoothingNone"), Mesh.NO_SMOOTHING));
		smList.add(new SMInfos(Translate.text("advcurves:createToolDialog.smoothingInter"), Mesh.INTERPOLATING));
		smList.add(new SMInfos(Translate.text("advcurves:createToolDialog.smoothingApprox"), Mesh.APPROXIMATING));
		
		Object selected = SMInfos.getObjectForID(callbackObject.getSmoothingMethod());
		if (selected != null)
			smList.setSelectedValue(selected);
		
		gcSM.add(new BLabel(Translate.text("advcurves:createToolDialog.smoothingLabel")), 0, 0, growInsets);
		gcSM.add(smList, 1, 0, growInsets);
		
		main.add(gcSM);
		// *** --- ***
		
		
		// *** BUTTONS AND WINDOW EVENTS ***
		GridContainer buttons = new GridContainer(2, 1);
		BButton bOK = new BButton("OK");
		BButton bCancel = new BButton("Cancel");
		
		bOK.addEventLink(CommandEvent.class, this, "clickOK");
		bCancel.addEventLink(CommandEvent.class, this, "clickCancel");
		
		addEventLink(WindowClosingEvent.class, this, "clickCancel");
		
		buttons.add(bOK, 0, 0, growButton);
		buttons.add(bCancel, 1, 0, growButton);
		
		main.add(buttons);
		// *** --- ***
		
		
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
		SMInfos.flushList();
		dispose();
	}
	
	/**
	 * click on ok button: save settings and get lost
	 */
	private void clickOK()
	{		
		Object listObject = smList.getSelectedValue();
		if (listObject instanceof SMInfos)
		{
			callbackObject.setSmoothingMethod(((SMInfos)listObject).getID());
		}
		
		SMInfos.flushList();
		dispose();
	}
}
