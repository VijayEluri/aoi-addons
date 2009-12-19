package troy.blob;

import artofillusion.math.*;

public class SphereCharge extends Charge
{
	public SphereCharge(Vec3 pos, double q)
	{
		super(pos, q);
	}

	public double getDist(double px, double py, double pz)
	{
		double dx = px - x;
		double dy = py - y;
		double dz = pz - z;

		return dx*dx + dy*dy + dz*dz;
	}
}
