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

import com.aoindustries.util.AoCollections;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ReadOnlyFileSystemException;
import java.util.Iterator;

/**
 * Wraps a file system to make it read-only.
 *
 * @author  AO Industries, Inc.
 */
public class ReadOnlyFileSystem implements FileSystem {

	private final FileSystem wrapped;

	public ReadOnlyFileSystem(FileSystem wrapped) {
		this.wrapped = wrapped;
	}

	/**
	 * Defers to the wrapped file system.
	 */
	@Override
	public Path checkPath(Path path) throws InvalidPathException {
		return wrapped.checkPath(path);
	}

	/**
	 * Defers to the wrapped file system.
	 */
	@Override
	public PathIterator list(Path path) throws InvalidPathException, FileNotFoundException, IOException {
		PathIterator iter = wrapped.list(path);
		return new PathIterator() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Path next() {
				return iter.next();
			}

			/**
			 * The iterators are already supposed to be read-only, but this is here
			 * for added assurance.
			 */
			@Override
			public void remove() throws ReadOnlyFileSystemException {
				throw new ReadOnlyFileSystemException();
			}

			@Override
			public void close() throws IOException {
				iter.close();
			}
		};
	}
}
