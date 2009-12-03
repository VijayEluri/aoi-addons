package troy.blob;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.object.*;
import artofillusion.math.*;

import java.util.*;

public class Blob extends ImplicitObject
{
	public class Charge
	{
		public double x, y, z, w;
		public Charge(double a, double b, double c, double d)
		{
			x = a; y = b; z = c; w = d;
		}
	}

	// Properites
	private static final Property[] PROPERTIES = {
		new Property("Cutoff", 0, Double.MAX_VALUE, 1),
		new Property("Hardness", 1, Double.MAX_VALUE, 1)
	};

	private double cutoff = 1.0;
	private double hardness = 1.0;
	private ArrayList<Charge> charges = new ArrayList<Charge>();

	static BoundingBox   cachedBounds = null;
	static WireframeMesh cachedWire   = null;

	public Blob()
	{
		charges.add(new Charge(-0.75, 0, 0, 0.5));
		charges.add(new Charge(0.75, 0, 0, 0.5));
		charges.add(new Charge(0.0, 0.5, 0.0, 0.25));
	}

	@Override
	public double getFieldValue(double x, double y, double z,
			double size, double time)
	{
		double sum = 0.0, d1, d2, d3;
		double val;

		for (Charge c : charges)
		{
			// Distances.
			d1 = (c.x - x);
			d2 = (c.y - y);
			d3 = (c.z - z);

			// Calculate value.
			val = (c.w*c.w) / (d1*d1 + d2*d2 + d3*d3);

			// This charge's "hardness".
			val = Math.pow(val, hardness);

			// Prevent division by zero.
			if (val < 1e-10)
				continue;

			// Additive or subtractive charge.
			if (c.w >= 0.0)
				sum += val;
			else
				sum -= val;
		}

		return sum;
	}

	@Override
	public double getCutoff()
	{
		return cutoff;
	}

	@Override
	public boolean getPreferDirectRendering()
	{
		return true;
	}

	@Override
	public BoundingBox getBounds()
	{
		// If there are no charges, return an empty box.
		if (charges.size() <= 0)
			return new BoundingBox(0, 0, 0, 0, 0, 0);

		if (cachedBounds == null)
		{
			// Start with the box of the first charge.
			Charge c = charges.get(0);
			double ext = Math.abs(c.w);
			double corr = 1.0 + (0.1 / hardness);  // see note
			cachedBounds = new BoundingBox(
				new Vec3(c.x - ext, c.y - ext, c.z - ext).times(corr),
				new Vec3(c.x + ext, c.y + ext, c.z + ext).times(corr));

			// A note on "corr":
			// As blobs are blobby, we can't use their weight/radius
			// directly. This would result in unwanted cut-off's. To
			// correct this, we extend the bounding box of a charge "a
			// bit". That "bit" can be smaller if the charge is harder.

			// Iteratively add the remaining charges.
			for (int i = 1; i < charges.size(); i++)
			{
				c = charges.get(i);
				ext = Math.abs(c.w);
				corr = 1.0 + (0.1 / hardness);
				cachedBounds.extend(new BoundingBox(
					new Vec3(c.x - ext, c.y - ext, c.z - ext).times(corr),
					new Vec3(c.x + ext, c.y + ext, c.z + ext).times(corr)));
			}
		}

		return cachedBounds;
	}

	@Override
	public WireframeMesh getWireframeMesh()
	{
		if (cachedWire != null)
			return cachedWire;

		// This is a dirty hack.
		// TODO: Remove it!

		Vec3 vert[] = new Vec3[0];
		int[] from  = new int[0];
		int[] to    = new int[0];

		for (Charge c : charges)
		{
			double rad = c.w;
			Sphere s = new Sphere(rad, rad, rad);
			WireframeMesh wfm = s.getWireframeMesh();

			Vec3[] vert2 = new Vec3[vert.length + wfm.vert.length];
			int i;
			for (i = 0; i < vert.length; i++)
				vert2[i] = vert[i];
			for (Vec3 v : wfm.vert)
				vert2[i++] = v.plus(new Vec3(c.x, c.y, c.z));

			int[] from2 = new int[from.length + wfm.from.length];
			for (i = 0; i < from.length; i++)
				from2[i] = from[i];
			for (int foreign = 0; foreign < wfm.from.length; foreign++)
				from2[i++] = wfm.from[foreign] + vert.length;
			from = from2;

			int[] to2 = new int[to.length + wfm.to.length];
			for (i = 0; i < to.length; i++)
				to2[i] = to[i];
			for (int foreign = 0; foreign < wfm.to.length; foreign++)
				to2[i++] = wfm.to[foreign] + vert.length;
			to = to2;

			vert = vert2;
		}

		return (cachedWire = new WireframeMesh(vert, from, to));
	}

	@Override
	public Property[] getProperties()
	{
		return PROPERTIES.clone();
	}

	@Override
	public Object getPropertyValue(int index)
	{
		switch (index)
		{
			case 0:
				return cutoff;
			case 1:
				return hardness;
		}
		return null;
	}

	@Override
	public void setPropertyValue(int index, Object value)
	{
		switch (index)
		{
			case 0:
				cutoff = (Double)value;
				return;
			case 1:
				hardness = (Double)value;
				cachedBounds = null;
				return;
		}
	}

	// -- Stubs
	@Override
	public void applyPoseKeyframe(Keyframe k)
	{
	}

	@Override
	public void copyObject(Object3D o)
	{
	}

	@Override
	public Object3D duplicate()
	{
		return null;
	}

	@Override
	public Keyframe getPoseKeyframe()
	{
		return null;
	}

	@Override
	public void setSize(double xsize, double ysize, double zsize)
	{
	}
}
