/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2021  AO Industries, Inc.
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
 * along with ao-io-filesystems.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aoapps.io.filesystems;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wraps any standard FileSystem implementation.
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

	private static final JavaFileSystem defaultInstance = new JavaFileSystem(FileSystems.getDefault());

	/**
	 * Gets the wrapper for the default file system implement by the Java runtime,
	 * only one instance is created.
	 */
	public static JavaFileSystem getDefault() {
		return defaultInstance;
	}

	protected final java.nio.file.FileSystem javaFS;
	protected final boolean isSingleRoot;

	public JavaFileSystem(java.nio.file.FileSystem javaFS) {
		this.javaFS = javaFS;
		Iterator<java.nio.file.Path> roots = javaFS.getRootDirectories().iterator();
		if(!roots.hasNext()) throw new AssertionError("No root");
		java.nio.file.Path root = roots.next();
		if(roots.hasNext()) {
			// Has more than one root
			isSingleRoot = false;
		} else {
			// Root must simply be the separator (Not something like C:\)
			isSingleRoot = javaFS.getSeparator().equals(root.toString());
		}
	}

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
	public void checkSubPath(Path parent, String name) throws InvalidPathException {
		if(parent.getFileSystem() != this) throw new IllegalArgumentException();
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
		String javaSep = javaFS.getSeparator();
		if(javaSep.length() != 1 && javaSep.charAt(0) != Path.SEPARATOR) {
			// Must not contain the current platform separator character
			if(name.contains(javaSep)) {
				throw new InvalidPathException("Path name must not contain the '" + javaSep + "' separator: " + name);
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
	}

	/**
	 * Gets a Java File for the given path.
	 *
	 * @throws InvalidPathException If the path is not acceptable
	 */
	protected java.nio.file.Path getJavaPath(Path path) throws IOException {
		assert path.getFileSystem() == this;
		if(isSingleRoot) {
			return javaFS.getPath(javaFS.getSeparator(), path.explode());
		} else {
			String[] exploded = path.explode();
			if(exploded.length == 0) throw new IOException("Cannot map fake root into non-Unix environment");
			String expectedRoot = exploded[0] + javaFS.getSeparator();
			for(java.nio.file.Path root : javaFS.getRootDirectories()) {
				String rootStr = root.toString();
				if(rootStr.equals(exploded[0])) {
					return javaFS.getPath(
						rootStr,
						Arrays.copyOfRange(exploded, 1, exploded.length)
					);
				}
			}
			throw new NoSuchFileException(expectedRoot);
		}
	}

	@Override
	public PathIterator list(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		if(isSingleRoot || path.getParent() != null) {
			DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(getJavaPath(path));
			Iterator<java.nio.file.Path> iter = stream.iterator();
			return new PathIterator() {
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				@Override
				public Path next() throws NoSuchElementException {
					return new Path(path, iter.next().getFileName().toString());
				}
				@Override
				public void close() throws IOException {
					stream.close();
				}
			};
		} else {
			// List roots and strip their trailing separator
			String javaSeparator = javaFS.getSeparator();
			Iterator<java.nio.file.Path> rootIter = javaFS.getRootDirectories().iterator();
			return new PathIterator() {
				@Override
				public boolean hasNext() {
					return rootIter.hasNext();
				}
				@Override
				public Path next() throws NoSuchElementException {
					String rootStr = rootIter.next().toString();
					if(!rootStr.endsWith(javaSeparator)) throw new AssertionError("Root does not end with separator: " + rootStr);
					return new Path(path, rootStr.substring(0, rootStr.length() - javaSeparator.length()));
				}
				@Override
				public void close() {
					// Nothing to do
				}
			};
		}
	}

	@Override
	public void delete(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		Files.delete(getJavaPath(path));
	}

	@Override
	public long size(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		return Files.size(getJavaPath(path));
	}

	@Override
	public Path createFile(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		Files.createFile(getJavaPath(path));
		return path;
	}

	@Override
	public Path createDirectory(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		Files.createDirectory(getJavaPath(path));
		return path;
	}

	@Override
	public FileLock lock(Path path) throws IOException {
		// Obtain lock
		FileChannel channel = FileChannel.open(getJavaPath(path), StandardOpenOption.READ);
		java.nio.channels.FileLock lock = channel.lock();
		return new FileLock() {
			@Override
			public boolean isValid() {
				return lock.isValid();
			}
			@Override
			public void close() throws IOException {
				channel.close();
			}
		};
	}
}
