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

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class AntUtilityPlugin extends Plugin {
	
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		configureMetricsListeners();
	}
	

	@Override
	public void stop(BundleContext context) throws Exception {
		MetricsHub.getInstance().removeAllListeners();
		super.stop(context);
	}
	

	private void configureMetricsListeners() {
		BuildMetricsExtensionPointReader.initialize(this);
	}
}
