/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-io-filesystems.
 *
 * ao-io-filesystems is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-io-filesystems is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-io-filesystems.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.io.filesystems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;

/**
 * The most basic layer of what all file systems have in common.
 * <p>
 * Every file system is forced to be case-sensitive, even if there is great
 * overhead in doing so.
 * </p>
 * <p>
 * We know this is in some ways redundant with the <code>java.nio.file</code>
 * package released in Java 1.7.  We are looking for something with a much
 * different (and narrower) focus, such as hiding differences between platforms
 * and trying to hide security gotchas.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public interface FileSystem {

	/**
	 * Checks that a given path name is acceptable to this file system.
	 * Regular path rules are already checked, this is for additional
	 * file system specific constraints.
	 * The root path is never passed here.
	 *
	 * @param path The path to check, must be from this file system.
	 * @return     The path, if it is acceptable
	 * @throws InvalidPathException If the path is not acceptable
	 */
	void checkSubPath(Path parent, String name) throws InvalidPathException;

	/**
	 * Joins the array of names to a path object.
	 * Stops at the end of the array or the first <code>null</code> element.
	 *
	 * @see Path#explode() for the inverse operation
	 * @see Path#explode(java.lang.String[]) for the inverse operation
	 */
	default Path join(String[] names) throws InvalidPathException {
		Path p = new Path(this);
		for(String name : names) {
			if(name == null) break;
			p = new Path(p, name);
		}
		return p;
	}

	/**
	 * Parses a string representation of a path.
	 *
	 * @see Path#toString() for the inverse operation
	 * @see Path#toString(java.lang.Appendable) for the inverse operation
	 */
	default Path parsePath(String value) throws InvalidPathException {
		Path p = null;
		int lastSepPos = -1;
		int len = value.length();
		do {
			int sepPos = value.indexOf(Path.SEPARATOR, lastSepPos + 1);
			if(sepPos == -1) sepPos = len;
			String name = value.substring(lastSepPos + 1, sepPos);
			if(p == null) {
				// root must have empty name
				if(!name.isEmpty()) throw new InvalidPathException("Non-empty root name: " + name);
				p = new Path(this);
			} else {
				p = new Path(p, name);
			}
			lastSepPos = sepPos;
		} while(lastSepPos < len);
		return p;
	}

	/**
	 * Lists the children of the given path in no specific order.
	 * It is possible that paths may be returned that no longer exist.
	 * It is also possible that new file system objects created after the beginning of iteration are not returned.
	 *
	 * @path  Must be from this file system.
	 *
	 * @return a read-only iterator of children
	 * 
	 * @throws FileNotFoundException if the path does not exist
	 * @throws NotDirectoryException if the path is not a directory
	 * @throws IOException if an underlying I/O error occurs.
	 */
	PathIterator list(Path path) throws FileNotFoundException, NotDirectoryException, IOException;
}
