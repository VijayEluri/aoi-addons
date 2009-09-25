/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.musick.readers;

import troy.musick.*;

/**
 * A "reader" that does nothing - is used when a file isn't readable
 * or when there's no appropriate reader for this filetype
 * @author TroY
 */
public class NullReader implements MusickReader
{
	private String path = "";

	@Override
	public boolean canRead(String path)
	{
		return true;
	}

	@Override
	public void attach(String path)
	{
		this.path = path;
	}

	@Override
	public String getFile()
	{
		return path;
	}

	@Override
	public double[] getScope(double time, MusickObject.ControlledObject job)
	{
		return new double[job.scopeLen];
	}
}
