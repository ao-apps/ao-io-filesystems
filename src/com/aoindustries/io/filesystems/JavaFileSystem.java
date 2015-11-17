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

import com.aoindustries.lang.NotImplementedException;
import com.aoindustries.lang.NullArgumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.util.Iterator;

/**
 * The file system implement by the Java runtime.
 * <p>
 * The system is treated as a single-root file system.  For Windows, this means
 * that <code>C:\</code> will become <code>/C:/</code>.
 * </p>
 * <p>
 * Note: To work with any possible filename correctly in Linux, one must use a
 * single-byte locale, such as "C", "POSIX", or "en_US".  Java has issues when
 * using UTF-8 encoding and filenames do not contain valid UTF-8.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class JavaFileSystem implements FileSystem {

	/**
	 * The maximum name length.  Until Java exposes a reasonable way to detect
	 * this, we'll just leave it hard-coded.
	 */
	public static final int MAX_PATH_NAME_LENGTH = 255;

	/**
	 * General filename restrictions are:
	 * <ol>
	 * <li>Must not be longer than <code>MAX_PATH_NAME_LENGTH</code> characters</li>
	 * <li>Must not contain the NULL character</li>
	 * <li>Must not contain the current platform separator character</li>
	 * <li>Must not be any length sequence of only "." characters (this protects Windows multi-dot, too)</li>
	 * </ol>
	 */
	@Override
	public Path checkPath(Path path) throws InvalidPathException {
		NullArgumentException.checkNotNull(path, "path");
		Path checking = path;
		do {
			String name = checking.getName();
			int nameLen = name.length();
			// Must not be longer than <code>MAX_PATH_NAME_LENGTH</code> characters
			if(nameLen > MAX_PATH_NAME_LENGTH) {
				throw new InvalidPathException("Path name must not be longer than " + MAX_PATH_NAME_LENGTH + " characters: " + name);
			}
			// Must not contain the NULL character
			if(name.indexOf(0) != -1) {
				throw new InvalidPathException("Path name must not contain the NULL character: " + name);
			}
			// Path.SEPARATOR already checked in the Path constructor
			if(File.separatorChar != Path.SEPARATOR) {
				// Must not contain the current platform separator character
				if(name.indexOf(File.separatorChar) != -1) {
					throw new InvalidPathException("Path name must not contain the '" + File.separatorChar + "' character: " + name);
				}
			}
			// Must not be any length sequence of only "." characters
			boolean hasNonDot = false;
			for(int i = 0; i < nameLen; i++) {
				if(name.charAt(i) != '.') {
					hasNonDot = true;
					break;
				}
			}
			if(!hasNonDot) {
				throw new InvalidPathException("Path name must not be any length sequence of only \".\" characters: " + name);
			}
			checking = checking.getParent();
		} while(checking != null);
		return path;
	}

	/**
	 * Gets a Java File for the given path.
	 *
	 * @throws InvalidPathException If the path is not acceptable
	 * 
	 * @throws FileNotFoundException if the path does not exist
	 */
	protected File getFile(Path path) throws InvalidPathException, FileNotFoundException {
		checkPath(path);
		File[] roots = File.listRoots();
		if(roots == null) throw new FileNotFoundException("Unable to list roots");
		throw new NotImplementedException("TODO");
	}

	@Override
	public PathIterator list(Path path) throws InvalidPathException, FileNotFoundException, IOException {
		File file = getFile(path);
		DirectoryStream<java.nio.file.Path> stream;
		try {
			stream = Files.newDirectoryStream(file.toPath());
		} catch(NotDirectoryException e) {
			return null;
		}
		Iterator<java.nio.file.Path> iter = stream.iterator();
		return new PathIterator() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			@Override
			public Path next() {
				return new Path(path, iter.next().getFileName().toString());
			}
			@Override
			public void close() throws IOException {
				stream.close();
			}
		};
	}
}
