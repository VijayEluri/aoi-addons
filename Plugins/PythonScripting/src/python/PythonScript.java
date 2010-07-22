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
		final String script = getScript();

		// Prepare an "ObjectScript".
		readyToExecute = new ObjectScript()
		{
			public void execute(ScriptedObjectController controller)
			{
				PythonRunner.run(script, controller);
			}
		};
		return readyToExecute;
	}
}

/* vim: set ts=2 sw=2 : */
