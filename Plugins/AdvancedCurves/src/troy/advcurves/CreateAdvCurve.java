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
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.widget.*;
import buoy.event.WidgetMouseEvent;

/**
 * The actual tool which creates an advanced curve.
 * @author TroY
 */
public class CreateAdvCurve extends EditingTool
{
	/** Let's count all created objects */
	private static int objectCounter = 1;
	
	/** Keep selected smoothing method */
	private int currentSmoothingMethod = Mesh.APPROXIMATING;

	/** Common constructor, set tool button icon */
	public CreateAdvCurve(EditingWindow parent)
	{
		super(parent);
		initButton("advcurves:createTool");
	}
	
	/** Return tool tip */
	@Override
	public String getToolTipText()
	{
		return Translate.text("advcurves:createAdvancedCurve.tipText");
	}
	
	/** What to do when the user activates the tool: Set the window's help text */
	@Override
	public void activate()
	{
		super.activate();
		theWindow.setHelpText(Translate.text("advcurves:createAdvancedCurve.helpText"));
	}
	
	/** Tell AoI which clicks we want to catch */
	@Override
	public int whichClicks()
	{
		return ALL_CLICKS;
	}
	
	/**
	 * A double click on the icon - let the user select some things
	 * (currently only smoothing method)
	 */
	@Override
	public void iconDoubleClicked()
	{
		BComboBox smoothingChoice = new BComboBox(new String [] {
			Translate.text("Angled"),
			Translate.text("Interpolating"),
			Translate.text("Approximating")
		});

		if (currentSmoothingMethod == Mesh.NO_SMOOTHING)
			smoothingChoice.setSelectedIndex(0);
		else if (currentSmoothingMethod == Mesh.INTERPOLATING)
			smoothingChoice.setSelectedIndex(1);
		else
			smoothingChoice.setSelectedIndex(2);

		ComponentsDialog dlg = new ComponentsDialog(theFrame,
			Translate.text("selectCurveSmoothing"),
			new Widget [] {smoothingChoice},
			new String [] {Translate.text("Smoothing Method")});

		if (!dlg.clickedOk())
			return;

		if (smoothingChoice.getSelectedIndex() == 0)
			currentSmoothingMethod = Mesh.NO_SMOOTHING;
		else if (smoothingChoice.getSelectedIndex() == 1)
			currentSmoothingMethod = Mesh.INTERPOLATING;
		else
			currentSmoothingMethod = Mesh.APPROXIMATING;
	}
	
	/** Get Smoothing Method */
	protected int getSmoothingMethod()
	{
		return currentSmoothingMethod;
	}
	
	/** Set Smoothing Method */
	protected void setSmoothingMethod(int sm)
	{
		currentSmoothingMethod = sm;
	}
	
	/** A click in the scene, let's create a new AdvCurve there */
	@Override
	public void mousePressed(WidgetMouseEvent e, ViewerCanvas view)
	{
		Scene theScene = theWindow.getScene();
		Camera cam  = view.getCamera();
		
		Vec3 origin = Snapper.snap(cam.convertScreenToWorld(e.getPoint(),
										Camera.DEFAULT_DISTANCE_TO_SCREEN), view);

		ObjectInfo toAdd = new ObjectInfo(new AdvCurve(currentSmoothingMethod), 
								new CoordinateSystem(origin, new Vec3(0.0, 0.0, 1.0),
								new Vec3(0.0, 1.0, 0.0)),
								"AdvCurve " + CreateAdvCurve.objectCounter);
		
		/*
		((LayoutWindow) theWindow).addObject(toAdd, null);
		((LayoutWindow) theWindow).setSelection(theWindow.getScene().getNumObjects()-1);
		((LayoutWindow) theWindow).setModified();
		((LayoutWindow) theWindow).updateImage();
		((LayoutWindow) theWindow).rebuildItemList();
		*/
		UndoRecord undo = new UndoRecord(theWindow, false);
		undo.addCommandAtBeginning(UndoRecord.SET_SCENE_SELECTION, new Object [] {((LayoutWindow) theWindow).getSelectedIndices()});
		((LayoutWindow) theWindow).addObject(toAdd, undo);
		theWindow.setUndoRecord(undo);
		((LayoutWindow) theWindow).setSelection(theScene.getNumObjects()-1);
		theWindow.updateImage();
		CreateAdvCurve.objectCounter++;
	}
}
