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
package net.java.dev.antutility.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class BuildMetricsExtensionPointReader {

	public static void initialize(AntUtilityPlugin plugin) {
		final String pluginId = plugin.getBundle().getSymbolicName();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(pluginId,"metricsListener");
		if (extensionPoint != null) {
			IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
			for (IConfigurationElement element : configurationElements) {
				Object extension;
				try {
					extension = element.createExecutableExtension("class");
				} catch (CoreException e) {
					plugin.getLog().log(e.getStatus());
					continue;
				}
				MetricsListener listener;
				try {
					listener = (MetricsListener) extension;
				} catch (ClassCastException e) {
					plugin.getLog().log(new Status(IStatus.ERROR,pluginId,e.getMessage(),e));
					continue;
				}
				MetricsHub.getInstance().addListener(listener);
			}
		}
	}

}
