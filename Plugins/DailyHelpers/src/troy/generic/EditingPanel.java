/*
	Copyright (C) 2008 by Peter H. ("TroY")

	This program is free software; you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation; either version 2 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY 
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
	PARTICULAR PURPOSE.  See the GNU General Public License for more details.
*/

package troy.generic;

import javax.swing.BorderFactory;
import java.awt.Insets;
import artofillusion.ui.*;

// this is situated in the main ArtOfIllusion.jar:
import buoy.widget.*;

// remember to include the PreferencesPlugin.jar in the CP for this one:
import artofillusion.preferences.*;

public class EditingPanel implements PreferencesEditor
{
	private ColumnContainer cc    = null;

	private BCheckBox boxMaximize = new BCheckBox("max", false);
	private BCheckBox boxShow     = new BCheckBox("grid", false);
	private BCheckBox boxSnap     = new BCheckBox("snap", false);
	
	private BCheckBox boxAmbient  = new BCheckBox("ambient", false);
	private BCheckBox boxLight    = new BCheckBox("light", false);
	private BCheckBox boxCam      = new BCheckBox("cam", false);
	
	private BCheckBox boxAutoBackup = new BCheckBox("autobackup", false);
	private BTextField textAutoBackupLevels = new BTextField();
	private BLabel labelAutoBackupLevels = new BLabel("lvl");

	private BCheckBox boxAutoSave = new BCheckBox("autosave", false);
	private BTextField textAutoSaveMinutes = new BTextField();
	private BLabel labelAutoSaveMinutes = new BLabel("min");

	private void updateWidgetTitles()
	{
		boxMaximize.setText(Translate.text("dailyhelpers:editing.maximize"));
		boxShow.setText(Translate.text("dailyhelpers:editing.showGrid"));
		boxSnap.setText(Translate.text("dailyhelpers:editing.snapToGrid"));
		
		boxAmbient.setText(Translate.text("dailyhelpers:editing.resetAmbient"));
		boxLight.setText(Translate.text("dailyhelpers:editing.killLight"));
		boxCam.setText(Translate.text("dailyhelpers:editing.alterCam"));
		
		boxAutoBackup.setText(Translate.text("dailyhelpers:editing.enableAutoBackup"));
		labelAutoBackupLevels.setText(Translate.text("dailyhelpers:editing.autoBackupLevels"));

		boxAutoSave.setText(Translate.text("dailyhelpers:editing.enableAutoSave"));
		labelAutoSaveMinutes.setText(Translate.text("dailyhelpers:editing.autoSaveMinutes"));
	}

	public String getName()
	{
		return "DailyHelpers";
	}
	
