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
import java.util.Iterator;

/**
 * The most basic layer of what all file systems have in common.
 * <p>
 * Every file system is forced to be case-sensitive, even if there is great
 * overhead in doing so.
 * </p>
 * <p>
 * We know this is in some ways redundant with the <code>java.nio.file</code>
 * package released in Java 1.7.  We are looking for something with a much
 * different focus, such as hiding differences between platforms and trying
 * to hide security gotchas.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public interface FileSystem {

	/**
	 * Checks that a path is acceptable to this file system.
	 *
	 * @param path The path to check
	 * @return     The path, if it is acceptable
	 * @throws InvalidPathException If the path is not acceptable
	 */
	Path checkPath(Path path) throws InvalidPathException;

	/**
	 * Lists the children of the given path in no specific order.
	 *
	 * @return <code>null</code> if the path is not a directory, or an iterator of children.
	 *                           The iterator remove() method may be called to delete the current child.
	 *
	 * @throws InvalidPathException If the path is not acceptable
	 * 
	 * @throws FileNotFoundException if the path does not exist
	 * @throws IOException if an underlying I/O error occurs.
	 */
	PathIterator list(Path path) throws InvalidPathException, FileNotFoundException, IOException;
}
