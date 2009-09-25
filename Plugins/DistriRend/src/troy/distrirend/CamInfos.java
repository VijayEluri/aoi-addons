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

import artofillusion.object.ObjectInfo;
//import artofillusion.object.SceneCamera;

/**
 * Store cameras and maintain indices
 */
public class CamInfos
{
	private ObjectInfo cam   = null;
	private int         index = 0;
	
	public CamInfos(ObjectInfo cam, int index)
	{
		this.cam   = cam;
		this.index = index;
	}
	
	public ObjectInfo getObjectInfo()
	{
		return cam;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public String toString()
	{
		return cam.name;
	}
}
