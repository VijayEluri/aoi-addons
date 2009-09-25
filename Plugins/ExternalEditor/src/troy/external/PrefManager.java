/*
	Copyright (C) 2009 by Peter Hofmann

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.external;

import java.io.*;

import artofillusion.*;

// remember to include the PreferencesPlugin.jar in the CP for this one:
import artofillusion.preferences.*;

/**
 * DataMap handling
 */
public class PrefManager
{
	private static DataMap cachedPrefs = null;
	private static String preferencesOwner = "ExternalEditor";

	private static DataMap getPrefs()
	{
		if (cachedPrefs != null)
			return cachedPrefs;

		Object[] args = new Object[] { preferencesOwner };

		Object prefs = null;
		try
		{
			prefs = PluginRegistry.invokeExportedMethod("preferences.getPreferences", args);
		}
		catch (Exception ex)
		{
			System.out.println(preferencesOwner
					+ "/getPrefs: Could not get preferences DataMap object. FAIL!");
			return null;
		}

		if (!(prefs instanceof DataMap))
		{
			System.out.println(preferencesOwner
					+ "/getPrefs: Returned object (by preferences.getPreferences) was no instance of a DataMap. FAIL!");
			return null;
		}

		cachedPrefs = (DataMap)prefs;

		return cachedPrefs;
	}

	public static boolean getBoolean(String name)
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return false;

		return prefs.getBoolean(name);
	}

	public static void putBoolean(String name, boolean val)
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return;

		prefs.putBoolean(name, val);
	}

	public static int getInt(String name)
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return 0;

		return prefs.getInt(name);
	}

	public static void putInt(String name, int val)
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return;

		prefs.putInt(name, val);
	}

	public static String getString(String name)
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return "";

		return prefs.getString(name);
	}

	public static void putString(String name, String val)
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return;

		prefs.putString(name, val);
	}

	public static void commitSettings()
	{
		DataMap prefs = getPrefs();

		if (prefs == null)
			return;

		try
		{
			prefs.commit();
		}
		catch (Exception ex)
		{
			System.out.println(preferencesOwner
					+ "/EditingPanel/savePreferences: COMMIT FAILED! See exception below.");
			ex.printStackTrace();
		}
	}
}
