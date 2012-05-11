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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BuildMetricsTreeContentProvider implements ITreeContentProvider {

	private static final Object[] NO_ELEMENTS = new Object[0];
	
	private BuildMetrics buildMetrics;
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement == buildMetrics) {
			return buildMetrics.getMeasurements().toArray();
		}
		return NO_ELEMENTS;
	}

	public Object getParent(Object element) {
		return buildMetrics;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		buildMetrics = (BuildMetrics) newInput;
	}

}
