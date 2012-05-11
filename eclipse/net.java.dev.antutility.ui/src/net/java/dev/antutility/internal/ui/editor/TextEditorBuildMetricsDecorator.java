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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.java.dev.antutility.internal.core.BuildMetrics;
import net.java.dev.antutility.internal.core.Locator;
import net.java.dev.antutility.internal.core.MeasurementAccumulator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class TextEditorBuildMetricsDecorator implements LineBackgroundListener {

	private static final String KEY = TextEditorBuildMetricsDecorator.class.getName();
	
	private final TextViewer textViewer;
	private Map<Integer,MeasurementAccumulator> accumulatorByLine = new HashMap<Integer, MeasurementAccumulator>();
	private Map<Long,Color> colorByPercentage = new HashMap<Long, Color>();
	
	private BuildMetrics buildMetrics;

	private final ITextEditor textEditor;

	private long maxLocalTime;

	public TextEditorBuildMetricsDecorator(ITextEditor textEditor, TextViewer textViewer) {
		this.textEditor = textEditor;
		this.textViewer = textViewer;
		textViewer.getTextWidget().setData(KEY, this);
		textViewer.getTextWidget().addLineBackgroundListener(this);
		textViewer.getTextWidget().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeColors();
			}
		});
	}

	public static void install(ITextEditor textEditor, BuildMetrics buildMetrics) {
		Object textOperationTarget = textEditor.getAdapter(ITextOperationTarget.class);
		if (textOperationTarget instanceof TextViewer) {
			TextViewer textViewer = (TextViewer) textOperationTarget;
			TextEditorBuildMetricsDecorator decorator = (TextEditorBuildMetricsDecorator) textViewer.getTextWidget().getData(KEY);
			if (decorator == null) {
				decorator = new TextEditorBuildMetricsDecorator(textEditor,textViewer);
			}
			decorator.update(buildMetrics);
		}
	}

	private void update(BuildMetrics buildMetrics) {
		if (buildMetrics == this.buildMetrics) {
			return;
		}
		this.buildMetrics = buildMetrics;
		accumulatorByLine.clear();
		disposeColors();
		
		IFile file = null;
		IPath path = null;
		IEditorInput editorInput = textEditor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			file = ((IFileEditorInput)editorInput).getFile();
		} else if (editorInput instanceof IPathEditorInput) {
			path = ((IPathEditorInput)editorInput).getPath();
		}
		maxLocalTime = 0L;
		for (MeasurementAccumulator accumulator: buildMetrics.getMeasurements()) {
			Locator location = accumulator.location;
			if (match(location,file,path)) {
				maxLocalTime = Math.max(accumulator.localTime, maxLocalTime);
				// widgets have 0-based line numbers
				int widgetLineNumber = accumulator.location.getLineNumber()-1;
				accumulatorByLine.put(widgetLineNumber, accumulator);
			}
		}
		textViewer.invalidateTextPresentation();
		textViewer.getTextWidget().redraw();
	}

	private boolean match(Locator location, IFile file, IPath path) {
		if (location.getFileName() != null) {
			IPath locationPath = new Path(location.getFileName());
			if (path != null && locationPath.equals(path)) {
				return true;
			}
			if (file != null) {
				IPath filePath = file.getLocation();
				if (filePath != null && filePath.equals(locationPath)) {
					return true;
				}
			}
		}
		return false;
	}

	public void lineGetBackground(LineBackgroundEvent event) {
		if (maxLocalTime <= 0) {
			return;
		}
		int lineOffset = widgetOffsetToModelOffset(event.lineOffset);
		if (lineOffset == -1) {
			return;
		}
		IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
		int lineOfOffset;
		try {
			lineOfOffset = document.getLineOfOffset(lineOffset);
		} catch (BadLocationException e) {
			return;
		}
		MeasurementAccumulator accumulator = accumulatorByLine.get(lineOfOffset);
		if (accumulator != null) {
			long percent = (accumulator.localTime*100L)/maxLocalTime;
			Color color = colorByPercentage.get(percent);
			if (color == null) {
				int red = 255;
				int other = 255 - ((int)(255*percent/100));
				color = new Color(textViewer.getTextWidget().getDisplay(), red,other,other);
				colorByPercentage.put(percent, color);
			}
			if (color != null) {
				event.lineBackground = color;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	int widgetOffsetToModelOffset(int offset) {
		int lineOffset = -1;
		if (textViewer != null) {
			if (textViewer instanceof ITextViewerExtension5) {
				lineOffset = ((ITextViewerExtension5) textViewer).widgetOffset2ModelOffset(offset);
			} else if (textViewer instanceof org.eclipse.jface.text.ITextViewerExtension3) {
				lineOffset = ((org.eclipse.jface.text.ITextViewerExtension3) textViewer).widgetOffset2ModelOffset(offset);
			} else {
				try {
					Method m = textViewer.getClass().getMethod("widgetOffset2ModelOffset", int.class);
					lineOffset = ((Integer) m.invoke(textViewer, new Integer(offset))).intValue();
				} catch (Exception e) {
					// ignore
					lineOffset = offset;
				}
			}
		}
		return lineOffset;
	}
	

	protected void disposeColors() {
		for (Color color: colorByPercentage.values()) {
			color.dispose();
		}
		colorByPercentage.clear();
	}

}
