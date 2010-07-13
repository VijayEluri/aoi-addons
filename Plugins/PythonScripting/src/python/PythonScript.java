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
	public PythonScript(String scriptText)
	{
		super(scriptText);
	}

	@Override
	public ObjectScript getObjectScript() throws EvalError
	{
		ObjectScript parsedScript = null;
		try
		{
			final ScriptEngine engine =
				new ScriptEngineManager().getEngineByName("python");

			final String myScript = getScript();

			parsedScript = new ObjectScript() {
				public void execute(ScriptedObjectController script)
				{
					try
					{
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

						engine.put("script", script);

						PrintWriter out = new PrintWriter(new ScriptOutputWindow());
						ScriptContext context = engine.getContext();
						context.setWriter(out);
						context.setErrorWriter(out);

						engine.eval(myScript);
					}
					catch (javax.script.ScriptException ex)
					{
						ex.printStackTrace();
					}
				}
			};
		}
		catch (final Exception ex)
		{
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					ScriptRunner.displayError(ex, 1);
				}
			});
			parsedScript = new ObjectScript() {
				public void execute(ScriptedObjectController script)
				{
				}
			};
		}
		return parsedScript;
	}
}

/* vim: set ts=2 sw=2 : */
