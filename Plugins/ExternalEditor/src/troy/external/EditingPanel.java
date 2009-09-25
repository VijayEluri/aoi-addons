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

import java.awt.*;
import java.awt.event.*;

import artofillusion.ui.Translate;
import artofillusion.keystroke.*;

// this is situated in the main ArtOfIllusion.jar:
import buoy.event.*;
import buoy.widget.*;

// remember to include the PreferencesPlugin.jar in the CP for this one:
import artofillusion.preferences.*;

/**
 * Takes care about the dialog under "Edit" - "Plugin Preferences"
 */
public class EditingPanel implements PreferencesEditor
{
	private BTextField txtEditor   = new BTextField(20);
	private BButton    btnRegister = new BButton("...");

	@Override
	public String getName()
	{
		return Translate.text("ExternalEditor:pluginName");
	}

	@Override
	public Widget getPreferencesPanel()
	{
		updateWidgets();

		// Build layout
		LayoutInfo growInsets = new LayoutInfo(LayoutInfo.CENTER,
				LayoutInfo.HORIZONTAL, new Insets(3, 3, 3, 3), null);
		FormContainer fc = new FormContainer(new double [] {0.0, 1.0}, new double [2]);
		fc.add(new BLabel(Translate.text("ExternalEditor:editorCommand")), 0, 0, growInsets);
		fc.add(txtEditor, 1, 0, growInsets);

		fc.add(new BLabel(Translate.text("ExternalEditor:registerHotkey")), 0, 1, growInsets);
		fc.add(btnRegister, 1, 1, growInsets);

		// Event links
		btnRegister.addEventLink(CommandEvent.class, this, "registerHotkey");

		return fc;
	}

	private void updateWidgets()
	{
		txtEditor.setText(PrefManager.getString("editorCommand"));
		btnRegister.setText(Translate.text("ExternalEditor:registerNow"));
	}

	@Override
	public void savePreferences()
	{
		PrefManager.putString("editorCommand", txtEditor.getText());
		PrefManager.commitSettings();
	}

	protected void registerHotkey()
	{
		KeystrokeManager.addRecord(new KeystrokeRecord(KeyEvent.VK_F4, 0, getName(),
			"ModellingTool plugin = (ModellingTool)PluginRegistry" +
				".getPluginObject(\"troy.external.ExternalEditorPlugin\");\n" +
			"if (plugin != null)\n" +
				"\tplugin.commandSelected(window);\n" +
			"else\n" +
				"\tprint(\"ExternalEditor plugin not found.\");\n"));

		new BStandardDialog
			(
				getName(),
				Translate.text("ExternalEditor:registerDone"),
				BStandardDialog.INFORMATION
			).showMessageDialog(null);

	}
}
