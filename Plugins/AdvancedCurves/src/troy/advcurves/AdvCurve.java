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
import java.io.*;
import java.awt.*;
import buoy.event.*;
import buoy.widget.*;

/**
 * Object type: Advanced Curves
 * @author TroY
 */
public class AdvCurve extends Curve
{
	// Stuff to return when the object is a null-len curve
	static BoundingBox   nullBounds = null;
	static WireframeMesh nullMesh   = null;
	
	static
	{
		// same as for NullObjects
		
		Vec3 vert[];
		double r = 0.25, i, j, k;
		int ind1, ind2, from[], to[];

		nullBounds = new BoundingBox(-r, r, -r, r, -r, r);
		vert = new Vec3 [6];
		from = new int [3];
		to = new int [3];
		vert[0] = new Vec3(r, 0.0, 0.0);
		vert[1] = new Vec3(-r, 0.0, 0.0);
		vert[2] = new Vec3(0.0, r, 0.0);
		vert[3] = new Vec3(0.0, -r, 0.0);
		vert[4] = new Vec3(0.0, 0.0, r);
		vert[5] = new Vec3(0.0, 0.0, -r);
		from[0] = 0;
		to[0] = 1;
		from[1] = 2;
		to[1] = 3;
		from[2] = 4;
		to[2] = 5;
		nullMesh = new WireframeMesh(vert, from, to);
	}
	
	// Constructors
	/**
	 * Default Constructor
	 */
	public AdvCurve()
	{
		// Create a null-length curve - approximated!
		this(Mesh.APPROXIMATING);
	}
	
	/**
	 * Constructor with smoothing method
	 */
	public AdvCurve(int smoothingMethod)
	{
		// Create a null-length curve!
		this(new Vec3[] {}, new float[] {}, smoothingMethod, false);
	}
	
	/**
	 * Create the curve according to these parameters - passed on to super class
	 */
	public AdvCurve(Vec3 v[], float smoothness[], int smoothingMethod, boolean isClosed)
	{
		super(v, smoothness, smoothingMethod, isClosed);
	}	
	
	/**
	 * Read from a file - passed on to super class
	 */
	public AdvCurve(DataInputStream in, Scene theScene) throws IOException, InvalidObjectException
	{
		super(in, theScene);
	}

	// Editing Windows
	/**
	 * Create/show a new AdvCurveEditorWindow for this curve
	 */
	@Override
	public void edit(EditingWindow parent, ObjectInfo info, Runnable cb)
	{		
		AdvCurveEditorWindow ed = new AdvCurveEditorWindow(parent, "AdvancedCurve object '"+ info.getName() +"'", info, cb, true);
		ed.setVisible(true);
	}
	
	/**
	 * Create/show a new AdvCurveEditorWindow for this curve-actor
	 */
	@Override
	public void editGesture(final EditingWindow parent, ObjectInfo info, Runnable cb, ObjectInfo realObject)
	{
		AdvCurveEditorWindow ed = new AdvCurveEditorWindow(parent, "(AdvancedCurve) Gesture '"+ info.getName() +"'", info, cb, false);
		ViewerCanvas views[] = ed.getAllViews();
		for (int i = 0; i < views.length; i++)
			((MeshViewer) views[i]).setScene(parent.getScene(), realObject);
		ed.setVisible(true);
	}

	// Mesh Viewer
	/**
	 * Define which objects draws the curve
	 * @return an instance of AdvCurveViewer which can draw outlines etc
	 */
	@Override
	public MeshViewer createMeshViewer(MeshEditController controller, RowContainer options)
	{
		return new AdvCurveViewer(controller, options);
	}
	
	// Methods which require distinction between null-len and normal curve
	/**
	 * Return a WireframeMesh ready to show.
	 * @return If there are 2 or more points, return the common curve
	 * mesh. Otherwise, return a simple icon.
	 */
	@Override
	public WireframeMesh getWireframeMesh()
	{
		if (vertex.length > 1)
		{
			return super.getWireframeMesh();
		}
		else
		{
			return AdvCurve.nullMesh;			
		}
	}
	
	/**
	 * Gets the bounding box for this object.
	 * @return If there are 2 or more points, return the curve's
	 * bounding box. Otherwise, return a box which encloses the icon.
	 */
	@Override
	public BoundingBox getBounds()
	{
		if (vertex.length > 1)
		{
			return super.getBounds();
		}
		else
		{
			return AdvCurve.nullBounds;
		}
	}

	/**
	 * Decides whether this curve can be converted to a TriMesh
	 * @return if there are 2 or more points, return what the Curve class
	 * returns. Otherwise, return CANT_CONVERT.
	 */
	@Override
	public int canConvertToTriangleMesh()
	{
		if (vertex.length > 1)
		{
			return super.canConvertToTriangleMesh();
		}
		else
		{
			return CANT_CONVERT;
		}
	}
	
	/**
	 * Decides whether this curve can be converted to an Actor
	 * @return if there are 2 or more points, return what the Curve class
	 * returns. Otherwise, return FALSE.
	 */
	@Override
	public boolean canConvertToActor()
	{
		if (vertex.length > 1)
		{
			return super.canConvertToActor();
		}
		else
		{
			return false;
		}
	}
	
	// Copy and Paste
	/**
	 * Duplicate this object.
	 * @return a copy of this object, instance of AdvCurve
	 */
	@Override
	public Object3D duplicate()
	{
		Vec3 v[] = new Vec3 [vertex.length];
		float s[] = new float [vertex.length];

		for (int i = 0; i < vertex.length; i++)
		{
			v[i] = new Vec3(vertex[i].r);
			s[i] = smoothness[i];
		}
		return new AdvCurve(v, s, smoothingMethod, closed);
	}
	
	/**
	 * Copy properties from another AdvCurve
	 * @param obj must be an instance of AdvCurve
	 */
	@Override
	public void copyObject(Object3D obj)
	{
		AdvCurve cv = (AdvCurve) obj;
		MeshVertex v[] = cv.getVertices();

		vertex = new MeshVertex [v.length];
		smoothness = new float [v.length];
		for (int i = 0; i < vertex.length; i++)
		{
			vertex[i] = new MeshVertex(new Vec3(v[i].r));
			smoothness[i] = cv.smoothness[i];
		}
		
		smoothingMethod = cv.smoothingMethod;
		setClosed(cv.closed);
		clearCachedMesh();
	}
}
