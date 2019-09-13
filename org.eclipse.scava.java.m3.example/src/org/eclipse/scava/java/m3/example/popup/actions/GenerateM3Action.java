package org.eclipse.scava.java.m3.example.popup.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.scava.java.m3.M3Java;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.collect.Multimap;

import io.usethesource.vallang.IValue;

public class GenerateM3Action implements IObjectActionDelegate {

	private Shell shell;
	private IProject project;

	/**
	 * Constructor for Action1.
	 */
	public GenerateM3Action() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		System.out.println("Generating M3 for project " + project.getName());

		try {
			M3Java extractor = new M3Java();
			IValue m3 = extractor.createM3FromEclipseProject(project.getName());
			
			// Each key in the map is a method declaration
			// The set of values associated to a key is the set of method invocations
			Multimap<String, String> methodInvocations = extractor.extractMethodInvocations(m3);

			MessageDialog.openInformation(shell, "M3",
					"M3 has been generated. " + methodInvocations.size() + " method invocations found.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TreeSelection) {
			Object elem = ((TreeSelection) selection).getFirstElement();
			if (elem instanceof IProject) {
				project = (IProject) elem;
			}
		}
	}

}
