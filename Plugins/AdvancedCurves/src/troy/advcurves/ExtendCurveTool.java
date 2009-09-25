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
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import java.awt.*;
import java.util.*;

/**
 * Add new points at the end or beginning of a curve
 * @author TroY
 */
public class ExtendCurveTool extends MeshEditingTool
{
	/** Common constructor, init tool button */
	public ExtendCurveTool(EditingWindow fr, MeshEditController controller)
	{
		super(fr, controller);
		initButton("advcurves:extendCurve");
	}
	
	/** Tell AoI which clicks we want to catch */
	@Override
	public int whichClicks()
	{
		return ALL_CLICKS;
	}
	
	/** This tool is able to change the current selection - depending on the situation */
	@Override
	public boolean allowSelectionChanges()
	{
		boolean[] selected = controller.getSelection();
		int       selCount = 0;
		
		// Count selected vertices
		for (boolean one : selected)
		{
			if (one)
				selCount++;
		}
		
		// Only first or last selected? Then do not deselect in order
		// to allow consecutive extension at the end or beginning.
		if (selCount == 1 && (selected[0] || selected[selected.length - 1]))
			return false;
			
		// Otherwise, there are 2 or more selected vertices or one single
		// vertices somewhere on the curve is selected.
		
		// This applies to:
		// -  2 or more vertices selected --> must be able to change selection
		// -  sth. special: 2 neighboured vertices selected. Now, a new
		//                  vertex is most prolly inserted. After *that*,
		//                  we want to lose the selection anyway.
		
		// So, the selection is only kept alive if the user is about
		// to extened the curve at the beginning or end. In any other
		// case, it's intended to lose/change the selection.
		
		return true;
	}
	
	/** What to do when the user activates the tool: Set the window's help text */
	@Override
	public void activate()
	{
		super.activate();
		theWindow.setHelpText(Translate.text("advcurves:extendCurveTool.helpText"));
	}
	
	/** Return tool tip */
	@Override
	public String getToolTipText()
	{
		return Translate.text("advcurves:extendCurveTool.tipText");
	}
	
	/** User has clicked somewhere in the scene - we are to extend the curve now */
	@Override
	public void mousePressed(WidgetMouseEvent e, ViewerCanvas view)
	{
		boolean[] selected = controller.getSelection();
		Curve     obj      = (controller.getObject().getObject() instanceof Curve ? (Curve)(controller.getObject().getObject()) : null);
		
		// working on a "non-null" curve
		if (obj != null && obj.getVertices().length > 0)
		{
			int selCount = 0;
			Vector<Integer> which = new Vector<Integer>();
			for (int i = 0; i < selected.length; i++)
			{
				if (selected[i])
				{
					selCount++;
					which.add(i);
				}
			}
			
			// beginning or end
			if (selCount == 1 && (selected[0] || selected[selected.length - 1]))
			{
				// undo record!
				theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, new Object [] {obj, obj.duplicate()}));
				
				// use the camera to transform screen coordinates to world coordinates
				Camera cam = view.getCamera();
				
				// calc distance from cam to the new point - we're doing it the
				// same way as it's done with bones
				// first, geht the depth of the currently selected point ...
				double distance = cam.getWorldToView().timesZ(obj.getVertices()[which.get(0)].r);
				// ... then look for the clicked point in this depth
				Vec3 newPoint = Snapper.snap(cam.convertScreenToWorld(e.getPoint(), distance), view);
				
				MeshVertex[] origV = obj.getVertices();
				float[] origF = obj.getSmoothness();
				
				Vec3[]  v = new  Vec3[origV.length + 1];
				float[] f = new float[origF.length + 1];
				
				boolean[] s = new boolean[v.length];
				
				// extended at the end:
				if (selected[selected.length - 1])
				{
				
					for (int i = 0; i < origV.length; i++)
					{
						v[i] = origV[i].r;
						f[i] = origF[i];
						s[i] = false;
					}
					
					v[v.length - 1] = newPoint;
					f[f.length - 1] = 1.0f;
					s[s.length - 1] = true;
				
				}
				// extended at the beginning:
				else
				{
					for (int i = 0; i < origV.length; i++)
					{
						v[i + 1] = origV[i].r;
						f[i + 1] = origF[i];
						s[i + 1] = false;
					}
					
					v[0] = newPoint;
					f[0] = 1.0f;
					s[0] = true;
				}
				
				// update object and editor window:
				obj.setShape(v, f);
				((CurveEditorWindow)controller).setSelection(s);
				controller.objectChanged();
				
				//System.out.println("Extension done (" + obj + "). " + v.length + "," + f.length + "," + s.length);
			}
			// in the middle of the curve - see above comments for most commands here
			// only allow insertion between first/last if there are only 2 vertices on the curve
			else if (selCount == 2 && (!(selected[0] && selected[selected.length - 1]) || selected.length == 2))
			{
				// we have to make sure that the selected vertices are neighbours.
				if (Math.abs(which.get(0) - which.get(1)) == 1)
				{
					theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, new Object [] {obj, obj.duplicate()}));

					Camera cam = view.getCamera();
					
					double distance0 = cam.getWorldToView().timesZ(obj.getVertices()[which.get(0)].r);
					double distance1 = cam.getWorldToView().timesZ(obj.getVertices()[which.get(1)].r);
					double distance = (distance0 + distance1) / 2.0;
					
					Vec3 newPoint = Snapper.snap(cam.convertScreenToWorld(e.getPoint(), distance), view);
					
					// new point found, now insert it
					MeshVertex[] origV = obj.getVertices();
					float[] origF = obj.getSmoothness();
					
					Vec3[]  v = new  Vec3[origV.length + 1];
					float[] f = new float[origF.length + 1];
					
					boolean[] s = new boolean[v.length];
					
					int npos = 0;
					int i    = 0;
					while (i < origV.length)
					{
						v[npos] = origV[i].r;
						f[npos] = origF[i];
						s[npos] = false;
						
						if (i == which.get(0))
						{
							npos++;
							v[npos] = newPoint;
							f[npos] = 1.0f;
							s[npos] = true;
						}
						
						i++;
						npos++;
					}
					
					// update object and editor window:
					obj.setShape(v, f);
					((CurveEditorWindow)controller).setSelection(s);
					controller.objectChanged();
				}
			}
		}
		// this is a null-length curve
		else if (obj != null && obj.getVertices().length == 0)
		{
			Camera cam = view.getCamera();
			
			theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, new Object [] {obj, obj.duplicate()}));
			
			// create a new point at the default distance
			Vec3 npoint = Snapper.snap(cam.convertScreenToWorld(e.getPoint(),
								Camera.DEFAULT_DISTANCE_TO_SCREEN), view);
			
			Vec3[]  v = new  Vec3[] {npoint};
			float[] f = new float[] {1.0f};
			
			// update object and editor window:
			obj.setShape(v, f);
			((CurveEditorWindow)controller).setSelection(new boolean[] {true});
			controller.objectChanged();
		}
	}
}
