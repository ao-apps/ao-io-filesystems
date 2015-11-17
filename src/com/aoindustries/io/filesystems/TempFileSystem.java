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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A temporary file system stored in the Java heap.
 *
 * @author  AO Industries, Inc.
 */
public class TempFileSystem implements FileSystem {

	private abstract static class FileSystemObject {
	}

	private static class Directory extends FileSystemObject {
		private final LinkedList<String> files = new LinkedList<>();
		private String[] list() {
			return files.toArray(new String[files.size()]);
		}
	}

	private static class RegularFile extends FileSystemObject {
	}

	private final Map<Path,FileSystemObject> files = new HashMap<>();

	public TempFileSystem() {
		synchronized(files) {
			files.put(Path.ROOT, new Directory());
		}
	}

	/**
	 * Temporary file systems support all possible paths.
	 */
	@Override
	public Path checkPath(Path path) {
		return path;
	}

	@Override
	public String[] list(Path path) throws FileNotFoundException {
		// All paths supported: checkPath(path);
		FileSystemObject file;
		synchronized(files) {
			file = files.get(path);
			if(file == null) throw new FileNotFoundException(path.toString());
			if(!(file instanceof Directory)) return null;
			return ((Directory)file).list();
		}
	}
}
