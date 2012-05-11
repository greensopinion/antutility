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

import net.java.dev.antutility.internal.core.Locator;
import net.java.dev.antutility.internal.core.MeasurementAccumulator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class LocationColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof MeasurementAccumulator) {
			MeasurementAccumulator accumulator = (MeasurementAccumulator) element;
			
			Locator location = accumulator.location;
			if (location != null) {
				String fileName = location.getFileName();
				if (fileName != null) {
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
					if (file != null) {
						fileName = file.getFullPath().toString();
					}
				}
				return String.format("%s:%s:%s",fileName,location.getLineNumber(),location.getColumnNumber());
			}
		}
		return "???";
	}
}
