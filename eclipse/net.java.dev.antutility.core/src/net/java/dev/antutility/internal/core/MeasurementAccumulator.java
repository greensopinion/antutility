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


public class MeasurementAccumulator implements Comparable<MeasurementAccumulator> {

	
	public MeasurementType type;
	public String name;
	public Locator location;
	public long childTime;
	public long localTime;
	public int invocationCount;
	
	public MeasurementAccumulator(Measurement measurement) {
		type = measurement.type;
		name = measurement.name;
		location = measurement.location;
		childTime = measurement.childTime;
		localTime = measurement.getLocalElapsedTime();
		invocationCount = 1;
	}
	
	public void add(Measurement measurement) {
		childTime += measurement.childTime;
		localTime += measurement.getLocalElapsedTime();
		++invocationCount;
	}

	public int compareTo(MeasurementAccumulator o) {
		if (o == this) {
			return 0;
		}
		int i = new Long(localTime).compareTo(o.localTime);
		if (i == 0) {
			i = new Long(childTime).compareTo(o.childTime);
			if (i == 0) {
				i = new Integer(invocationCount).compareTo(o.invocationCount);
				if (i == 0) {
					i = name == o.name?0:name == null?-1:o.name == null?1:name.compareTo(o.name);
					if (i == 0) {
						i = type.compareTo(o.type);
						if (i == 0) {
							i = (location == null)?o.location==null?0:1:o.location==null?-1:new Integer(location.getLineNumber()).compareTo(o.location.getLineNumber());
						}
					}
				} else {
					i = -i;
				}
			} else {
				i = -i;
			}
		} else {
			i = -i;
		}
		return i;
	}
}