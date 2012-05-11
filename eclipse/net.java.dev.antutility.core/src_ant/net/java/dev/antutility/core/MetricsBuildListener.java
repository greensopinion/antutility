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
package net.java.dev.antutility.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import net.java.dev.antutility.internal.core.BuildMetrics;
import net.java.dev.antutility.internal.core.Locator;
import net.java.dev.antutility.internal.core.Measurement;
import net.java.dev.antutility.internal.core.MeasurementAccumulator;
import net.java.dev.antutility.internal.core.MeasurementType;
import net.java.dev.antutility.internal.core.MetricsHub;
import net.java.dev.antutility.internal.core.Measurement.Key;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;


/**
 * An ant build listener that keeps timing metrics on tasks and target invocations and produces a report upon build completion.
 * Useful utility for optimizing the build.  Invoke as follows:
 * 
 * <code>
 * command prompt:> ant -listener net.java.antutility.BuildMetricsListener [target]
 * </code>
 * 
 * @author dgreen
 *
 */
public class MetricsBuildListener implements BuildListener {

	private List<Measurement> measurements = new ArrayList<Measurement>(1000);
	
	private ThreadLocal<Stack<Measurement>> oustandingByThread = new ThreadLocal<Stack<Measurement>>();
	
	public Stack<Measurement> getOutstanding() {
		Stack<Measurement> stack = oustandingByThread.get();
		if (stack == null) {
			stack = new Stack<Measurement>();
			oustandingByThread.set(stack);
		}
		return stack;
	}
	
	public void buildFinished(BuildEvent event) {
		finish(MeasurementType.BUILD);
		
		projectCompleted(event.getProject());
	}


	public void buildStarted(BuildEvent event) {
		// fix https://antutility.dev.java.net/issues/show_bug.cgi?id=3
		// we don't yet know the location, since the build file has not yet been fully processed.
		getOutstanding().push(new Measurement(MeasurementType.BUILD,"build",getLocation(event.getTarget())));
	}

	public void messageLogged(BuildEvent event) {
	}

	public void targetFinished(BuildEvent event) {
		finish(MeasurementType.TARGET);
	}


	public void targetStarted(BuildEvent event) {
		getOutstanding().push(new Measurement(MeasurementType.TARGET,event.getTarget().getName(),getLocation(event.getTarget())));
	}

	public void taskFinished(BuildEvent event) {
		finish(MeasurementType.TASK);
	}

	public void taskStarted(BuildEvent event) {
		getOutstanding().push(new Measurement(MeasurementType.TASK,event.getTask().getTaskName(),getLocation(event.getTask())));
	}

	private void finish(MeasurementType type) {
		Stack<Measurement> outstanding = getOutstanding();
		Measurement measurement = outstanding.pop();
		if (measurement.type != type) {
			throw new IllegalStateException();
		}
		measurement.end();
		if (!outstanding.isEmpty()) {
			outstanding.peek().childTime += measurement.getElapsedTime();
		}
		synchronized (measurements) {
			measurements.add(measurement);
		}
	}
	
	private Locator getLocation(Object o) {
		if (o instanceof Task) {
			return createLocator(((Task)o).getLocation());
		} else if (o instanceof Target) {
			return createLocator(((Target)o).getLocation());
		} else if (o instanceof Project) {
			Project project = (Project)o;
			return new Locator(project.getUserProperty("ant.file"),0,0);
		}
		if (!getOutstanding().isEmpty()) {
			return getOutstanding().peek().location;
		}
		return null;
	}
	
	private Locator createLocator(Location location) {
		return new Locator(location.getFileName(),location.getLineNumber(),location.getColumnNumber());
	}

	private void projectCompleted(Project project) {
		String buildFilePath = project.getUserProperty("ant.file");
		
		Map<Key,MeasurementAccumulator> measurementByKey = new HashMap<Key, MeasurementAccumulator>();
		for (Measurement measurement: measurements) {
			Key key = new Key(measurement);
			if (key.getLocation() == null) {
				key.setLocation(new Locator(buildFilePath,0,0));
			} else if (key.getLocation().getFileName() == null) {
				key.getLocation().setFileName(buildFilePath);
			}
			MeasurementAccumulator m = measurementByKey.get(key);
			if (m == null) {
				m = new MeasurementAccumulator(measurement);
				if (m.location == null) {
					m.location = key.getLocation();
				}
				measurementByKey.put(key,m);
			} else {
				m.add(measurement);
			}
		}
		SortedSet<MeasurementAccumulator> metrics = new TreeSet<MeasurementAccumulator>();
		metrics.addAll(measurementByKey.values());

		BuildMetrics buildMetrics = new BuildMetrics(buildFilePath==null?null:new File(buildFilePath),metrics);
		report(buildMetrics);
	}

	protected void report(BuildMetrics buildMetrics) {
		MetricsHub.getInstance().fireBuildMetricsMeasured(buildMetrics);
	}
}
