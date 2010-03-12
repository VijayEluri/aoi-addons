/*
	Copyright (C) 2009 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	General Public License for more details.
*/


package troy.blob;

import artofillusion.math.*;

public abstract class Charge
{
	protected double x, y, z, w;
	public Charge(Vec3 pos, double q)
	{
		x = pos.x;
		y = pos.y;
		z = pos.z;
		w = q;
	}

	abstract public double getDist(double px, double py, double pz);
}
