/*
	Copyright (C) 2010 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	General Public License for more details.
*/


package python;

import artofillusion.*;
import artofillusion.script.*;

import java.awt.*;
import java.io.*;
import javax.script.*;
import bsh.*;

public class PythonRunner
{
	/** Use Jython to execute the script. Provide access to AoI classes, a
	 * binding to "script" and do error handling. */
	public static void run(String script, Object bound)
	{
		final ScriptEngine engine =
			new ScriptEngineManager().getEngineByName("python");

		try
		{
			// Those are copied from ScriptRunner.getInterpreter().
			engine.eval("from artofillusion import *");
			engine.eval("from artofillusion.image import *");
			engine.eval("from artofillusion.material import *");
			engine.eval("from artofillusion.math import *");
			engine.eval("from artofillusion.object import *");
			engine.eval("from artofillusion.script import *");
			engine.eval("from artofillusion.texture import *");
			engine.eval("from artofillusion.ui import *");
			engine.eval("from buoy.event import *");
			engine.eval("from buoy.widget import *");

			// Object or Tool script?
			if (bound instanceof LayoutWindow)
			{
				engine.put("window", (LayoutWindow)bound);
			}
			else if (bound instanceof ScriptedObjectController)
			{
				engine.put("script", (ScriptedObjectController)bound);
			}

			// Set up output channels. That is, throw all output into the
			// script output window.
			Interpreter interp = ScriptRunner.getInterpreter();
			PrintWriter out = new PrintWriter(interp.getOut());
			ScriptContext context = engine.getContext();
			context.setWriter(out);
			context.setErrorWriter(out);

			// Run the script.
			engine.eval(script);
		}
		catch (final javax.script.ScriptException ex)
		{
			// When an error happens, display the appropriate dialog and
			// show the stack trace (both will be done by displayError()).
			// It's important that this happens on AWT's event thread.
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					ScriptRunner.displayError(ex, -1);
				}
			});
		}
	}
}

/* vim: set ts=2 sw=2 : */
