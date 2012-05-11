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

public class Locator {
	private String fileName;
	private int lineNumber;
	private int columnNumber;
	
	public Locator(String fileName, int lineNumber, int columnNumber) {
		super();
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}
	

	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public int getLineNumber() {
		return lineNumber;
	}


	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}


	public int getColumnNumber() {
		return columnNumber;
	}


	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columnNumber;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + lineNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Locator other = (Locator) obj;
		if (columnNumber != other.columnNumber)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}
	
	
}
