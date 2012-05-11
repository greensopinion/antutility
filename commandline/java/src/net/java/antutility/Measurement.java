/**
 * 
 */
package net.java.antutility;

import org.apache.tools.ant.Location;

class Measurement {

	public static class Key {
		private String name;
		private Location location;
		private MeasurementType type;
		
		Key(Measurement measurement) {
			this.name = measurement.name;
			this.type = measurement.type;
			this.location = measurement.location;
			if (name == null) {
				name = "";
			}
		}

		public int hashCode() {
			return name.hashCode() + ((location == null)?0:location.getLineNumber());
		}
		
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other != null && other.getClass() == Key.class) {
				Key key = (Key) other;
				return type == key.type && (name == key.name || (name != null && name.equals(key.name))) && (location == key.location || (location != null && location.equals(key.location)));
			}
			return false;
		}
	}
	
	MeasurementType type;
	String name;
	Location location;
	long startTime;
	long endTime;
	long childTime;
	
	Measurement(MeasurementType type, String name, Location location) {
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