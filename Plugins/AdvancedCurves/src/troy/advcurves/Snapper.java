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

/**
 * This class handles all snapping used in Advanced Curves
 * @author TroY
 */
public class Snapper
{
	/** 
	 * Align a vector according to grid settings
	 * @param v The vector you want to snap
	 * @param view Viewer canvas from where to read grid settings
	 * @return Although snapping is applied to the vector <i>directly</i>, it
	 *         is returned, too, to make some other operations possible.
	 */
	public static Vec3 snap(Vec3 v, ViewerCanvas view)
	{
		if (!view.getSnapToGrid())
			return v;
		
		
		double gridSpacing = view.getGridSpacing();
		int numDecs = view.getSnapToSubdivisions();
		
		v.x = MathHelper.round(v.x / gridSpacing, numDecs, true) * gridSpacing;
		v.y = MathHelper.round(v.y / gridSpacing, numDecs, true) * gridSpacing;
		v.z = MathHelper.round(v.z / gridSpacing, numDecs, true) * gridSpacing;
		
		return v;
	}
}
