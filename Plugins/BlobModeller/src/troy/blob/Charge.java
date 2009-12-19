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
