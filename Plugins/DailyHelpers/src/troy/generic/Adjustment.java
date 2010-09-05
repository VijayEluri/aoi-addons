/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.generic;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.math.*;

/**
 * This class contains most of the adjustment functionality
 */
public class Adjustment
{
	/**
	 * kills the initial directional light if desired - only new scenes
	 */
	public static void doKillLight(LayoutWindow lw)
	{
		Scene sc = lw.getScene();
		
		// only new scenes:
		if (sc.getName() != null)
			return;
		
		ObjectInfo oi  = null;
		int        num = sc.getNumObjects();
		
		for (int i = 0; i < num; i++)
		{
			oi = sc.getObject(i);
			if (oi != null && oi.getObject() instanceof DirectionalLight)
			{
				sc.removeObject(i, null);
				
				lw.rebuildItemList();
		
				System.out.println("DailyHelpers/doKillLight: Successful.");

				return;
			}
		}
		
		System.out.println("DailyHelpers/doKillLight: Initial directional light not found. FAIL!");
	}

	/**
	 * activates grid and snap-to-grid if desired
	 */
	public static void doAutoGrid(LayoutWindow lw, boolean showGrid, boolean snapToGrid)
	{
		Scene sc = lw.getScene();
		
		if (showGrid)
		{		
			// alter the scene directly
			if (!(sc.getShowGrid()))
			{
				sc.setShowGrid(true);
				System.out.println("DailyHelpers/doAutoGrid: setShowGrid(true)");
			}
			else
			{
				System.out.println("DailyHelpers/doAutoGrid: Scene already has grid enabled");
			}
		
		}
		
		if (snapToGrid)
		{
			if (!(sc.getSnapToGrid()))
			{
				sc.setSnapToGrid(true);
				System.out.println("DailyHelpers/doAutoGrid: setSnapToGrid(true)");
			}
			else
			{
				System.out.println("DailyHelpers/doAutoGrid: Scene already has snap-to-grid enabled");
			}
		}

		// as a look at LayoutWindow.java revealed: all viewports need to be adjusted, too.
		// so, iterate through the window's views
		for (ViewerCanvas v : lw.getAllViews())
		{
			// we call this:
			//     public void setGrid(double spacing, int subdivisions, boolean show, boolean snap)
			// we'll set the last two booleans, so what we need are the first two values ...
			
			double sp = v.getGridSpacing();
			int   sub = v.getSnapToSubdivisions();
			
			// now we can set the grid's parameters
			v.setGrid(sp, sub, showGrid, snapToGrid);
			
			// log again
			System.out.println("DailyHelpers/doAutoGrid: ViewCanvas updated: " + v);
		}
	}
	
	/**
	 * maximizes window if desired
	 */
	public static void doMaximize(LayoutWindow lw)
	{
		lw.getFrame().setMaximized(true);	
		
		System.out.println("DailyHelpers/doMaximize: Scene Window maximized");
	}
	
	/**
	 * resets ambient light to black if desired - only new scenes
	 */
	public static void doResetAmbient(LayoutWindow lw)
	{
		Scene sc = lw.getScene();
		
		// only new scenes:
		if (sc.getName() != null)
			return;
		
		sc.setAmbientColor(new RGBColor(0.0, 0.0, 0.0));
		
		System.out.println("DailyHelpers/doResetAmbient: Ambient color set to black");
	}

	/**
	 * alters the scenes camera if desired - only new scenes
	 */
	public static void doAlterCam(LayoutWindow lw)
	{
		Scene sc = lw.getScene();
		
		// only new scenes:
		if (sc.getName() != null)
			return;
	
		ObjectInfo oi  = null;
		int        num = sc.getNumObjects();
		
		for (int i = 0; i < num; i++)
		{
			oi = sc.getObject(i);
			if (oi != null && oi.name.equals("Camera 1") && oi.object instanceof SceneCamera)
			{
				oi.coords.setOrigin(new Vec3(-11.0, 9.0, 15.0));
				oi.coords.setOrientation(-25.0, -145.0, 5.0);
				
				sc.objectModified(oi.object);
				oi.object.sceneChanged(oi, sc);
				
				lw.updateImage();
		
				System.out.println("DailyHelpers/doAlterCam: SceneCamera adjusted");

				return;
			}
		}
	}
}

