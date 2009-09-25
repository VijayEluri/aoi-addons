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
 * The actual tool which is able to measure distances
 * @author TroY
 */
public class MeasureTool extends EditingTool
{
	private Vec3 mStart	= null;
	private Vec3 mEnd	= null;
	private Point sStart = null;
	private Point sEnd = null;
	
	private double currentDistance = 0.0;
	
	/** Common constructor, set tool button icon */
	public MeasureTool(EditingWindow parent)
	{
		super(parent);
		initButton("measure:measure");
	}
	
	/** Return tool tip */
	@Override
	public String getToolTipText()
	{
		return Translate.text("measure:measureTool.tipText");
	}
	
	/** What to do when the user activates the tool: Set the window's help text */
	@Override
	public void activate()
	{
		super.activate();
		theWindow.setHelpText(Translate.text("measure:measureTool.helpText"));
		
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
		Scene theScene = theWindow.getScene();
		Camera cam  = view.getCamera();
		
		// no SHIFT pressed, start from scratch
		if (!e.isShiftDown())
		{
			resetState();

			// save starting point
			mStart = cam.convertScreenToWorld(e.getPoint(),
										Camera.DEFAULT_DISTANCE_TO_SCREEN);
			sStart = e.getPoint();
		}
		// SHIFT is pressed, so set new points
		else
		{
			// just pass on to mouseDragged
			mouseDragged(e, view);
		}
	}
	
	/** Mouse is being dragged: Show distance */
	@Override
	public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view)
	{
		if (mStart == null || sStart == null)
			return;
		
		Scene theScene = theWindow.getScene();
		Camera cam  = view.getCamera();
		
		// get end point
		mEnd = cam.convertScreenToWorld(e.getPoint(),
										Camera.DEFAULT_DISTANCE_TO_SCREEN);
		
		// update "help" text which displays the distance
		currentDistance = mStart.distance(mEnd);
		
		sEnd = e.getPoint();

		drawAll(view);
		updateStatusBarText();
	}

	/** draws all stuff */
	private void drawAll(ViewerCanvas view)
	{
		// draw indicator
		GeneralPath path = new GeneralPath();
		
		Line2D line = new Line2D.Double(sStart, sEnd);
		path.append(line, false);
		
		path.append(getIndicatorShape(sStart), false);
		path.append(getIndicatorShape(sEnd), false);
		
		// current circle with screen radius as radius (ye, it's teh radius.)
		Vec2 sVStart = new Vec2(sStart.getX(), sStart.getY());
		Vec2 sVEnd   = new Vec2(sEnd.getX(), sEnd.getY());
		getCircleShape(sStart, sVStart.distance(sVEnd), path);
				
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
	
	/** reset points */
	private void resetState()
	{
		mStart	= null;
		mEnd	= null;
		sStart	= null;
		sEnd	= null;
		
		currentDistance = 0.0;
	}
	
	private void updateStatusBarText()
	{
		String fmt = Translate.text("measure:measureTool.distanceFormattedText");
		fmt = fmt.replaceAll("%d", Double.toString(currentDistance));
		
		theWindow.setHelpText(fmt);
	}
}
