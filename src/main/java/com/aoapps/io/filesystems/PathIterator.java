/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2021, 2022  AO Industries, Inc.
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

import java.io.Closeable;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.util.Iterator;

/**
 * Iterates over paths, must be closed when done.
 * TODO: Support ListIterator when underlying iterator is a list iterator, will require PathListIterator implementation.
 *
 * @see DirectoryStream for iteration details
 *
 * @author  AO Industries, Inc.
 */
public abstract class PathIterator implements Iterator<Path>, Closeable {

  /**
   * {@inheritDoc}
   *
   * @throws DirectoryIteratorException when an underlying IOException has occurred.
   */
  @Override
  public abstract boolean hasNext() throws DirectoryIteratorException;

  /**
   * The path iterators must be read-only.
   */
  @Override
  public final void remove() {
    Iterator.super.remove();
  }
}
