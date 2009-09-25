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
import artofillusion.object.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * The actual tool which is able to measure angles
 * @author TroY
 */
public class AngleTool extends EditingTool
{
	private Vec3[]		pv = new Vec3[3];
	private Point2D[]	pp = new Point2D[3];

	private double currentAngle = 0.0;
	private double currentDistance = 0.0;
	
	private boolean canReset = false;
	
	/** Common constructor, set tool button icon */
	public AngleTool(EditingWindow parent)
	{
		super(parent);
		initButton("measure:angle");
	}
	
	/** Return tool tip */
	@Override
	public String getToolTipText()
	{
		return Translate.text("measure:angleTool.tipText");
	}
	
	/** What to do when the user activates the tool: Set the window's help text */
	@Override
	public void activate()
	{
		super.activate();
		theWindow.setHelpText(Translate.text("measure:angleTool.helpText"));
		
		resetState();
	}
	
	/** Tell AoI which clicks we want to catch */
	@Override
	public int whichClicks()
	{
		return ALL_CLICKS;
	}

	/** A click in the scene, let's save this starting point */
	@Override
	public void mousePressed(WidgetMouseEvent e, ViewerCanvas view)
	{
		// shift or a valid state (= 3 points already saved && not shift down)
		// resets the current state
		if (e.isControlDown() || (canReset && !e.isShiftDown()))
		{
			resetState();
		}
		
		int curidx = 0;
		Scene theScene = theWindow.getScene();
		Camera cam  = view.getCamera();
		
		// starting point not yet set? then do so.
		if (pv[0] == null && pp[0] == null)
		{
			curidx = 0;
		}
		// otherwise save the "angle" point
		else
		{
			curidx = 2;
		}
		
		// save point
		pv[curidx] = cam.convertScreenToWorld(e.getPoint(),
									Camera.DEFAULT_DISTANCE_TO_SCREEN);
		pp[curidx] = e.getPoint();
		
		drawAll(view);
		updateStatusBarText();
	}
	
	/** Mouse is being dragged: Show distance */
	@Override
	public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view)
	{
		int curidx = 0;
		Scene theScene = theWindow.getScene();
		Camera cam  = view.getCamera();
		
		// "angle" point not yet set? then drag the first end point
		if (pv[2] == null && pp[2] == null)
		{
			curidx = 1;
		}
		// otherwise drag "angle" point
		else
		{
			curidx = 2;
		}
		
		// get end point
		pv[curidx] = cam.convertScreenToWorld(e.getPoint(),
										Camera.DEFAULT_DISTANCE_TO_SCREEN);	
		pp[curidx] = e.getPoint();
		
		// update "help" text which displays the distance
		currentDistance = pv[0].distance(pv[1]);

		drawAll(view);
		updateStatusBarText();
	}
	
	/** mouse released */
	@Override
	public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view)
	{
		if (pp[2] != null && pv[2] != null)
		{
			canReset = true;
		}
		else
		{
			canReset = false;
		}
	}

	/** draws all stuff depending on shift is pressed */
	private void drawAll(ViewerCanvas view)
	{
		if (pp[0] == null || pp[1] == null)
			return;
		
		// draw indicator
		GeneralPath path = new GeneralPath();
		
		Line2D line = new Line2D.Double(pp[0], pp[1]);
		path.append(line, false);
		
		path.append(getIndicatorShape(pp[0]), false);
		path.append(getIndicatorShape(pp[1]), false);

		// draw angle		
		if (pp[2] != null)
		{
			Vec2 sVStart = new Vec2(pp[0].getX(), pp[0].getY());
			Vec2 dir     = new Vec2(pp[1].getX(), pp[1].getY());
			Vec2 dirCur  = new Vec2(pp[2].getX(), pp[2].getY());
			
			dir.subtract(sVStart);
			dirCur.subtract(sVStart);
			
			dirCur.normalize();
			dirCur.scale(dir.length());
			
			path.append(new Line2D.Double(pp[0], new Point2D.Double(sVStart.x + dirCur.x, sVStart.y + dirCur.y)), false);
			
			
			// update current angle
			Vec3 d1 = pv[1].minus(pv[0]);
			Vec3 d2 = pv[2].minus(pv[0]);
			
			d1.normalize();
			d2.normalize();
			
			double aCosFrom = d1.dot(d2);
			if (aCosFrom > 1.0)
				aCosFrom = 1.0;
			else if (aCosFrom < -1.0)
				aCosFrom = -1.0;
			
			currentAngle = Math.acos(aCosFrom) * (180.0 / Math.PI);
		}
		else
		{
			currentAngle = 0.0;
		}

		// current circle with screen radius as radius (ye, it's teh radius.)
		Vec2 pp0v = new Vec2(pp[0].getX(), pp[0].getY());
		Vec2 pp1v = new Vec2(pp[1].getX(), pp[1].getY());
		getCircleShape(pp[0], pp0v.distance(pp1v), path);
				
		view.drawDraggedShape(path);
	}
	
	/** create an indicator shape */
	private Shape getIndicatorShape(Point2D origin)
	{
		GeneralPath out = new GeneralPath();
		
		out.append(new Line2D.Double(origin.getX() - 5, origin.getY() - 5,
									 origin.getX() + 5, origin.getY() + 5), false);
		
		out.append(new Line2D.Double(origin.getX() - 5, origin.getY() + 5,
									 origin.getX() + 5, origin.getY() - 5), false);
		
		return out;
	}
	
	/** creates a circle */
	private void getCircleShape(Point2D sCenter, double screenRadius, GeneralPath target)
	{
		// I didn't manage to draw an Ellipse2D directly... O_o
		
		int segments = 2 + (int)Math.round(8.0 * Math.log(screenRadius+1.0));
		Mat4 tm = Mat4.zrotation( (2 * Math.PI) / segments);
		Vec3 dir = new Vec3(screenRadius, 0.0, 0.0);

		for (int i = 0; i < segments; i++)
		{
			Vec3 a = dir;
			dir    = tm.times(dir);
			
			target.append(new Line2D.Double(a.x + sCenter.getX(), a.y + sCenter.getY(),
										  dir.x + sCenter.getX(), dir.y + sCenter.getY()), false);
		}
	}
	
	/** reset everything */
	private void resetState()
	{
		for (int i = 0; i < pp.length; i++)
			pp[i] = null;
			
		for (int i = 0; i < pv.length; i++)
			pv[i] = null;
	}
	
	private void updateStatusBarText()
	{
		String fmt = Translate.text("measure:angleTool.distanceAngleFormattedText");
		fmt = fmt.replaceAll("%d", Double.toString(currentDistance));
		fmt = fmt.replaceAll("%a", Double.toString(currentAngle));
		
		theWindow.setHelpText(fmt);
	}
}
