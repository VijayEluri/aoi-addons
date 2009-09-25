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

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.script.*;
import artofillusion.ui.Translate;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.security.*;
import java.awt.*;

public class ExternalEditorPlugin implements ModellingTool
{
	/**
	 * Return the string that will appear in the "Tools" menu
	 */
	@Override
	public String getName()
	{
		return Translate.text("ExternalEditor:pluginName") + "...";
	}

	/**
	 * What happens when the user clicks on the menu item:
	 * Launch the editing routine in a separate thread.
	 */
	@Override
	public void commandSelected(final LayoutWindow lw)
	{
		// This should be done on the EDT
		final Collection<ObjectInfo> sel = lw.getSelectedObjects();

		// Launch editing in background
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				editExternal(lw, sel);
			}
		};
		t.start();
	}

	/**
	 * Do the actual work: Open the script(s) in the external editor.
	 */
	private void editExternal(final LayoutWindow lw, final Collection<ObjectInfo> sel)
	{
		final ArrayList<File> tempFiles = new ArrayList<File>();
		final ArrayList<String> hashes = new ArrayList<String>();

		// Iterate over all selected scripted objects and create tempfiles
		for (ObjectInfo oi : sel)
		{
			if (oi.getObject() instanceof ScriptedObject)
			{
				try
				{
					ScriptedObject scr = (ScriptedObject)oi.getObject();

					// Save the scripts content to a file and hash it
					File newTemp = File.createTempFile(safeName(oi.getName()) + "-", ".bsh");

					System.out.println("Created temporary file: " + newTemp.getAbsolutePath());

					saveToFile(scr.getScript(), newTemp);
					hashes.add(hash(scr.getScript()));
					tempFiles.add(newTemp);
				}
				catch (IOException e)
				{
					System.err.println("Oops while creating temp file:");
					e.printStackTrace();

					cleanup(tempFiles);
					return;
				}
			}
		}

		// Do nothing if nothing is selected.
		if (tempFiles.size() == 0)
		{
			System.out.println("Nothing is selected, aborting.");
			return;
		}

		// Create array which contains all filenames
		String[] args = new String[tempFiles.size()];
		for (int i = 0; i < tempFiles.size(); i++)
		{
			args[i] = tempFiles.get(i).getAbsolutePath();
		}

		// Edit them
		Process editProcess = null;
		try
		{
			String[] cmdline = parseCmdline(PrefManager.getString("editorCommand"));
			String[] allArgs = cat(cmdline, args);

			System.out.print("Editor args:\n");
			for (String s : allArgs)
				System.out.print("|" + s + "|\n");
			System.out.println();

			editProcess = Runtime.getRuntime().exec(allArgs);
		}
		catch (IOException e)
		{
			System.err.println("Oops while launching your editor:");
			e.printStackTrace();

			cleanup(tempFiles);
			return;
		}

		// Wait for the editor to quit
		try
		{
			editProcess.waitFor();
		}
		catch (InterruptedException e)
		{
			System.err.println("Oops while waiting for your editor:");
			e.printStackTrace();

			// Note: No call to cleanup() to prevent edited scripts from deletion.
			return;
		}

		// Ok, the editor has returned. So, to avoid further inconsistency,
		// do the whole object-manipulation-stuff directly on the EDT.
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Re-read the scripts with undo records
				int i = 0;
				boolean modified = false;
				for (ObjectInfo oi : sel)
				{
					if (oi.getObject() instanceof ScriptedObject)
					{
						try
						{
							String content = readFromFile(tempFiles.get(i));
							String newHash = hash(content);

							System.out.println("Comparing hashes for object "
									+ oi.getName() + ", " + oi.getObject() + ":\n\t"
									+ hashes.get(i) + "\n\t"
									+ newHash);

							// See if this object is still valid
							if (objectValid(oi, lw))
							{
								// Only update the object if the script has changed
								// or if no valid hash algorithm was used
								if (!newHash.equals(hashes.get(i)) || newHash.equals("INVALID"))
								{
									System.out.println("Creating undo record and updating: "
											+ oi.getName() + ", " + oi.getObject());

									lw.setUndoRecord(new UndoRecord(lw, false,
											UndoRecord.COPY_OBJECT,
											new Object [] {
												oi.getObject(),
												oi.getObject().duplicate()}));

									((ScriptedObject)oi.getObject()).setScript(content);
									lw.getScene().objectModified(oi.getObject());
									modified = true;
								}
							}
							else
							{
								System.err.println("Oops: This object is no longer valid: "
										+ oi.getName() + ", " + oi.getObject());
							}
						}
						catch (IOException e)
						{
							System.err.println("Oops while re-reading " + tempFiles.get(i) + ":");
							e.printStackTrace();

							// Note: No call to cleanup() to prevent edited scripts from deletion.
							return;
						}

						i++;
					}
				}

				if (modified)
				{
					lw.updateImage();
					lw.updateMenus();
				}

				// Cleanup and quit
				cleanup(tempFiles);
			}
		});
	}

	/**
	 * Perform some checks: Is it safe to update this object?
	 * This is executed on the EDT but we can *NOT* be sure
	 * that no other threads are manipulating our objects in
	 * the meantime. To resolve this, a lot of synchronisation
	 * with AOIs core would be needed.
	 *
	 * However, it can detect whether the scene is still there
	 * and whether an object still exists in this scene.
	 */
	private boolean objectValid(ObjectInfo oi, LayoutWindow lw)
	{
		// Does the scene exist?
		Scene sc = lw.getScene();
		if (sc == null)
			return false;

		// Does the object exist in this scene?
		ObjectInfo inScene = sc.getObjectById(oi.getId());
		if (inScene == null)
			return false;

		return true;
	}

	/**
	 * Split up a command line that may contain quoted strings:
	 *
	 * "C:\My Programs\Some\Program.exe" --tabs   --theme "Cool Theme"
	 *  |=============================|  |====|   |=====|  |========|
	 */
	private String[] parseCmdline(String in)
	{
		// Match everything that is either:
		// - A quoted string or
		// - Something that does not contain a quote or a space,
		//   i.e. a normal token
		Pattern p = Pattern.compile("(\"[^\"]*\")|([^\"\\s]*)");
		Matcher m = p.matcher(in);

		ArrayList<String> buf = new ArrayList<String>();

		while (m.find())
		{
			String match = in.substring(m.start(), m.end());

			if (match.startsWith("\""))
			{
				// Remove quotes from quoted tokens, then add it
				match = match.substring(1, match.length() - 1);
				buf.add(match);
			}
			else
			{
				// Trim normal tokens but don't add empty ones
				match = match.trim();
				if (!match.equals(""))
					buf.add(match);
			}
		}

		return buf.toArray(new String[] {});
	}

	/**
	 * Remove all temporary files
	 */
	private void cleanup(ArrayList<File> t)
	{
		for (File f : t)
		{
			if (!f.delete())
			{
				System.err.println("Oops while deleting file: " + f);
			}
			else
			{
				System.out.println("Removed temporary file: " + f.getAbsolutePath());
			}
		}
	}

	/**
	 * Concatenate two String arrays
	 */
	private String[] cat(String[] a, String[] b)
	{
		ArrayList<String> both = new ArrayList<String>();
		Collections.addAll(both, a);
		Collections.addAll(both, b);
		return both.toArray(new String[] {});
	}

	/**
	 * Converts an object name into a string which could be used as a
	 * part of a filename.
	 */
	static private String safeName(String str)
	{
		int maxlen = 15;

		str = str.trim();

		// Replace all non-basic characters
		str = str.replaceAll("[^a-zA-Z0-9]", "_");

		// Trim size
		if (str.length() > maxlen)
			str = str.substring(0, maxlen);

		while (str.length() < 3)
			str += "x";

		return str;
	}

	/**
	 * Write the script of this scripted object into its temporary file
	 */
	private void saveToFile(String str, File target) throws IOException
	{
		FileWriter fw = new FileWriter(target);
		fw.write(str);
		fw.close();
	}

	/**
	 * Read the script from the specified file
	 */
	private String readFromFile(File input) throws IOException
	{
		// Users most probably don't create scripts which are larger
		// than Integer.MAX_VALUE...
		byte[] bytes = new byte[(int)input.length()];

		FileInputStream fis = new FileInputStream(input);
		fis.read(bytes);
		fis.close();

		return new String(bytes);
	}

	/**
	 * Hash the given string.
	 */
	private String hash(String in)
	{
		String out = "";
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(in.getBytes());
			for (byte b : digest)
				out += String.format("%02x", b);
		}
		catch (Exception e)
		{
			//e.printStackTrace(); 		// <--- this causes AoI to report an alert and stop.
			return "INVALID";
		}

		return out;
	}
}
