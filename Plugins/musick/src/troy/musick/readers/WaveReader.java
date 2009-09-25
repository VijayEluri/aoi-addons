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

import java.io.*;

/**
 * MusickReader for PCM WAVE files
 * @author TroY
 */
public class WaveReader implements MusickReader
{
	private String filename = null;
	private byte[] data = null;
	private WaveInfo info = null;

	// ---------------------------------------------------------------------
	// MusickReader-Interface stuff
	
	/**
	 * A method to verify if this reader can actually read the given file
	 * Looks a bit too detailed, but: if the file passes this test, only
	 * a very corrupt file could throw an error later on
	 */
	@Override
	public boolean canRead(String path)
	{
		RandomAccessFile ff = open(path, "r");
		if (ff == null)
			return false;

		// check first 4 bytes for "RIFF"
		byte[] four = new byte[4];
		if (!read(ff, four))
			return false;

		if (!(new String(four).equals("RIFF")))
			return false;

		// skip 4 bytes (it's the length)
		if (!skip(ff, 4))
			return false;

		// check next 4 bytes for "WAVE"
		if (!read(ff, four))
			return false;

		if (!(new String(four).equals("WAVE")))
			return false;

		// fmt-chunk must follow now
		if (!read(ff, four))
			return false;

		if (!(new String(four).equals("fmt ")))
			return false;

		// must be PCM
		if (!skip(ff, 4))
			return false;

		if (!read(ff, four))
			return false;

		int format = swapOrder(four, 0, 2);
		if (format != 1)
			return false;

		// skip to data-chunk and read
		if (!skip(ff, 12))
			return false;

		if (!read(ff, four))
			return false;

		if (!(new String(four).equals("data")))
			return false;

		// now if it can be closed, everything is alright
		if (!close(ff))
			return false;

		return true;
	}		

	/**
	 * Attach this reader to this file
	 */
	@Override
	public void attach(String path)
	{
		filename = path;
	}

	/**
	 * Get the filename which belongs to this reader
	 */
	@Override
	public String getFile()
	{
		return filename;
	}

	/**
	 * Get the scope starting at this time, scaled from -1.0 to 1.0,
	 * duration according to given length
	 * NOTE: YOU have to take care of the time offset! it's stored in the "job" object
	 */
	@Override
	public double[] getScope(double time, MusickObject.ControlledObject job)
	{
		if (data == null || info == null)
		{
			if (!loadFile())
			{
				// file could not be loaded ... reset data to null to try it again
				// the next time this method is called. return an array full of 0.0
				data = null;
				return new double[job.scopeLen];
			}
		}

		// First of all, test if requested channels fit the ones in the file
		if (
				   (job.channels == 4 && info.channels == 2)
				|| (job.channels != 4 && info.channels == 1)
			)
		{
			// mono request but stereo found or the other way round
			System.out.println("WaveReader reports: File is MONO but you requested STEREO. Or the other way round. Returning zeroes.");
			return new double[job.scopeLen];
		}

		// Now, copy as much data as possible. Rest will be zeroes.
		double[] out = null;
		if (info.channels == 1)
		{
			// Ok, this is a MONO file. Easy.
			
			out = new double[job.scopeLen];

			// find the position in the array where we will start
			int startindex = (int)((time + job.timeOffset) * info.bytesPerSecond);
			// "step size"
			int increment  = info.blockAlign;

			// "mod-check": startindex has to be divisible w/o remainder by blockAlign
			// this will warp us safely to the beginning of a block
			while ((startindex % info.blockAlign) != 0)
				startindex++;
			
			// this for-loop (as the one for stereo) will cycle through *all* possible
			// indices, whether they are valid or not. the inner if checks this.
			// as outAt still gets counted, every valid entry will be filled and all
			// others are left at 0.0
			int outAt = 0;
			for (int at = startindex; at < startindex + (info.blockAlign * job.scopeLen); at += increment)
			{
				if (at >= 0 && (at + info.blockAlign) < data.length)
				{
					// divide by 2^[bpsam-1], because this value is signed
					// this will scale it down to ]-1.0, 1.0[

					// the cast to "short" is VERY important - it gets the sign back in!
					short value = (short)swapOrder(data, at, info.blockAlign);
					out[outAt] = value / (Math.pow(2.0, info.bitsPerSample - 1));
				}
				outAt++;
			}
		}
		else
		{
			// STEREO. but more difficult.

			// decide: are we dropping one channel? or are we sending full stereo?
			if (job.channels == 0)
			{
				// full stereo
				out = new double[job.scopeLen * 2];
			}
			else
			{
				// drop or avg
				out = new double[job.scopeLen];
			}

			int startindex = (int)((time + job.timeOffset) * info.bytesPerSecond);
			int increment  = info.blockAlign;

			// "mod-check": startindex has to be divisible w/o remainder by blockAlign
			// this will warp us safely to the beginning of a block
			while ((startindex % info.blockAlign) != 0)
				startindex++;
			
			int outAt = 0;
			for (int at = startindex; at < startindex + (info.blockAlign * job.scopeLen); at += increment)
			{
				if (at >= 0 && (at + info.blockAlign) < data.length)
				{
					short left  = (short)swapOrder(data, at, info.blockAlign / 2);
					short right = (short)swapOrder(data, at + info.blockAlign / 2, info.blockAlign / 2);
					if (job.channels == 0)
					{
						// both channels, left first, then right
						out[outAt]   = left / Math.pow(2.0, info.bitsPerSample - 1);
						out[outAt+1] = right / Math.pow(2.0, info.bitsPerSample - 1);
					}
					else if (job.channels == 1)
					{
						// left only
						out[outAt] = left / Math.pow(2.0, info.bitsPerSample - 1);
					}
					else if (job.channels == 2)
					{
						// left only
						out[outAt] = right / Math.pow(2.0, info.bitsPerSample - 1);
					}
					else if (job.channels == 3)
					{
						// average
						out[outAt] = left;
						out[outAt] += right;
						out[outAt] = out[outAt] / (2.0 * Math.pow(2.0, info.bitsPerSample - 1));
					}
				}

				if (job.channels == 0)
					outAt += 2;
				else
					outAt++;
			}
		}

		return out;
	}


