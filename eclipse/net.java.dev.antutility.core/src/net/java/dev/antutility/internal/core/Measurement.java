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

import org.apache.tools.ant.Location;

public class Measurement {

	public static class Key {
		private String name;
		private Locator location;
		private MeasurementType type;

		public Key(Measurement measurement) {
			this.name = measurement.name;
			this.type = measurement.type;
			this.location = measurement.location;
			if (name == null) {
				name = "";
			}
		}

		public String getName() {
			return name;
		}

		public Locator getLocation() {
			return location;
		}
		public void setLocation(Locator location) {
			this.location = location;
		}
		public MeasurementType getType() {
			return type;
		}

		public int hashCode() {
			return name.hashCode()
					+ ((location == null) ? 0 : location.getLineNumber());
		}

		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other != null && other.getClass() == Key.class) {
				Key key = (Key) other;
				return type == key.type
						&& (name == key.name || (name != null && name
								.equals(key.name)))
						&& (location == key.location || (location != null && location
								.equals(key.location)));
			}
			return false;
		}

	}

	public MeasurementType type;
	public String name;
	public Locator location;
	public long startTime;
	public long endTime;
	public long childTime;

	public Measurement(MeasurementType type, String name, Locator location) {
		super();
		this.type = type;
		this.name = name;
		this.location = location;
		this.startTime = System.currentTimeMillis();
	}

	public void end() {
		this.endTime = System.currentTimeMillis();
	}

	public long getElapsedTime() {
		return endTime - startTime;
	}

	public long getLocalElapsedTime() {
		return getElapsedTime() - childTime;
	}
}