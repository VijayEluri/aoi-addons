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
import artofillusion.ui.*;
import artofillusion.object.*;
import artofillusion.math.*;

import java.util.*;

/**
 * This will handle all menu commands - as long as they're few, no methods
 * will be "outsourced" ;>
 * @author TroY
 */
public class MenuHandler
{
	/** Reference to parent window */
	private EditingWindow theWindow = null;
	
	/** Common constructor */
	public MenuHandler(EditingWindow w)
	{
		theWindow = w;
	}
	
	// --------------------------------------------------------------------
	// -- Main Window --
	/** Try to convert all selected objects */
	public void doConvert()
	{
		if (!(theWindow instanceof LayoutWindow))
			return;
		
		Collection<ObjectInfo> selection = ((LayoutWindow)theWindow).getSelectedObjects();
		for (ObjectInfo oi : selection)
		{
			Object3D obj = oi.getObject();

			// If this is a curve ...
			if (obj instanceof Curve)
			{
				// ... get vertices and everything
				Curve orig = (Curve)obj;
				
				Vec3[] vecs = new Vec3[orig.getVertices().length];
				for (int i = 0; i < vecs.length; i++)
				{
					vecs[i] = orig.getVertices()[i].r;
				}
				
				
				String addition = "";
				Object3D out = null;
				// now, if it's even an advanced curve, convert it down
				if (obj instanceof AdvCurve)
				{
					out = new Curve(vecs, orig.getSmoothness(),
										orig.getSmoothingMethod(), orig.isClosed());
					addition = " (regular)";
				}
				// just to have it consequent ;=) and maybe there will be some other types later
				else if (obj instanceof Curve)
				{
					out = new AdvCurve(vecs, orig.getSmoothness(),
										orig.getSmoothingMethod(), orig.isClosed());
					addition = " (advanced)";
				}
				

				if (out != null)
				{
					((LayoutWindow)theWindow).addObject(out, oi.getCoords().duplicate(), oi.getName() + addition, null);
				}
			}
		}
		theWindow.updateImage();
	}
	
	// --------------------------------------------------------------------
	// -- Editing Window --
	/** Align selected vertices on grid */
	public void doAlignOnGrid()
	{
		if (!(theWindow instanceof CurveEditorWindow))
			return;
		
		ObjectInfo   oi         = ((CurveEditorWindow)theWindow).getObject();
		Object3D     obj        = oi.getObject();
		MeshVertex[] v          = ((Curve)oi.getObject()).getVertices();
		boolean      selected[] = ((CurveEditorWindow)theWindow).getSelection();
		ViewerCanvas view       = theWindow.getView();
			
		if (view == null)
			return;
		
		// undo record!
		theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, new Object [] {obj, obj.duplicate()}));

		for (int i = 0; i < selected.length; i++)
		{
			if (selected[i])
			{
				Snapper.snap(v[i].r, view);
			}
		}
		
		((CurveEditorWindow)theWindow).objectChanged();
		theWindow.updateImage();
	}
	
	/** Revert the vertices order */
	public void doRevertVertexOrder()
	{
		if (!(theWindow instanceof CurveEditorWindow))
			return;
		
		ObjectInfo   oi  = ((CurveEditorWindow)theWindow).getObject();
		Object3D     obj = oi.getObject();
		MeshVertex[] v   = ((Curve)obj).getVertices();
		float[]      s   = ((Curve)obj).getSmoothness();
		
		// undo record!
		theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, new Object [] {obj, obj.duplicate()}));
		
		// Just get all vertices and their smoothness-value, create new
		// arrays and save them in reversed order ... that's it
		
		Vec3[] revV  = new Vec3[v.length];
		float[] revS = new float[s.length];
		
		int i = v.length - 1;
		int t = 0;
		while (i >= 0)
		{
			revV[t] = v[i].r;
			revS[t] = s[i];
			
			i--;
			t++;
		}
		
		((Curve)obj).setShape(revV, revS);
		
		((CurveEditorWindow)theWindow).objectChanged();
		theWindow.updateImage();
	}
}
