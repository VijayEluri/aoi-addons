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
import artofillusion.texture.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;

/**
 * This class alters the existing CurveEditorWindow
 * @author TroY
 */
public class AdvCurveEditorWindow extends CurveEditorWindow
{
	/** Are we able to add/remove vertices? */
	private boolean topology = false;
	
	/** Constructor which adds new tools and sets grid settings */
	public AdvCurveEditorWindow(EditingWindow parent, String title,
					ObjectInfo obj, Runnable onClose, boolean allowTopology)
	{
		super(parent, title, obj, onClose, allowTopology);

		this.topology = allowTopology;

		// *** Grid settings
		Scene sc = parent.getScene();
		for (int i = 0; i < theView.length; i++)
		{
			MeshViewer view = (MeshViewer) theView[i];
			view.setScene(sc, obj);
			view.setGrid(sc.getGridSpacing(), sc.getGridSubdivisions(),
							sc.getShowGrid(), sc.getSnapToGrid());
		}
		
		// *** Add the new tool
		if (topology)
		{
			ToolPalette tools = getToolPalette();
			if (tools != null)
			{
				ExtendCurveTool exCu = new ExtendCurveTool(this, this);
				tools.addTool(exCu);
				
				// Ya, I don't like typecasts without being *really* sure. :=) mostly...
				if (obj.getObject() instanceof Curve)
				{
					// If this curve has less than 2 vertices, select the
					// extension tool by default
					if (((Curve)obj.getObject()).getVertices().length < 2)
					{
						tools.selectTool(exCu);
						
						// a bit special: one single point? then select it.
						if (((Curve)obj.getObject()).getVertices().length == 1)
						{
							setSelection(new boolean[] {true});
						}
					}
				}
			}
		}
		
		// *** Modify menu
		BMenuItem miAlign  = Translate.menuItem("advcurves:alignVerticesToGrid", new MenuHandler(this), "doAlignOnGrid");
		BMenuItem miRevert = Translate.menuItem("advcurves:revertVertexOrder", new MenuHandler(this), "doRevertVertexOrder");
		
		// add it before the first separator -> under "Center Curve"
		int position = 0;
		boolean added = false;
		for (position = 0; !added && position < meshMenu.getChildCount(); position++)
		{
			MenuWidget mw = meshMenu.getChild(position);
			if (mw instanceof BSeparator)
			{
				if (topology)
					meshMenu.add(miRevert, position);
				meshMenu.add(miAlign, position);
				added = true;
			}
		}
	}

	/** Show right mouse context menu */
	protected void showContextMenu(WidgetMouseEvent e)
	{
		// Copy the common "Curve" menu to a popup menu
		BPopupMenu popper = createPopupMenu();
		
		if (popper == null)
			return;
		
		popper.show(e.getWidget(), e.getX(), e.getY());
	}
	
	/** Create right mouse context menu */
	protected BPopupMenu createPopupMenu()
	{
		// Sadly, I'm not able to duplicate a whole BMenu. So we have to
		// create a very own one...
		
		Curve obj = null;
		Object3D obj3d = getObject().getObject();
		if (obj3d instanceof Curve)
		{
			obj = (Curve)obj3d;
		}
		else
		{
			return null;
		}
		
		BPopupMenu popper = new BPopupMenu();
		
		if (topology)
		{
			popper.add(Translate.menuItem("deletePoints", this, "deleteCommand"));
			popper.add(Translate.menuItem("subdivide", this, "subdivideCommand"));
		}
		
		popper.add(Translate.menuItem("editPoints", this, "setPointsCommand"));
		popper.add(Translate.menuItem("transformPoints", this, "transformPointsCommand"));
		popper.add(Translate.menuItem("randomize", this, "randomizeCommand"));
		popper.add(Translate.menuItem("centerCurve", this, "centerCommand"));
		popper.add(Translate.menuItem("advcurves:alignVerticesToGrid", new MenuHandler(this), "doAlignOnGrid"));
		if (topology)
			popper.add(Translate.menuItem("advcurves:revertVertexOrder", new MenuHandler(this), "doRevertVertexOrder"));
		popper.addSeparator();
		popper.add(Translate.menuItem("smoothness", this, "setSmoothnessCommand"));
		
		BMenu smoothMenu = null;
		popper.add(smoothMenu = Translate.menu("smoothingMethod"));
		smoothItem = new BCheckBoxMenuItem [3];
		smoothMenu.add(smoothItem[0] = Translate.checkboxMenuItem("none", this, "smoothingChanged", obj.getSmoothingMethod() == Curve.NO_SMOOTHING));
		smoothMenu.add(smoothItem[1] = Translate.checkboxMenuItem("interpolating", this, "smoothingChanged", obj.getSmoothingMethod() == Curve.INTERPOLATING));
		smoothMenu.add(smoothItem[2] = Translate.checkboxMenuItem("approximating", this, "smoothingChanged", obj.getSmoothingMethod() == Curve.APPROXIMATING));
		
		BMenuItem closeAndOpen = null;
		popper.add(closeAndOpen = Translate.menuItem("closedEnds", this, "toggleClosedCommand"));
		if (obj.isClosed())
			closeAndOpen.setText(Translate.text("menu.openEnds"));
		
		return popper;
	}
	
	/**
	 * Overriden/simplified delete command which allows to curve to get empty again
	 */
	@Override
	public void deleteCommand()
	{
		if (!topology)
			return;
		
		boolean[] selected = getSelection();
		Curve theCurve = (Curve) objInfo.getObject();
		MeshVertex vt[] = theCurve.getVertices();
		float oldS[] = theCurve.getSmoothness();
		int num = 0, i = 0, j = 0;
		
		// count selected vertices
		for (boolean one : selected)
		{
			if (one)
			{
				num++;
			}
		}
		
		if (num == 0)
			return;
		
		// delete any selected vertex - regardless of how many will be left
		Vec3[] v = new Vec3 [vt.length-num];
		float[] news = new float [vt.length-num];
		boolean[] newsel = new boolean [vt.length-num];
		
		setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, new Object [] {theCurve, theCurve.duplicate()}));
		
		for (i = 0, j = 0; i < vt.length; i++)
		{
			if (!selected[i])
			{
				newsel[j] = false;
				news[j]   = oldS[i];
				v[j]      = vt[i].r;
				j++;
			}
		}
		
		theCurve.setShape(v, news);
		setSelection(newsel);
	}
}
