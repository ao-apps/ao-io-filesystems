/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2017  AO Industries, Inc.
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

import java.io.Closeable;
import java.io.IOException;

/**
 * A lock object obtained when a file is successfully locked.
 *
 * @see  FileSystem#lock()
 *
 * @author  AO Industries, Inc.
 */
public interface FileLock extends Closeable {

	/**
	 * @see  java.nio.channels.FileLock#isValid()
	 */
	boolean isValid();

	/**
	 * Unlocks a file.  Will usually be called in a try/finally or try-with-resources block.
	 * 
	 * @see  #lock()
	 */
	@Override
	void close() throws IOException;
}
