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

import java.util.Vector;

/**
 * This classes intention is:
 * <ul>
 * 		<li>... to be wrapper object for a smoothing method id (in order to return a string when toString() is called)</li>
 * 		<li>... to maintain association "sm id" &lt;--&gt; "caption"</li>
 * </ul>
 */
public class SMInfos
{
	private static Vector<SMInfos> myList = new Vector<SMInfos>();
	
	// *** Object *** //
	private String caption;
	private int    id;
	
	public SMInfos(String caption, int id)
	{
		this.caption = caption;
		this.id      = id;
		
		SMInfos.addObject(this);
	}
	
	public String toString()
	{
		return caption;
	}
	
	public int getID()
	{
		return id;
	}
	
	// *** Static Management *** //
	private static void addObject(SMInfos which)
	{
		myList.add(which);
	}
	
	public static SMInfos getObjectForID(int id)
	{
		for (SMInfos one : myList)
		{
			if (one.getID() == id)
			{
				return one;
			}
		}
		
		return null;
	}
	
	public static void flushList()
	{
		myList.clear();
	}
}
