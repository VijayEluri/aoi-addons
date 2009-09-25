/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.musick;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.script.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * One single controller in the scene, never rendered
 * @author TroY
 */
public class MusickObject extends NullObject
{
	// what's to be controlled
	protected Vector<MusickObject.ControlledObject> targets = new Vector<MusickObject.ControlledObject>();

	/** This object IS editable */
	@Override
	public boolean isEditable()
	{
		return true;
	}

	/** Hence, we need something to edit it */
	@Override
	public void edit(EditingWindow parent, ObjectInfo info, Runnable callback)
	{
		new MusickEditingWindow((LayoutWindow)parent, info, callback).setVisible(true);
	}

	/** Let's go! Inform all attached objects. */
	@Override
	public void sceneChanged(ObjectInfo info, Scene scene)
	{
		System.out.println("MusickObject reports: TIME CHANGED! Informing targets!");


		for (ControlledObject job : targets)
		{
			if (job.sendScope)
				sendScope(scene, job);
		}
	}

	/** I'm changed by an edit dialogue! */
	public void updateOneTarget(ControlledObject co)
	{
		// have a look at all existing objects. if we already know the object,
		// update it.
		for (ControlledObject existingOne : targets)
		{
			// things are identified by their AoI ID
			// it's an integer, so comparison via "==" is ok
			if (existingOne.info.getId() == co.info.getId())
			{
				// gotcha!
				targets.removeElement(existingOne);
				targets.add(co);
				System.out.println("MusickObject reports an updated item:\n" + co.toString()); 
				return;
			}
		}

		// ok, not found. just add it.
		System.out.println("MusickObject reports a new item:\n" + co.toString());
		targets.add(co);
	}

	/** Edit dialogue requests current target info */
	public Vector<MusickObject.ControlledObject> getTargets()
	{
		return targets;
	}
	
	/** SEND SCOPE TO A TARGET  */
	public void sendScope(Scene scene, ControlledObject job)
	{
		if (job.reader == null)
			return;

		System.out.println("MusickObject reports: Sending Scope to " + job.info.getName());

		// ok, for now, only scripted objects are supported
		double[] scope = job.reader.getScope(scene.getTime(), job);
		String id = "musick_amplitude_";

		Object3D obj = job.info.getObject();
		if (obj instanceof ScriptedObject)
		{
			ScriptedObject so = (ScriptedObject)obj;
			String[] names  = prepareNames(so, scope.length, id);
			double[] values = prepareValues(so, scope.length, id);

			//System.out.println("Lens after prep: " + names.length + ", " + values.length);

			// add scope values
			int scopeCounter = 0;
			for (int i = names.length - scope.length; i < names.length; i++)
			{
				names[i] = id + Integer.toString(scopeCounter);
				values[i] = scope[scopeCounter++];
			}

			so.setParameters(names, values);
		}
	}

	/**
	 * Copy all names in the array except the one listed in "skip"
	 * IMPORTANT: It is NOT guaranteed that the current order will persist!
	 */
	public String[] prepareNames(ScriptedObject so, int newSpace, String skip)
	{
		Vector<String> tempVec = new Vector<String>();

		for (int in = 0; in < so.getNumParameters(); in++)
		{
			// get the in'th name
			String name = so.getParameterName(in);

			// see if "blacklisted"
			if (!name.startsWith(skip))
				tempVec.add(name);
		}

		// "add new blank space"
		for (int i = 0; i < newSpace; i++)
			tempVec.add("");

		// convert it to an array and return it
		return tempVec.toArray(new String[1]);
	}

	/**
	 * Copy all values in the array except the one listed in "skip"
	 * IMPORTANT: It is NOT guaranteed that the current order will persist!
	 */
	public double[] prepareValues(ScriptedObject so, int newSpace, String skip)
	{
		Vector<Double> tempVec = new Vector<Double>();

		for (int in = 0; in < so.getNumParameters(); in++)
		{
			// get the in'th name
			String name = so.getParameterName(in);
			double value = so.getParameterValue(in);

			// see if "blacklisted"
			if (!name.startsWith(skip))
				tempVec.add(value);
		}

		// "add new blank space"
		for (int i = 0; i < newSpace; i++)
			tempVec.add(0.0);

		// convert it to an array and return it
		double[] out = new double[tempVec.size()];
		for (int i = 0; i < tempVec.size(); i++)
		{
			out[i] = tempVec.get(i).doubleValue();
		}
		return out;
	}

	// ----------------------------------------
	// contains info on what will be controlled
	// inner class so simplify things
	public static class ControlledObject
	{
		public ObjectInfo info = null;
		public MusickReader reader = null;
		public boolean sendScope     = false;
		public int     scopeLen      = 0;
		public int     channels      = 0;
		public double  timeOffset    = 0.0;

		public String toString()
		{
			return new String(
					"ControlledObject["
					+ "sendScope=" + sendScope
					+ ",scopeLen=" + scopeLen
					+ ",channels=" + channels
					+ ",timeoffset=" + timeOffset
					+ ",reader=" + (reader == null ? "" : reader.getClass().getSimpleName())
					+ ",file=" + (reader == null ? "" : reader.getFile())
					+ "]"
					);
		}

		public String getStringForChannel(int which)
		{
			switch (which)
			{
				case 0:
					return "Stereo";
				case 1:
					return "Left";
				case 2:
					return "Right";
				case 3:
					return "Avg";
				case 4:
					return "Mono";
			}

			return getStringForChannel(getDefaultChannel());
		}

		public int getChannelForString(String which)
		{
			if (which.equals("Stereo"))
				return 0;

			if (which.equals("Left"))
				return 1;

			if (which.equals("Right"))
				return 2;

			if (which.equals("Avg"))
				return 3;

			if (which.equals("Mono"))
				return 4;

			return getDefaultChannel();
		}

		public String getDefaultChannelString()
		{
			return getStringForChannel(getDefaultChannel());
		}

		public int getDefaultChannel()
		{
			return 0;
		}

		public static Object[] enumerateChannelStrings()
		{
			return new Object[] {"Stereo", "Left", "Right", "Avg", "Mono"};
		}
	}
}
