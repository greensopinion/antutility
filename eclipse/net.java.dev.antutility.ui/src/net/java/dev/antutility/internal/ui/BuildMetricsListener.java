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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import net.java.dev.antutility.internal.core.BuildMetrics;
import net.java.dev.antutility.internal.core.MetricsListener;

public class BuildMetricsListener implements MetricsListener {

	public BuildMetricsListener() {
	}

	public void buildMetricsMeasured(final BuildMetrics buildMetrics) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new BuildMetricsEditorInput(buildMetrics), AntUtilityUi.BUILD_METRICS_EDITOR_ID);
				} catch (PartInitException e) {
					
				}
			}
		});
	}

}
