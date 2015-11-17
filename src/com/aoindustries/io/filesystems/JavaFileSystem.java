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

import com.aoindustries.lang.NullArgumentException;

/**
 * The file system implement by the Java runtime.
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
}
