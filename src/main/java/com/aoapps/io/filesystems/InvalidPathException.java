/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.Throwables;

/**
 * Thrown when an invalid path is created, or when a path is invalid for
 * a given file system.
 *
 * @author  AO Industries, Inc.
 */
public class InvalidPathException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public InvalidPathException(String message) {
    super(message);
  }

  public InvalidPathException(String message, Throwable cause) {
    super(message, cause);
  }

  static {
    Throwables.registerSurrogateFactory(InvalidPathException.class, (template, cause) ->
      new InvalidPathException(template.getMessage(), cause)
    );
  }
}
