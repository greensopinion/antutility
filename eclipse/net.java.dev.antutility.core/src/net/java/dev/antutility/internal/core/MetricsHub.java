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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MetricsHub {
	private static MetricsHub instance = new MetricsHub();
	
	private List<MetricsListener> listeners = new CopyOnWriteArrayList<MetricsListener>();
	
	public static MetricsHub getInstance() {
		return instance;
	}
	
	public void addListener(MetricsListener listener) {
		listeners.add(listener);
	}
	public void removeListener(MetricsListener listener) {
		listeners.remove(listener);
	}
	
	public void fireBuildMetricsMeasured(BuildMetrics buildMetrics) {
		for (MetricsListener listener: listeners) {
			listener.buildMetricsMeasured(buildMetrics);
		}
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
}
