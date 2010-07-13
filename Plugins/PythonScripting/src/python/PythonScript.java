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

public class PythonScript extends ScriptedObject
{
	/** Invoke constructor of ScriptedObject. */
	public PythonScript(String scriptText)
	{
		super(scriptText);
	}

	/** Use Jython to execute the script. Provide access to AoI classes, a
	 * binding to "script" and do error handling. */
	@Override
	public ObjectScript getObjectScript() throws EvalError
	{
		ObjectScript readyToExecute = null;
		final ScriptEngine engine =
			new ScriptEngineManager().getEngineByName("python");

		final String myScript = getScript();

		// Prepare an "ObjectScript". This will be run by a
		// ScriptedObjectController.
		readyToExecute = new ObjectScript()
		{
			public void execute(ScriptedObjectController script)
			{
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

					// The "script" object which provides access to the scene etc.
					engine.put("script", script);

					// Set up output channels. That is, throw all output into the
					// script output window.
					Interpreter interp = ScriptRunner.getInterpreter();
					PrintWriter out = new PrintWriter(interp.getOut());
					ScriptContext context = engine.getContext();
					context.setWriter(out);
					context.setErrorWriter(out);

					// Run the script.
					engine.eval(myScript);
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
		};
		return readyToExecute;
	}
}

/* vim: set ts=2 sw=2 : */
