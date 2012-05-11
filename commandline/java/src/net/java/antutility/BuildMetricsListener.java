package net.java.antutility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import net.java.antutility.Measurement.Key;

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
public class BuildMetricsListener implements BuildListener {

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
		
		report();
	}


	public void buildStarted(BuildEvent event) {
		// fix https://antutility.dev.java.net/issues/show_bug.cgi?id=3
		// we don't yet know the location, since the build file has not yet been fully processed.
		getOutstanding().push(new Measurement(MeasurementType.BUILD,"build",new Location("n/a",0,0)));
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
	
	private Location getLocation(Object o) {
		if (o instanceof Task) {
			return ((Task)o).getLocation();
		} else if (o instanceof Target) {
			return ((Target)o).getLocation();
		} else if (o instanceof Project) {
			Project project = (Project)o;
			return new Location(project.getName()+project.getBaseDir());
		}
		if (!getOutstanding().isEmpty()) {
			return getOutstanding().peek().location;
		}
		return null;
	}
	
	private void report() {
		Map<Key,MeasurementAccumulator> measurementByKey = new HashMap<Key, MeasurementAccumulator>();
		for (Measurement measurement: measurements) {
			Key key = new Key(measurement);
			MeasurementAccumulator m = measurementByKey.get(key);
			if (m == null) {
				m = new MeasurementAccumulator(measurement);
				measurementByKey.put(key,m);
			} else {
				m.add(measurement);
			}
		}
		System.out.println("BUILD METRICS:");
		SortedSet<MeasurementAccumulator> metrics = new TreeSet<MeasurementAccumulator>();
		metrics.addAll(measurementByKey.values());
		System.out.println("Local Time, Child Time, Invocation Count, Type, Name, Location");
		for (MeasurementAccumulator ma: metrics) {
			System.out.println(String.format("%s, %s, %s, %s, %s, %s",ma.localTime,ma.childTime,ma.invocationCount,ma.type,ma.name,ma.location));
		}
	}
}
