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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import artofillusion.*;
import artofillusion.ui.*;

// remember to include the PreferencesPlugin.jar in the CP for this one:
import artofillusion.preferences.*;

// this is situated in the main ArtOfIllusion.jar:
import buoy.widget.BStandardDialog;

/**
 * Dummy plugin implementation to fetch messages
 */
public class DailyHelpersPlugin implements Plugin
{
	private static DataMap cachedPrefs = null;
	private static String preferencesOwner = "DailyHelpers";


	// ####################
	// # DataMap Handling #
	// ####################

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
			System.out.println("DailyHelpers/getPrefs: Could not get preferences DataMap object. FAIL!");
			return null;
		}
		
		if (!(prefs instanceof DataMap))
		{
			System.out.println("DailyHelpers/getPrefs: Returned object (by preferences.getPreferences) was no instance of a DataMap. FAIL!");
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
	
	public static int getInt(String name)
	{
		DataMap prefs = getPrefs();
		
		if (prefs == null)
			return 0;
		
		return prefs.getInt(name);
	}
	
	public static void putBoolean(String name, boolean val)
	{
		DataMap prefs = getPrefs();
		
		if (prefs == null)
			return;
		
		prefs.putBoolean(name, val);
	}
	
	public static void putInt(String name, int val)
	{
		DataMap prefs = getPrefs();
		
		if (prefs == null)
			return;
		
		prefs.putInt(name, val);
	}
	
	public static void commitSettings() throws IOException
	{
		DataMap prefs = getPrefs();
		
		if (prefs == null)
			return;
		
		prefs.commit();
	}

	// ########################
	// # AoI Message Handling #
	// ########################

	public void processMessage(int message, java.lang.Object[] args)
	{
		if (message == SCENE_WINDOW_CREATED)
		{
			if (!(args[0] instanceof LayoutWindow))
			{
				System.out.println("DailyHelpers/processMessage: args[0] (" + args[0] + ") is no instance of LayoutWindow. Aborting.");
				return;
			}
			
			LayoutWindow lw = (LayoutWindow)args[0];
			
			// do whatever the user wants to do. :-)
			if (DailyHelpersPlugin.getBoolean("killDirectionalLight"))
				Adjustment.doKillLight(lw);
				
			Adjustment.doAutoGrid(lw, DailyHelpersPlugin.getBoolean("showGrid"), DailyHelpersPlugin.getBoolean("snapToGrid"));

			if (DailyHelpersPlugin.getBoolean("maximize"))
				Adjustment.doMaximize(lw);
			
			if (DailyHelpersPlugin.getBoolean("resetAmbient"))
				Adjustment.doResetAmbient(lw);
				
			if (DailyHelpersPlugin.getBoolean("alterCam"))
				Adjustment.doAlterCam(lw);

			// Notify AutoSaver
			AutoSaveFunctions.add(lw);
		}
		else if (message == SCENE_WINDOW_CLOSING)
		{
			if (!(args[0] instanceof LayoutWindow))
			{
				System.out.println("DailyHelpers/processMessage: args[0] (" + args[0] + ") is no instance of LayoutWindow. Aborting.");
				return;
			}
			
			LayoutWindow lw = (LayoutWindow)args[0];

			// Notify AutoSaver
			AutoSaveFunctions.remove(lw);
		}
		else if (message == SCENE_SAVED && DailyHelpersPlugin.getBoolean("autoBackup"))
		{
			if (!(args[0] instanceof File))
			{
				System.out.println("DailyHelpers/processMessage: args[0] (" + args[0] + ") is no instance of File. Aborting.");
				return;
			}
			
			if (!(args[1] instanceof LayoutWindow))
			{
				System.out.println("DailyHelpers/processMessage: args[1] (" + args[1] + ") is no instance of LayoutWindow. Aborting.");
				return;
			}
			
			String origFilename = ((File)args[0]).getAbsolutePath();
			LayoutWindow lw = (LayoutWindow)args[1];
			
			AutoBackupFunctions a = new AutoBackupFunctions();
			int maxLevels = DailyHelpersPlugin.getInt("autoBackupLevels");
			int ret = a.createBackup(origFilename, maxLevels);
			if (ret == 1)
			{
				// OVERFLOW!
				new BStandardDialog
					(
						Translate.text("dailyhelpers:editing.autoBackupGroup"),
						Translate.text("dailyhelpers:autobackup.overflow"),
						BStandardDialog.WARNING
					).showMessageDialog((LayoutWindow)args[1]);
			}
			else if (ret < 0)
			{
				// Other error
				new BStandardDialog
					(
						Translate.text("dailyhelpers:editing.autoBackupGroup"),
						Translate.text("dailyhelpers:autobackup.saveError"),
						BStandardDialog.ERROR
					).showMessageDialog((LayoutWindow)args[1]);
			}
		}
		else if (message == APPLICATION_STARTING)
		{
			// update autosaver-status
			if (DailyHelpersPlugin.getBoolean("autoSave"))
				AutoSaveFunctions.enable();
			else
				AutoSaveFunctions.disable();

			AutoSaveFunctions.startThread(DailyHelpersPlugin.getInt("autoSaveMinutes"));
		}
		else if (message == APPLICATION_STOPPING)
		{
			AutoSaveFunctions.shutdown();
		}
	}
}
