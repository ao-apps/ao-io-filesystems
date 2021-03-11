/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2019, 2020, 2021  AO Industries, Inc.
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

import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.NotImplementedException;

/**
 * A temporary file system stored in the Java heap.
 *
 * @author  AO Industries, Inc.
 */
public class TempFileSystem implements FileSystem {

	protected abstract static class FileSystemObject {
	}

	private static class Directory extends FileSystemObject {
		private final LinkedList<String> files = new LinkedList<>();
		private String[] list() {
			return files.toArray(new String[files.size()]);
		}
	}

	private static class RegularFile extends FileSystemObject {
	}

	protected final Map<Path, FileSystemObject> files = new HashMap<>();

	public TempFileSystem() {
		synchronized(files) {
			files.put(new Path(this), new Directory());
		}
	}

	/**
	 * Temporary file systems support all possible paths.
	 */
	@Override
	public void checkSubPath(Path parent, String name) {
		if(parent.getFileSystem() != this) throw new IllegalArgumentException();
		// All allowed
	}

	@Override
	public PathIterator list(Path path) throws NoSuchFileException, NotDirectoryException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		String[] list;
		synchronized(files) {
			FileSystemObject file = files.get(path);
			if(file == null) throw new NoSuchFileException(path.toString());
			if(!(file instanceof Directory)) throw new NotDirectoryException(path.toString());
			list = ((Directory)file).list();
		}
		return new PathIterator() {
			private int next = 0;
			@Override
			public boolean hasNext() {
				return next < list.length;
			}
			@Override
			public Path next() {
				if(next >= list.length) throw new NoSuchElementException();
				return new Path(path, list[next++]);
			}
			@Override
			public void close() {
				// Nothing to do
			}
		};
	}

	@Override
	public void delete(Path path) {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		throw new NotImplementedException("TODO");
	}

	@Override
	public long size(Path path) {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		throw new NotImplementedException("TODO");
	}

	@Override
	public Path createFile(Path path) {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		throw new NotImplementedException("TODO");
	}

	@Override
	public Path createDirectory(Path path) {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		throw new NotImplementedException("TODO");
	}

	@Override
	public FileLock lock(Path path) {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		throw new NotImplementedException("TODO");
	}
}
