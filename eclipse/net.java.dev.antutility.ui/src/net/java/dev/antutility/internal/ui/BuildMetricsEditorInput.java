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
package net.java.dev.antutility.internal.ui;

import java.io.File;

import net.java.dev.antutility.internal.core.BuildMetrics;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class BuildMetricsEditorInput extends PlatformObject implements IEditorInput {

	private BuildMetrics buildMetrics;
	
	public BuildMetricsEditorInput(BuildMetrics buildMetrics) {
		this.buildMetrics = buildMetrics;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		File buildFile = buildMetrics.getBuildFile();
		if (buildFile != null) {
			return String.format("%s Build Metrics",buildFile.getName());
		}
		return "Build Metrics";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		File buildFile = buildMetrics.getBuildFile();
		if (buildFile != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(buildFile.toString()));
			if (file != null) {
				return String.format("%s %s",file.getFullPath().toString(),buildMetrics.getTimestamp());
			}
		}
		return buildMetrics.getTimestamp().toString();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(buildMetrics.getClass())) { 
			return buildMetrics;
		}
		if (adapter.isAssignableFrom(IFile.class)) {
			File buildFile = buildMetrics.getBuildFile();
			if (buildFile != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(buildFile.toString()));
				return file;
			}
		}
		return super.getAdapter(adapter);
	}

}
