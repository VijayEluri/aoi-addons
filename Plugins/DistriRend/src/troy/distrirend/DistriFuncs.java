/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.distrirend;

import artofillusion.Scene;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SceneCamera;
import artofillusion.math.Vec3;
import java.io.File;

/**
 * The core of scene alteration - all static
 */
public class DistriFuncs
{
	/**
	 * Keep hold of the target directory
	 */
	private static String targetDir = "";
	public static void setTargetDir(String d)
	{
		targetDir = d;
	}
	public static String getTargetDir()
	{
		return targetDir;
	}
	
	/**
	 * Keep hold of the scene
	 */
	private static Scene scene = null;
	public static void setScene(Scene s)
	{
		scene = s;
	}
	public static Scene getScene()
	{
		return scene;
	}
	
	/**
	 * Math helpers
	 */
	private static Double degCos(Double a)
	{
		return Math.cos(Math.PI / 180.0 * a);
	}
	private static Double degSin(Double a)
	{
		return Math.sin(Math.PI / 180.0 * a);
	}
	private static Vec3 rotateVec(Vec3 which, Vec3 by, Double degree)
	{
		// Note: This segment is anything but optimized.

		// n = by
		// r = which
		// p = degree
		
		// r' = r cos p + (1 - cos p)(nr)n + (nxr)sin p
		Vec3 vecmult = new Vec3(by);
		vecmult.multiply(which);	// = nr
		vecmult.multiply(by);		// = (nr)n = a
		
		Vec3 cross = by.cross(which);	// nxr     = b
		
		// r' = r cos p + (1 - cos p) a + b sin p
		Vec3 rcosp = new Vec3(which);
		rcosp.scale(degCos(degree));
		
		Vec3 acosp = new Vec3(vecmult);
		acosp.scale(1 - degCos(degree));
		
		Vec3 bsinp = new Vec3(cross);
		bsinp.scale(degSin(degree));
		
		Vec3 out = new Vec3(rcosp);
		out.add(acosp);
		out.add(bsinp);
		
		return out;
	}

	/**
	 * Save the scene...
	 */
	private static void saveScene(int index)
	throws Exception
	{
		String name = "multi-";
		name += index;
		name += ".aoi";
		
		File f;
		
		if (getTargetDir().equals(""))
			f = new File(name);
		else
			f = new File(getTargetDir(), name);
			
		scene.writeToFile(f);
	}

	/**
	 * Alter the camera and save the scene
	 */
	//public static void distribute(Scene scene, ObjectInfo cam)
	public static void distribute(ObjectInfo cam)
	throws Exception
	{
		Vec3 oldz  = cam.coords.getZDirection();
		Vec3 oldup = cam.coords.getUpDirection();
		Vec3 oldx  = oldz.cross(oldup);

		Double oldfov = ((SceneCamera)cam.object).getFieldOfView();

		// ------- FOV
		((SceneCamera)cam.object).setPropertyValue(0, oldfov / 2.0);

		for (int i = 0; i < 2; i++)
		{
			// ------- FIRST IMAGE
			Double rotateBy = Math.pow((-1.0), i) * oldfov / 4.0; // do it for i = -1 and i = 1 ...
			Vec3 rotateAround = oldx;
			Vec3 newup = rotateVec(oldup, rotateAround, rotateBy);
			Vec3 newz  = rotateVec(oldz,  rotateAround, rotateBy);
			cam.coords.setOrientation(newz, newup);
			

			// ------- SAVE AND STUFF
			saveScene(i);
			
			// ------- RESET
			cam.coords.setOrientation(oldz, oldup);
		}

		// -------- RESET FOV
		((SceneCamera)cam.object).setPropertyValue(0, oldfov);
	}
}
