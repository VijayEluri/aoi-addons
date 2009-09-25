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

import artofillusion.*;

import java.util.*;

public class AutoSaveFunctions
{
	protected static ArrayList<LayoutWindow> registered = new ArrayList<LayoutWindow>();
	private static boolean enabled = false;

	public static void add(LayoutWindow lw)
	{
		synchronized (registered)
		{
			registered.add(lw);

			System.out.println("AutoSaver: Added " + lw);
		}
	}

	public static void remove(LayoutWindow lw)
	{
		synchronized (registered)
		{
			registered.remove(lw);

			System.out.println("AutoSaver: Removed " + lw);
		}
	}

	public static void saveAll()
	{
		synchronized (registered)
		{
			if (!enabled)
				return;

			System.out.println("AutoSaver-Thread: Saving triggered!");

			for (LayoutWindow lw : registered)
			{
				System.out.println("AutoSaver-Thread: Now at " + lw);

				// Check if this scene has already been saved and is actually modified!
				// If so, then save again.
				Scene s = lw.getScene();
				if (s.getName() != null && lw.isModified())
				{
					lw.saveCommand();
					System.out.println("AutoSaver-Thread: " + lw + " done.");
				}
				else
				{
					System.out.println("AutoSaver-Thread: " + lw + " skipped.");
				}
			}
			System.out.println("AutoSaver-Thread: Saving done!");
		}
	}

	public static void enable()
	{
		synchronized (registered)
		{
			enabled = true;
		}
	}

	public static void disable()
	{
		synchronized (registered)
		{
			enabled = false;
		}
	}

	public static void startThread(int waitMinutes)
	{
		System.out.println("AutoSaver: Starting up. Interval: " + waitMinutes + " minutes.");

		final int waitMillis = waitMinutes * 1000 * 60;

		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				System.out.println("AutoSaver-Thread running.");

				while (true)
				{
					try
					{
						Thread.sleep(waitMillis);
						AutoSaveFunctions.saveAll();
					}
					catch (InterruptedException e)
					{
						System.out.println("AutoSaver-Thread: Interrupted. Quitting.");
						return;
					}
				}
			}
		};
		t.start();
	}

	public static void shutdown()
	{
		// We only need to make sure that the saver-thread is not killed while
		// working. So we enter the lock, set the control variable to prevent
		// further writes and then we leave the lock. As the saver uses the
		// same lock, this makes sure any working saver exits before we can
		// proceed.
		System.out.println("AutoSaver: Waiting for working threads to finish...");
		disable();
		System.out.println("AutoSaver: Good bye.");
	}
}
