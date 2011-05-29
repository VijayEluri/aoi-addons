/*
	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	General Public License for more details.
*/

package artofillusion.translators;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;

import java.io.*;
import java.util.*;

import buoy.widget.*;

public class Sketch3DExporter
{
	protected static String getName(List<String> seenNames, String basename)
	{
		basename = basename.replaceAll("[^a-zA-Z0-9]", "_");
		String name = new String(basename);
		int ext = 2;
		while (seenNames.contains(name))
			name = basename + "_" + (ext++);
		seenNames.add(name);
		return name;
	}

	public static void exportObject(Collection<ObjectInfo> objs, File target)
	{
		try
		{
			FileWriter fw = new FileWriter(target);
			BufferedWriter bw = new BufferedWriter(fw);

			// Maintain a list of object names that we've seen. Do this
			// to avoid duplicate names.
			List<String> seenNames = new ArrayList<String>();

			for (ObjectInfo oi : objs)
			{
				if (!(oi.getObject() instanceof FacetedMesh
						|| oi.getObject() instanceof Curve
						|| oi.getObject() instanceof NullObject))
					continue;

				if (!oi.isVisible())
					continue;

				// Figure out the name for this object.
				String name = getName(seenNames, oi.getName());

				// Write this object. Transform vertices into global
				// coordinates.
				// (TODO: Make this an option.)
				bw.write("def " + name + " ");

				if (oi.getObject() instanceof FacetedMesh)
				{
					bw.write("{");
					bw.newLine();

					Mat4 trans = oi.getCoords().fromLocal();
					FacetedMesh fm = (FacetedMesh)oi.getObject();

					for (int f = 0; f < fm.getFaceCount(); f++)
					{
						// Start a new polygon.
						bw.write("\tpolygon ");
						for (int v = 0; v < fm.getFaceVertexCount(f); v++)
						{
							int fvi = fm.getFaceVertexIndex(f, v);
							Vec3 vert = trans.times(fm.getVertices()[fvi].r);
							bw.write("("
									+ vert.x + ", "
									+ vert.y + ", "
									+ vert.z
									+ ")");
						}
						bw.newLine();
					}

					bw.write("}");
				}
				else if (oi.getObject() instanceof NullObject)
				{
					Vec3 vert = oi.getCoords().getOrigin();
					bw.write("("
							+ vert.x + ", "
							+ vert.y + ", "
							+ vert.z
							+ ")");
				}
				else if (oi.getObject() instanceof Curve)
				{
					bw.write("{");
					bw.newLine();

					Mat4 trans = oi.getCoords().fromLocal();
					Curve c = (Curve)oi.getObject();
					int max;
					if (c.isClosed())
						max = c.getVertices().length;
					else
						max = c.getVertices().length - 1;

					for (int i = 0; i < max; i++)
					{
						int now = i;
						bw.write("\tline ");
						Vec3 vert = trans.times(c.getVertices()[now].r);
						bw.write("("
								+ vert.x + ", "
								+ vert.y + ", "
								+ vert.z
								+ ")");

						now = (i + 1) % c.getVertices().length;
						vert = trans.times(c.getVertices()[now].r);
						bw.write("("
								+ vert.x + ", "
								+ vert.y + ", "
								+ vert.z
								+ ")");

						bw.newLine();
					}

					bw.write("}");
				}

				bw.newLine();
				bw.newLine();
			}

			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
