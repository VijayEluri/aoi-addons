/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.measure;

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
	/** Measure the first selected curve */
	public void doMeasureCurveLength()
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
				measureCurve((Curve)obj, false);
				return;
			}
		}
	}
	
	/** Measure the first selected curve and take care about its smoothing */
	public void doMeasureCurveLengthSmoothed()
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
				measureCurve((Curve)obj, true);
				return;
			}
		}
	}
	
	private void measureCurve(Curve orig, boolean smoothed)
	{
		// ... get vertices and everything
		if (smoothed && orig.getSmoothingMethod() != Curve.NO_SMOOTHING)
			orig = orig.subdivideCurve();
		
		double len = 0.0;
		
		MeshVertex[] mv = orig.getVertices();
		
		// everything is alright here - even with 0-len or 1-len curves
		for (int i = 0; i < mv.length - 1; i++)
		{
			Vec3 a = mv[i].r;
			Vec3 b = mv[i+1].r;
			
			len += a.distance(b);
		}
		
		// update "help" text which displays the distance
		String fmt = Translate.text("measure:measureTool.lengthFormattedText");
		fmt = fmt.replaceAll("%d", Double.toString(len));
		
		theWindow.setHelpText(fmt);
	}
}