	// ---------------------------------------------------------------------
	// File loading
	
	/**
	 * NO format checks in here! This has to be done BEFORE via canRead()!
	 */
	private boolean loadFile()
	{
		RandomAccessFile ff = open(getFile(), "r");
		if (ff == null)
			return false;

		// this reads all necessary info and skips to the first byte of data
		info = new WaveInfo(ff, this);
		System.out.println("WaveReader reports: Specs for the file " + getFile()
				+ ":\n" + info.toString());

		// now read all data at once
		data = new byte[info.numBytes];
		if (!read(ff, data))
		{
			System.out.println("WaveReader reports: FATAL ERROR WHILE READING " + getFile());
			return false;
		}

		if (!close(ff))
		{
			System.out.println("WaveReader reports: FATAL ERROR WHILE CLOSING " + getFile());
			return false;
		}

		System.out.println("WaveReader reports: " + getFile() + " successfully read.");

		return true;
	}


	// ---------------------------------------------------------------------
	// File reading abstraction
	
	protected RandomAccessFile open(String path, String mode)
	{
		RandomAccessFile ff = null;
		try
		{
			ff = new RandomAccessFile(path, mode);
		}
		catch (FileNotFoundException e)
		{
			return null;
		}

		return ff;
	}

	protected boolean read(RandomAccessFile ff, byte[] buf)
	{
		try
		{
			ff.readFully(buf);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	protected boolean skip(RandomAccessFile ff, int num)
	{
		try
		{
			ff.skipBytes(num);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	protected boolean close(RandomAccessFile ff)
	{
		try
		{
			ff.close();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}


	// ---------------------------------------------------------------------
	// Math help stuff
	
	protected int swapOrder(byte[] arr, int offset, int len)
	{
		// 0xFF is necessary because java doesn't know unsigned bytes.
		// this will kill all high bits and so the sign will get lost.
		int out = 0;
		for (int i = offset; i < arr.length && (i - offset) < len; i++)
		{
			out += ((arr[i] & 0xFF) << ((i - offset) * 8));
		}
		return out;
	}


	// ---------------------------------------------------------------------
	// INNER CLASS: WaveInfo
	public static class WaveInfo
	{
		public int channels = 0;
		public int sampleRate = 0;
		public int bytesPerSecond = 0;
		public int blockAlign = 0;
		public int bitsPerSample = 0;
		public int numBytes = 0;

		public WaveInfo(RandomAccessFile ff, WaveReader p)
		{
			byte[] allinfo = new byte[22];

			// skip down to relevant info
			p.skip(ff, 22);
			p.read(ff, allinfo);

			channels		= p.swapOrder(allinfo, 0, 2);
			sampleRate		= p.swapOrder(allinfo, 2, 4);
			bytesPerSecond	= p.swapOrder(allinfo, 6, 4);
			blockAlign		= p.swapOrder(allinfo, 10, 2);
			bitsPerSample	= p.swapOrder(allinfo, 12, 2);
			numBytes		= p.swapOrder(allinfo, 18, 4);
		}

		public String toString()
		{
			return "WaveInfo["
				+ "channels=" + channels
				+ ",sampleRate=" + sampleRate
				+ ",bytesPerSecond=" + bytesPerSecond
				+ ",blockAlign=" + blockAlign
				+ ",bitsPerSample=" + bitsPerSample
				+ ",numBytes=" + numBytes
				+ "]";
		}
	}
}
