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

import artofillusion.object.*;

/**
 * As a regular "ObjectInfo" doesn't have a toString()-method,
 * we sadly need a wrapper for this
 * @author TroY
 */
public class ObjectInfoWrapper
{
	private ObjectInfo which = null;
	
	public ObjectInfoWrapper(ObjectInfo which)
	{
		this.which = which;
	}
	
	public ObjectInfo getItem()
	{
		return which;
	}
	
	public void setItem(ObjectInfo which)
	{
		this.which = which;
	}
	
	public String toString()
	{
		if (which != null)
		{
			return which.name.toString();
		}
		else
		{
			return "<None>";
		}
	}
}
