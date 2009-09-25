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

import java.util.Vector;
import troy.musick.readers.*;

/**
 * Gets the right reader for a file
 * @author TroY
 */
public class MusickReaderFactory
{
	private static boolean initialRegistrationDone = false;
	private static Vector<Class> registeredReaders = new Vector<Class>();

	/**
	 * Cycle through all registered readers and see, if one of them can read
	 * the file - if so, return an instance of it
	 */
	public static MusickReader getReaderForFile(String path)
	{
		if (!initialRegistrationDone)
			init();

		// check all registered readers if one of them can read the file
		for (Class one : registeredReaders)
		{
			Object obj = null;
			try
			{
				obj = one.newInstance();
			}
			catch (Exception e)
			{
				System.out.println("MusickReaderFactory reports: Whoops! "
						+ one.getName() + " could not be instantiated.");
			}

			if (obj != null && obj instanceof MusickReader)
			{
				MusickReader reader = (MusickReader)obj;
				if (reader.canRead(path))
				{
					reader.attach(path);
					return reader;
				}
			}
		}

		// in case there's no appropriate reader, return a so called
		// "NullReader" which always returns 0 to anything
		MusickReader n = new NullReader();
		n.attach(path);
		return n;
	}

	/** Add a new reader if not yet existing */
	public static boolean registerReader(Class impl)
	{
		Class[] interfaces = impl.getInterfaces();

		// check if "impl" actually implements the needed interface
		for (Class one : interfaces)
		{
			if (one.isInterface() && one.equals(MusickReader.class))
			{
				if (!registeredReaders.contains(impl))
					registeredReaders.add(impl);
				return true;
			}
		}

		return false;
	}

	/**
	 * Register built-in readers, other potential readers to have to register on their
	 * own at an approriate point in time.
	 */
	private static void init()
	{
		initialRegistrationDone = true;

		registerReader(WaveReader.class);
	}
}
