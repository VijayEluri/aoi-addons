package troy.blob;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;

public class BlobPlugin implements ModellingTool
{
	private static int counter = 1;

	/**
	 * Creates a new object and sets undo records.
	 */
	@Override
	public void commandSelected(LayoutWindow theWindow)
	{
		Scene theScene = theWindow.getScene();
		String cstr = "Blob " + (counter++);

		Object3D blob = new Blob();

		UndoRecord undo = new UndoRecord(theWindow, false);
		undo.addCommandAtBeginning(UndoRecord.SET_SCENE_SELECTION,
				new Object[] { theWindow.getSelectedIndices() });
		theWindow.addObject(blob, new CoordinateSystem(), cstr, undo);
		theWindow.setUndoRecord(undo);

		theWindow.setSelection(theScene.getNumObjects() - 1);
		theWindow.updateImage();
	}

	@Override
	public String getName()
	{
		return "Add a new blob";
	}
}
