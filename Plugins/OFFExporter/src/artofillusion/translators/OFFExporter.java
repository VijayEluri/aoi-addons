/* This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.translators;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;

import java.io.*;

import buoy.widget.*;

public class OFFExporter
{
	public static void exportFile(BFrame parent, Scene theScene)
	{
		// komfortkram hier rein ...
		
		
		for (int i = 0; i < theScene.getNumObjects(); i++)
		{
			ObjectInfo oi = theScene.getObject(i);
			
			if (oi.getObject() instanceof FacetedMesh)
			{
				File out = new File("/tmp/" + i + ".off");
				exportObjectToFile(out, (FacetedMesh)oi.getObject());
			}
		}
	}
	
	private static void exportObjectToFile(File path, FacetedMesh fmesh)
	{
/*
		File ausgabeDatei = new File("vomWeb.html"); 
      // FileWriter erzeugen. 
      FileWriter fw = new FileWriter(ausgabeDatei); 
      // Den FileWriter in einem BufferedWriter verpacken. 
      BufferedWriter bw = new BufferedWriter(fw); 

      // 
      // Zeilenweise einlesen 
       // 
     String zeile = br.readLine(); 
      while (zeile != null) { 
	bw.write(zeile); 
	bw.newLine(); 
        zeile = br.readLine(); 
      } 
*/
		try
		{
			FileWriter fw = new FileWriter(path);
			BufferedWriter bw = new BufferedWriter(fw);
			
			// Header
			bw.write("OFF");
			bw.newLine();
			
			int numVertices = fmesh.getVertices().length;
			int numFaces    = fmesh.getFaceCount();
			
			bw.write(numVertices + " " + numFaces + " 0");
			bw.newLine();
			
			// Vertices
			MeshVertex[] mv = fmesh.getVertices();
			for (int i = 0; i < mv.length; i++)
			{
				bw.write(mv[i].r.x + " " + mv[i].r.y + " " + mv[i].r.z + " ");
				bw.newLine();
			}
			
			// Faces
			for (int i = 0; i < numFaces; i++)
			{
				int numIndices = fmesh.getFaceVertexCount(i);
				
				// Anzahl Vertices für dieses Face
				bw.write(numIndices + " ");
				
				// Dazugehörige Vertex-Indizes
				for (int index = 0; index < numIndices; index++)
				{
					bw.write(Integer.toString(fmesh.getFaceVertexIndex(i, index)));
					
					// ggf. Leerzeichen
					if (index < numIndices - 1)
					{
						bw.write(" ");
					}
				}
				
				bw.newLine();
			}
			
			bw.close();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
