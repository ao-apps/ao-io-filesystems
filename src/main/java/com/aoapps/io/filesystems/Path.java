/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2021, 2022, 2024  AO Industries, Inc.
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

/**
 * The object representing the path to a file.  This API is for programmer tools,
 * not for user interfaces.  We try to hide the platform differences.
 *
 * <p>As there is no concept of "working directory" in this API, the path is
 * always absolute.  However, the path may be interpreted from a different
 * effective root by a file system.</p>
 *
 * <p>Each file system may limit the length of path names or total paths, but
 * there is no limit imposed within <code>Path</code> itself.</p>
 *
 * <p>The path separator is always "/", even on platforms that do otherwise.</p>
 *
 * <p>The root path is an empty string (and the only path name that may be an empty
 * string).  In string form, the root path by itself is represented as "/".</p>
 *
 * <p>Paths are case-sensitive, even on platforms that do otherwise.</p>
 *
 * @author  AO Industries, Inc.
 */
public class Path implements Comparable<Path> {

  public static final char SEPARATOR = '/';
  public static final String SEPARATOR_STRING = String.valueOf(SEPARATOR);

  private final FileSystem fileSystem;
  private final Path parent;
  private final String name;
  private final int depth;

  /**
   * Constructs the root path for the given file system.
   * This will always be the empty path ("").
   */
  public Path(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.parent = null;
    this.name = "";
    this.depth = 0;
  }

  /**
   * Constructs a child path of the given parent.
   *
   * @param  parent      Must not be <code>null</code>.
   * @param  name        Must not be <code>""</code>.
   *                     Must not contain the <code>SEPARATOR</code> character.
   *
   * @see FileSystem#checkSubPath(com.aoapps.io.filesystems.Path, java.lang.String)
   *                     Each file system may impose additional path restrictions.
   */
  public Path(Path parent, String name) throws InvalidPathException {
    // Must not be null
    if (parent == null) {
      throw new InvalidPathException("Parent required for non-root path");
    }
    if (name == null) {
      throw new InvalidPathException("Path name may not be null");
    }
    // Must not be ""
    if (name.isEmpty()) {
      throw new InvalidPathException("Only the root may have an empty name");
    }
    // Must not contain the SEPARATOR character
    if (name.indexOf(SEPARATOR) != -1) {
      throw new InvalidPathException("Path name must not contain the '" + SEPARATOR + "' character: " + name);
    }
    parent.fileSystem.checkSubPath(parent, name);
    this.fileSystem = parent.fileSystem;
    this.parent = parent;
    this.name = name;
    this.depth = parent.depth + 1;
  }

  /**
   * {@inheritDoc}
   *
   * @see #equals(com.aoapps.io.filesystems.Path)
   */
  @Override
  public boolean equals(Object o) {
    return
        (o instanceof Path)
            && equals((Path) o);
  }

  /**
   * Two paths are equal if they are of the same depth and have equal names.
   * Two paths on different file systems can be equal.
   */
  public boolean equals(Path other) {
    // Identity shortcut
    if (this == other) {
      return true;
    }
    // Must have matching depths
    if (this.depth != other.depth) {
      return false;
    }
    // Iterative implementation
    Path me = this;
    Path o = other;
    do {
      if (!me.name.equals(o.name)) {
        // Name does not match
        return false;
      }
      me = me.parent;
      o = o.parent;
    } while (me != null);
    assert o == null : "This is root, the other must also be root";
    return true;
  }

  /** Cache the hash. */
  private int hash;

  @Override
  public int hashCode() {
    if (parent == null) {
      return 0;
    } else {
      int h = hash;
      if (h == 0) {
        h = parent.hashCode() * 31 + name.hashCode();
        hash = h;
      }
      return h;
    }
  }

  /**
   * Compares two paths in lexical order.
   */
  @Override
  public int compareTo(final Path other) {
    if (this == other) {
      return 0;
    }

    Path me = this;
    Path o = other;
    int meDepth = me.depth;
    int odepth = o.depth;
    int tailDiff;
    if (meDepth < odepth) {
      tailDiff = -1;
      while (meDepth < odepth) {
        o = o.parent;
        odepth--;
      }
    } else if (meDepth > odepth) {
      tailDiff = 1;
      while (meDepth > odepth) {
        me = me.parent;
        meDepth--;
      }
    } else {
      assert meDepth == odepth;
      tailDiff = 0;
    }
    assert me.depth == meDepth;
    assert o.depth == odepth;
    assert meDepth == odepth;
    // Roots are always equal
    if (meDepth != 0) {
      // Compare parents first
      int parentDiff = me.parent.compareTo(o.parent);
      if (parentDiff != 0) {
        return parentDiff;
      }
      int nameDiff = me.name.compareTo(o.name);
      if (nameDiff != 0) {
        return nameDiff;
      }
    }
    return tailDiff;
  }

  /**
   * Gets a string representation of the path.
   * The root is the separator by itself ("/").
   *
   * @see #toString(java.lang.Appendable) for a possibly faster implementation
   * @see FileSystem#parsePath(java.lang.String) for the inverse operation
   */
  @Override
  public String toString() {
    if (parent == null) {
      return SEPARATOR_STRING;
    } else {
      int totalLen = 0;
      Path path = this;
      do {
        totalLen += path.name.length();
        path = path.parent;
        if (path != null) {
          // Add room for the separator
          totalLen++;
        }
      } while (path != null);
      StringBuilder buff = new StringBuilder(totalLen);
      try {
        toString(buff);
      } catch (IOException e) {
        throw new AssertionError("IOException should not happen with StringBuilder", e);
      }
      assert buff.length() == totalLen : "StringBuffer preallocation inconsistent with resulting String length";
      return buff.toString();
    }
  }

  /**
   * Gets a string representation of the path.
   * The root is the separator by itself ("/").
   *
   * @see FileSystem#parsePath(java.lang.String) for the inverse operation
   */
  public void toString(Appendable out) throws IOException {
    if (parent == null) {
      out.append(SEPARATOR);
    } else {
      toString0(out);
    }
  }

  /**
   * Recursive component of {@link #toString()}.
   */
  private void toString0(Appendable out) throws IOException {
    if (parent != null) {
      parent.toString0(out);
      out.append(SEPARATOR);
    }
    out.append(name);
  }

  /**
   * Gets the file system this path belongs to and is valid for.
   */
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  /**
   * Gets the parent of this path.  Only the root will have a null parent.
   */
  public Path getParent() {
    return parent;
  }

  /**
   * Gets the name of this part of the path.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the depth of this path.  The root has a zero depth.
   * This is also the number of non-root names in this path, such as the
   * number of elements that will be returned from <code>explode()</code>.
   *
   * @see #explode()
   */
  public int getDepth() {
    return depth;
  }

  /**
   * Explodes this path into a set of names, not including the empty root name
   * itself.  The root path is represented by an empty array.
   *
   * @see FileSystem#join(java.lang.String[]) for the inverse operation
   */
  public String[] explode() {
    String[] names = new String[depth];
    explode(names);
    return names;
  }

  /**
   * Explodes this path to the given array.  If the array is not filled,
   * the element one past the last name will be set to null.
   *
   * @return  the array parameter
   *
   * @throws ArrayIndexOutOfBoundsException if the provided array is of insufficient length
   *
   * @see FileSystem#join(java.lang.String[]) for the inverse operation
   */
  public String[] explode(String[] names) {
    if (names.length > this.depth) {
      names[this.depth] = null;
    }
    Path me = this;
    while (me.depth > 0) {
      names[me.depth - 1] = me.name;
      me = me.parent;
    }
    return names;
  }
}
