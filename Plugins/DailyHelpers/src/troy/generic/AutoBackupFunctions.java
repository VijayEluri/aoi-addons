/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

/*
	This class actually creates the backups.
	
	Nonstatic methods in the hope to reduce memory req's.
	
	.dm'TroY., October 2007
*/

package troy.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;

public class AutoBackupFunctions
{
	/**
	 * How long the extension shall be
	 */
	private final int EXTLEN = 5;
	
	/**
	 * What shall be appended? "autobak" for example
	 */
	private final String EXTPREFIX = "autobak";
	
	/**
	 * What shall be appended to old files in case of overflow? "old" for example
	 */
	private final String EXTOLDSUFFIX = "old";

	/**
	 * Pad the string with leading zeroes
	 * @param in String to pad
	 * @param num Append up to num zeroes
	 * @return Padded string. ;=)
	 */
	private String padZero(String in, int num)
	{
		for (int i = 0; i < num && in.length() < num; i++)
		{
			in = "0" + in;
		}
		
		return in;
	}
	
	/**
	 * Just copies on file to another.
	 */
	private void copy(File source, File dest) throws IOException
	{
		
		FileInputStream in = null;
		FileOutputStream out = null;
		
		try
		{
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			
			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
		}
		finally
		{
			if (in != null)
				in.close();
			
			if (out != null)
				out.close();
		}
	}
	
	/**
	 * Find all existing files in the given dir which match "<origFile>.autobakNNNNN"
	 * @param inDir dir in which to look for files
	 * @param origName original filename, i.e. "scene.aoi"
	 * @return a new ArrayList<File> which contains all matching files
	 */
	private ArrayList<File> findExistingBackups(File inDir, String origName)
	{
		ArrayList<File> existingBackups = new ArrayList<File>();
		String[] dirCont;
		final String forAnony = origName;
		
		dirCont = inDir.list(new java.io.FilenameFilter()
				{
					public boolean accept(File dir, String name)
					{
						return name.matches(forAnony.replace(".", "\\.") + "\\." + EXTPREFIX + "[0-9]{" + EXTLEN + "}");
					}
				}
			);
		for (String one : dirCont)
		{
			System.out.print("\t" + one + " ... ");
			
			// let's check it with a regex.
			//if (one.getName().matches(origName.replace(".", "\\.") + "\\." + EXTPREFIX + "[0-9]{" + EXTLEN + "}"))
			//if (one.matches(origName.replace(".", "\\.") + "\\." + EXTPREFIX + "[0-9]{" + EXTLEN + "}"))
			//{
				System.out.print("MATCH!");
				existingBackups.add(new File(inDir, one));
			//}
			
			System.out.println();
		}
		
		return existingBackups;
	}
	
	/**
	 * Creates a new backup of a file.
	 * <ul>
	 *      <li>Starting with .bak00000 if no backup yet exists</li>
	 *      <li>These files will be stored in the same directory</li>
	 *      <li>This number will increase with each new backup, holding up to maxLevel copies.</li>
	 *      <li>If more than maxLevel copies exist, files with lower numbers will be deleted!!!</li>
	 *      <li>This method does NOT depend on lastModified(), it only looks for the file numbers!</li>
	 *      <li>If the last backup was .bak99999, the user will be warned, this file will be renamed
	 *          to .bak99999.old also all other remaining old files, and a new round will start
	 *          with .bak00000.</li>
	 *      <li>This method can only handle ONE overflow of this type!</li>
	 * </ul>
	 * 
	 * @param path Path to file to backup
	 * @param maxLevel Keep up to maxLevel backups
	 * @return 0 if everything was ok, less than 0 on error, 1 on filenumber-overflow
	 */
	public int createBackup(String path, int maxLevel)
	{
		File orig = new File(path);
		String origName;
		File[] dirCont;
		ArrayList<File> existingBackups = null;
		ArrayList<File> temp            = new ArrayList<File>();
		boolean hadOverflow = false;
		
		// check if file exists ...
		if (!(orig.exists()))
		{
			System.out.println("Original file does not exist.");
			return -1;
		}
		
		origName = orig.getName();
		
		// we want to see all files in this dir
		System.out.println("Dir contents:");
		
		// save all yet existing backups
		/*
		dirCont = new File(orig.getAbsoluteFile().getParent()).listFiles();
		for (File one : dirCont)
		{
			System.out.print("\t" + one.getName() + " ... ");
			
			// let's check it with a regex.
			if (one.getName().matches(origName.replace(".", "\\.") + "\\." + EXTPREFIX + "[0-9]{" + EXTLEN + "}"))
			{
				System.out.print("MATCH!");
				existingBackups.add(one);
			}
			
			System.out.println();
		}
		*/
		existingBackups = findExistingBackups(orig.getParentFile(), origName);

		// sort them: by Filename DESC
		// 		this implies: scene.aoi.bak0143 is newer than scene.aoi.bak0010
		java.util.Collections.sort(existingBackups, new java.util.Comparator<File>()
			{
				public int compare(File f1, File f2)
				{
					return -1 * (f1.getName().compareTo(f2.getName()));
				}
			}
		);
		
		int count = 0;
		System.out.println("Sorted:");
		for (File one : existingBackups)
		{
			System.out.print("\t" + one.getAbsolutePath());
			count++;
			
			if (count > maxLevel - 1)
			{
				System.out.print(" (DEL");
				if (!one.canWrite())
				{
					System.out.print(" - NO WRITE ACCESSS");
				}
				else
				{
					if (!one.delete())
					{
						System.out.print(" - FAILED");
					}
				}
				System.out.print("!)");
			}
			else
			{
				temp.add(one);
			}
			
			System.out.println();
		}
		// kill deleted files
		existingBackups = temp;
		
		
		System.out.println("After Cleansing:");
		for (File one : existingBackups)
		{
			System.out.println("\t" + one.getName());
		}
		
		
		// after this cleansing, achieve highest filename to build the new name
		String newName = orig.getAbsolutePath();
		if (existingBackups.size() == 0)
		{
			// no backup yet, start with 0
			newName += "." + EXTPREFIX + padZero("0", EXTLEN);
		}
		else
		{
			String lastBak = existingBackups.get(0).getName();
			
			// through to our regex above, it is secured that this string will have an
			// ".bakNNNNN" or whatever at it's end. (with EXTLEN's padded zero's)
			int newNum = Integer.parseInt(lastBak.substring(lastBak.length() - EXTLEN)) + 1;
			
			// possible: this new number has more digits than allowed.
			if (Integer.toString(newNum).length() > EXTLEN)
			{
				System.out.println("*** OVERFLOW! ALERT USER! IMPORTANT! ***");
				
				hadOverflow = true;
				
				// start again with 0, but rename all old backups
				newName += "." + EXTPREFIX + padZero("0", EXTLEN);
				
				for (File one : existingBackups)
				{
					System.out.println("Renamed " + one.getName() + " to " + one.getName() + "." + EXTOLDSUFFIX + " !");
					one.renameTo(new File(one.getAbsolutePath() + "." + EXTOLDSUFFIX));
				}
			}
			// no overflow, just set the new number
			else
			{
				newName += "." + EXTPREFIX + padZero(Integer.toString(newNum), EXTLEN);
			}
		}
		
		try
		{
			// now copy ...
			copy(orig, new File(newName));
			System.out.println("\nNew Backup: " + newName);
			return (hadOverflow ? 1 : 0);
		}
		catch (IOException e)
		{
			// D'OH!
			System.out.println("\nERROR: " + e.getMessage() + "\n");
			e.printStackTrace();
			return -2;
		}
	}
}