	public Widget getPreferencesPanel()
	{
		updateWidgetTitles();
	
		// fire up the panel
		if (cc == null)
		{
			cc                       = new ColumnContainer();
			ColumnContainer ccFirst  = new ColumnContainer();
			ColumnContainer ccSecond = new ColumnContainer();
			ColumnContainer ccAB     = new ColumnContainer();
			ColumnContainer ccAS     = new ColumnContainer();
	
			LayoutInfo grow       = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.HORIZONTAL, null, null);
			LayoutInfo growInsets = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(10, 10, 10, 10), null);
			LayoutInfo growCenter = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, null, null);
		
		
			// group for "all new scenes"
			ccFirst.add(boxMaximize, grow);
			ccFirst.add(boxShow, grow);
			ccFirst.add(boxSnap, grow);
		
			BorderContainer bcFirst = new BorderContainer();
			bcFirst.setDefaultLayout(growInsets);
			bcFirst.add(new BOutline(ccFirst, BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), Translate.text("dailyhelpers:editing.newAndOpenedGroup"))), BorderContainer.CENTER);

			cc.add(bcFirst, growCenter);
		
		
			// group for "only new scenes"
			ccSecond.add(boxAmbient, grow);
			ccSecond.add(boxLight, grow);
			ccSecond.add(boxCam, grow);
		
			BorderContainer bcSecond = new BorderContainer();
			bcSecond.setDefaultLayout(growInsets);
			bcSecond.add(new BOutline(ccSecond, BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), Translate.text("dailyhelpers:editing.onlyNewGroup"))), BorderContainer.CENTER);
		
			cc.add(bcSecond, growCenter);
			
			
			// group for "autobackup"
			ccAB.add(boxAutoBackup, grow);

			GridContainer texts = new GridContainer(2, 1);
			texts.add(labelAutoBackupLevels, 0, 0, growInsets);
			texts.add(textAutoBackupLevels, 1, 0, growInsets);			
			ccAB.add(texts, grow);
		
			BorderContainer bcAB = new BorderContainer();
			bcAB.setDefaultLayout(growInsets);
			bcAB.add(new BOutline(ccAB, BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), Translate.text("dailyhelpers:editing.autoBackupGroup"))), BorderContainer.CENTER);
		
			cc.add(bcAB, growCenter);


			// group for "autosave"
			ccAS.add(boxAutoSave, grow);

			texts = new GridContainer(2, 1);
			texts.add(labelAutoSaveMinutes, 0, 0, growInsets);
			texts.add(textAutoSaveMinutes, 1, 0, growInsets);
			ccAS.add(texts, grow);

			BorderContainer bcAS = new BorderContainer();
			bcAS.setDefaultLayout(growInsets);
			bcAS.add(new BOutline(ccAS, BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), Translate.text("dailyhelpers:editing.autoSaveGroup"))), BorderContainer.CENTER);

			cc.add(bcAS, growCenter);
		}
		
		// update items
		boxMaximize.setState(DailyHelpersPlugin.getBoolean("maximize"));
		boxShow.setState(DailyHelpersPlugin.getBoolean("showGrid"));
		boxSnap.setState(DailyHelpersPlugin.getBoolean("snapToGrid"));
		boxAmbient.setState(DailyHelpersPlugin.getBoolean("resetAmbient"));
		boxLight.setState(DailyHelpersPlugin.getBoolean("killDirectionalLight"));
		boxCam.setState(DailyHelpersPlugin.getBoolean("alterCam"));
		
		boxAutoBackup.setState(DailyHelpersPlugin.getBoolean("autoBackup"));
		textAutoBackupLevels.setText(Integer.toString(DailyHelpersPlugin.getInt("autoBackupLevels")));

		boxAutoSave.setState(DailyHelpersPlugin.getBoolean("autoSave"));
		textAutoSaveMinutes.setText(Integer.toString(DailyHelpersPlugin.getInt("autoSaveMinutes")));
		
		return cc;
	}
	
	public void savePreferences()
	{
		// Main props
		DailyHelpersPlugin.putBoolean("maximize", boxMaximize.getState());
		DailyHelpersPlugin.putBoolean("showGrid", boxShow.getState());
		DailyHelpersPlugin.putBoolean("snapToGrid", boxSnap.getState());
		DailyHelpersPlugin.putBoolean("resetAmbient", boxAmbient.getState());
		DailyHelpersPlugin.putBoolean("killDirectionalLight", boxLight.getState());
		DailyHelpersPlugin.putBoolean("alterCam", boxCam.getState());
		
		// AutoBackup
		DailyHelpersPlugin.putBoolean("autoBackup", boxAutoBackup.getState());
		DailyHelpersPlugin.putInt("autoBackupLevels", Integer.parseInt(textAutoBackupLevels.getText()));

		// AutoSave
		int oldMinutes = DailyHelpersPlugin.getInt("autoSaveMinutes");
		int newMinutes = Integer.parseInt(textAutoSaveMinutes.getText());

		boolean oldStatus = DailyHelpersPlugin.getBoolean("autoSave");
		boolean newStatus = boxAutoSave.getState();

		if (newMinutes != oldMinutes)
		{
			new BStandardDialog
				(
					Translate.text("dailyhelpers:editing.autoSaveGroup"),
					Translate.text("dailyhelpers:autosave.restartWarning"),
					BStandardDialog.WARNING
				).showMessageDialog(null);
		}
		DailyHelpersPlugin.putBoolean("autoSave", newStatus);
		DailyHelpersPlugin.putInt("autoSaveMinutes", newMinutes);

		// Update AutoSaver-status
		if (boxAutoSave.getState())
			AutoSaveFunctions.enable();
		else
			AutoSaveFunctions.disable();
	
		// Commit
		try
		{
			DailyHelpersPlugin.commitSettings();
		}
		catch (Exception ex)
		{
			System.out.println("DailyHelpers/EditingPanel/savePreferences: COMMIT FAILED! See exception below.");
			ex.printStackTrace();
		}
	}
}

