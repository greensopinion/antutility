/*******************************************************************************
 * Copyright (c) 2007, 2009 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package net.java.dev.antutility.internal.ui.editor;

import net.java.dev.antutility.internal.core.BuildMetrics;
import net.java.dev.antutility.internal.core.Locator;
import net.java.dev.antutility.internal.core.MeasurementAccumulator;
import net.java.dev.antutility.internal.ui.AntUtilityUi;
import net.java.dev.antutility.internal.ui.AntUtilityUiPlugin;
import net.java.dev.antutility.internal.ui.BuildMetricsEditorInput;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class BuildMetricsEditor extends EditorPart {

	private TreeViewer viewer;

	public BuildMetricsEditor() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent,SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL| SWT.FULL_SELECTION);
		viewer.setContentProvider(new BuildMetricsTreeContentProvider());

		getSite().setSelectionProvider(viewer); // ORDER DEPENDENCY
		
		final Tree tree = viewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
				
		TreeViewerColumn column;
		
		column = new TreeViewerColumn(viewer,SWT.LEFT);
		column.getColumn().setText("Name");
		column.setLabelProvider(new NameColumnLabelProvider());
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);

		column = new TreeViewerColumn(viewer,SWT.RIGHT);
		column.getColumn().setText("Self Time (ms)");
		column.setLabelProvider(new LocalTimeColumnLabelProvider());
		column.getColumn().setWidth(100);
		column.getColumn().setMoveable(true);

		column = new TreeViewerColumn(viewer,SWT.RIGHT);
		column.getColumn().setText("Total Time (ms)");
		column.setLabelProvider(new TotalTimeColumnLabelProvider());
		column.getColumn().setWidth(100);
		column.getColumn().setMoveable(true);
		
		column = new TreeViewerColumn(viewer,SWT.RIGHT);
		column.getColumn().setText("Child Time (ms)");
		column.setLabelProvider(new ChildTimeColumnLabelProvider());
		column.getColumn().setWidth(100);
		column.getColumn().setMoveable(true);

		column = new TreeViewerColumn(viewer,SWT.RIGHT);
		column.getColumn().setText("Invocation Count");
		column.setLabelProvider(new InvocationCountColumnLabelProvider());
		column.getColumn().setWidth(100);
		column.getColumn().setMoveable(true);

		
		column = new TreeViewerColumn(viewer,SWT.LEFT);
		column.getColumn().setText("Location");
		column.setLabelProvider(new LocationColumnLabelProvider());
		column.getColumn().setWidth(500);
		column.getColumn().setMoveable(true);

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object element = ((IStructuredSelection)selection).getFirstElement();
					if (element instanceof MeasurementAccumulator) {
						MeasurementAccumulator accumulator = (MeasurementAccumulator) element;
						BuildMetricsEditor.this.open(accumulator.location);
					}
				}
			}

		});
		
		viewer.setInput(getBuildMetrics());
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void open(Locator location) {
		if (location != null) {
			String fileName = location.getFileName();
			if (fileName != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
				if (file != null) {
					try {
						IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
						if (location.getLineNumber() > 1) {
							if (editor instanceof ITextEditor) {
								try {
									ITextEditor textEditor = (ITextEditor) editor;
									IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
									IRegion lineRegion = document.getLineInformation(location.getLineNumber()-1);
									textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength()-1);
									decorate(textEditor,getBuildMetrics());
								} catch (BadLocationException e) {
									// ignore
								}
							}
						}
						return;
					} catch (PartInitException e) {
						AntUtilityUiPlugin.getDefault().getLog().log(e.getStatus());
					}
				}
			}
		}
		Display.getCurrent().beep(); // FIXME: this is anti-social
	}

	private void decorate(ITextEditor textEditor, BuildMetrics buildMetrics) {
		TextEditorBuildMetricsDecorator.install(textEditor,buildMetrics);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof BuildMetricsEditorInput)) {
			throw new PartInitException(new Status(IStatus.ERROR,AntUtilityUi.BUNDLE_ID,"invalid input"));
		}
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		setContentDescription(input.getToolTipText());
	}
	
	public BuildMetrics getBuildMetrics() {
		return (BuildMetrics) getEditorInput().getAdapter(BuildMetrics.class);
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
