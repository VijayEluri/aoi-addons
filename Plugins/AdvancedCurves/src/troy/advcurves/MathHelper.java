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
 * Some mathematical methods used rather often ...
 * @author TroY
 */
public class MathHelper
{
	/**
	 * Chooses an arbitrary vector that is orthogonal to this particular one 
	 * @param a Reference vector
	 * @return Orthogonal vector
	 */
	public static Vec3 randomOrtho(Vec3 a)
	{
		// For two orthogonal vectors, the dot product has to be 0.
		// So, find a vector which fullfills this condition. As a division
		// by 0 is rather unwanted, distinguish these three cases:
		
		if (Math.abs(a.x) > 1e-16)			// "a.x != 0.0"
		{
			return new Vec3((- a.y - a.z) / a.x, 1.0, 1.0);
		}
		else if (Math.abs(a.y) > 1e-16)		// "a.y != 0.0"
		{
			return new Vec3(1.0, (- a.x - a.z) / a.y, 1.0);
		}
		else if (Math.abs(a.z) > 1e-16)		// "a.z != 0.0"
		{
			return new Vec3(1.0, 1.0, (- a.x - a.y) / a.z);
		}

		return new Vec3(0.0, 0.0, 0.0);
	}

	/**
	 * Normalize the vector, then scale it to the given length
	 * @param a Vector to modify (it IS modified!)
	 * @param len target length
	 * @return To allow cascading, the vector is returned, too
	 */
	public static Vec3 toLen(Vec3 a, double len)
	{
		a.normalize();
		a.scale(len);
		return a;
	}
	
	/**
	 * Round a number to xyz decimal values ...
	 * @param num Which number to round
	 * @param numDecs Round to which amount of decimals 
	 * @param rawFactor Interpret numDecs as the real factor
	 * @return rounded value
	 */
	public static double round(double num, int numDecs, boolean rawFactor)
	{
		double ex = numDecs;
		
		if (!rawFactor)
			ex = Math.pow(10, numDecs);
			
		return Math.round(num * ex) / ex;
	}
	
	/**
	 * Round a number to xyz decimal values ... rawFactor always <b>false</b>
	 * @param num Which number to round
	 * @param numDecs Round to which amount of decimals 
	 * @return rounded value
	 */
	public static double round(double num, int numDecs)
	{
		return round(num, numDecs, false);
	}
}
