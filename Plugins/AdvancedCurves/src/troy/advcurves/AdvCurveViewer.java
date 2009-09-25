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
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;

/**
 * Extended CurveViewer to draw outlines etc
 * Remark: Colors are defined in ViewerCanvas.
 * @author TroY
 */
public class AdvCurveViewer extends CurveViewer
{
	private boolean mouseMoved = false;
	
	/** Common constructor */
	public AdvCurveViewer(MeshEditController window, RowContainer p)
	{
		super(window, p);
	}
	
	/** Draw the object */
	@Override
	protected void drawObject()
	{
		MeshVertex[] mv = ((Mesh) getController().getObject().getObject()).getVertices();
		
		// draw the object itself only if there are 2 or more points
		if (mv.length > 1)
		{
			drawSmoothed(lineColor);
			drawOutline(disabledColor);
		}
		
		// handles are always to be drawn		
		drawHandles(lineColor, specialHighlightColor);
		
		// do not draw an arrow if there are less than 2 points
		if (mv.length > 1)
		{
			drawArrow(lineColor);
		}
	}
	
	/** draw the curve's handles */
	protected void drawHandles(Color commonHandle, Color startEndHandle)
	{
		if (!showMesh)
			return;
		
		MeshVertex v[] = ((Mesh) getController().getObject().getObject()).getVertices();
		boolean selected[] = controller.getSelection();
		Color col = null;
		for (int i = 0; i < v.length; i++)
		{
			if (theCamera.getObjectToView().timesZ(v[i].r) > theCamera.getClipDistance())
			{
				// usually highlight first / last vertex
				if (i == 0 || i == v.length - 1)
				{
					col = startEndHandle;
				}
				else
				{
					col = commonHandle;
				}
				
				// selected coloring
				if (selected[i])
				{
					col = highlightColor;
				}
				
				Vec2 p = theCamera.getObjectToScreen().timesXY(v[i].r);
				double z = theCamera.getObjectToView().timesZ(v[i].r);
				renderBox(((int) p.x) - HANDLE_SIZE/2, ((int) p.y) - HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE, z, col);
			}
		}
	}
	
	/** draw regular smoothed curve */
	protected void drawSmoothed(Color curveColor)
	{
		WireframeMesh wireframe = null;
		
		Object3D theObject = getController().getObject().getObject();
		if (theObject instanceof Curve)
		{
			// Just draw the curve - but subdivide it once more
			
			Curve theCurve = (Curve)theObject;

			if (theCurve.getSmoothingMethod() != Mesh.NO_SMOOTHING)
			{
				wireframe = theCurve.subdivideCurve().getWireframeMesh();
			}
			else
			{
				wireframe = theCurve.getWireframeMesh();
			}
			for (int i = 0; i < wireframe.from.length; i++)
			{
				renderLine(wireframe.vert[wireframe.from[i]], wireframe.vert[wireframe.to[i]], theCamera, curveColor);
			}
		}
	}
	
	/** draw unsmoothed curve in light gray */
	protected void drawOutline(Color outlineColor)
	{
		if (!showMesh)
			return;
		
		WireframeMesh wireframe = null;
		
		Object3D theObject = getController().getObject().getObject();
		if (theObject instanceof Curve)
		{
			// save the curves smoothing method if it's not set to "no smoothing".
			// then set it to "no smoothing", draw the raw curve, and restore
			// the previous smoothing method
			
			Curve theCurve = (Curve)theObject;
			int smBefore = theCurve.getSmoothingMethod();
			if (smBefore != Mesh.NO_SMOOTHING)
			{
				theCurve.setSmoothingMethod(Mesh.NO_SMOOTHING);
				wireframe = theCurve.getWireframeMesh();
				for (int i = 0; i < wireframe.from.length; i++)
					renderLine(wireframe.vert[wireframe.from[i]], wireframe.vert[wireframe.to[i]], theCamera, outlineColor);
				theCurve.setSmoothingMethod(smBefore);
			}
		}
	}
	
	/** draws a little arrow at the end of the curve */
	protected void drawArrow(Color arrowColor)
	{		
		if (!showMesh)
			return;
			
		// how long each line on the arrow is supposed to be
		double arrowLen = 0.1;
		
		Object3D theObject = getController().getObject().getObject();
		if (theObject instanceof Curve)
		{
			Curve theCurve = (Curve)theObject;

			// Get the last direction of the curve:			
			MeshVertex[] mv = theCurve.getVertices();
			
			Vec3 lastDir = new Vec3(mv[mv.length-1].r);
			lastDir.subtract(mv[mv.length-2].r);

			// Get one of the possible vectors that are orthogonal:
			Vec3 b = MathHelper.randomOrtho(lastDir);

			lastDir.normalize();
			b.normalize();

			// Build a new coordinate system and get the matrix which
			// transforms these points to "world" coordinates
			CoordinateSystem traffo = new CoordinateSystem(mv[mv.length-1].r, b, lastDir);
			Mat4 traffoMat = traffo.fromLocal();
			
			// Our vector in "local" coordinates (the newly created system)
			// toLen scales them to the desired length
			Vec3[] arrow = new Vec3[4];
			arrow[0] = MathHelper.toLen(new Vec3( 0.0, -1.0,  1.0), arrowLen);
			arrow[1] = MathHelper.toLen(new Vec3( 0.0, -1.0, -1.0), arrowLen);
			arrow[2] = MathHelper.toLen(new Vec3(-1.0, -1.0,  0.0), arrowLen);
			arrow[3] = MathHelper.toLen(new Vec3( 1.0, -1.0,  0.0), arrowLen);
			
			int from[] = new int[arrow.length];
			int to[]   = new int[arrow.length];
			
			// every line is drawn from the last point on the curve to the
			// transformed vectors
			
			Vec3[] arrowTrans = new Vec3[arrow.length+1];
			arrowTrans[0] = traffo.getOrigin();
			for (int i = 0; i < arrow.length; i++)
			{
				arrowTrans[i+1] = traffoMat.times(arrow[i]);
				from[i]         = 0;
				to[i]           = i+1;
			}
			
			renderWireframe(new WireframeMesh(arrowTrans, from, to), theCamera, arrowColor);
		}
	}

	/** Mouse event: Some mouse button pressed, reset "mouseMoved" state */
	@Override
	protected void mousePressed(WidgetMouseEvent e)
	{
		super.mousePressed(e);
		
		mouseMoved = false;
	}
	
	/** Mouse event: Drag. */
	@Override
	protected void mouseDragged(WidgetMouseEvent e)
	{
		super.mouseDragged(e);
		
		mouseMoved = true;
	}
	
	/** Mouse event: Try to fetch right click only if the mouse hasn't been moved */
	@Override
	protected void mouseReleased(WidgetMouseEvent e)
	{
		super.mouseReleased(e);
		
		if (!mouseMoved && e.getButton() == java.awt.event.MouseEvent.BUTTON3)
		{
			Object src = e.getSource();
			
			// Get parents until we've found the AdvCurveEditorWindow
			while (!(src instanceof AdvCurveEditorWindow) && src instanceof Widget)
			{
				src = ((Widget)src).getParent();
			}
			
			if (src instanceof AdvCurveEditorWindow)
			{
				((AdvCurveEditorWindow)src).showContextMenu(e);
			}
		}
	}
}
