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

/**
 * The interface which all MusickReaders must implement
 * @author TroY
 */
public interface MusickReader
{
	/**
	 * A method to verify if this reader can actually read the given file
	 */
	public boolean canRead(String path);

	/**
	 * Attach this reader to this file
	 */
	public void attach(String path);

	/**
	 * Get the file which belongs to this reader
	 */
	public String getFile();

	/**
	 * Get the scope starting at this time, scaled from -1.0 to 1.0,
	 * duration according to given length
	 * NOTE: YOU have to take care of the time offset! it's stored in the "job" object
	 */
	public double[] getScope(double time, MusickObject.ControlledObject job);
}
