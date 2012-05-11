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

import java.io.File;
import java.util.Date;
import java.util.SortedSet;

public class BuildMetrics {
	private File buildFile;
	private Date timestamp;
	private SortedSet<MeasurementAccumulator> measurements;
	
	public BuildMetrics(File buildFile,
			SortedSet<MeasurementAccumulator> measurements) {
		this.buildFile = buildFile;
		this.measurements = measurements;
		this.timestamp = new Date();
	}
	public File getBuildFile() {
		return buildFile;
	}
	public void setBuildFile(File buildFile) {
		this.buildFile = buildFile;
	}
	public SortedSet<MeasurementAccumulator> getMeasurements() {
		return measurements;
	}
	public void setMeasurements(SortedSet<MeasurementAccumulator> measurements) {
		this.measurements = measurements;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	
}
