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

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.util.NoSuchElementException;

/**
 * A file system that wraps another to intercept and otherwise modify interactions.
 *
 * @author  AO Industries, Inc.
 */
public abstract class FileSystemWrapper implements FileSystem {

  /**
   * Wraps a {@link Path}.
   */
  protected static class PathWrapper extends Path {
    protected final Path wrappedPath;

    /**
     * Wraps a root.
     */
    private PathWrapper(FileSystemWrapper wrapper, Path wrappedRoot) {
      super(wrapper);
      this.wrappedPath = wrappedRoot;
    }

    /**
     * Wraps a non-root.
     */
    private PathWrapper(PathWrapper parent, Path wrappedPath) {
      super(parent, wrappedPath.getName());
      assert parent.wrappedPath == wrappedPath.getParent();
      this.wrappedPath = wrappedPath;
    }
  }

  /**
   * Wraps a new sub path.
   */
  protected PathWrapper wrapSubPath(PathWrapper parent, Path subPath) {
    return new PathWrapper(parent, subPath);
  }

  /**
   * Wraps a path.
   */
  protected PathWrapper wrapPath(Path path) {
    return
        (path.getParent() == null)
            ? new PathWrapper(this, path)
            : wrapSubPath(wrapPath(path.getParent()), path);
  }

  /**
   * Unwraps a path.
   */
  protected Path unwrapPath(Path path) {
    assert path.getFileSystem() == this;
    Path wrappedPath = ((PathWrapper) path).wrappedPath;
    assert wrappedPath.getFileSystem() == wrappedFileSystem;
    return wrappedPath;
  }

  protected final FileSystem wrappedFileSystem;

  protected FileSystemWrapper(FileSystem wrappedFileSystem) {
    this.wrappedFileSystem = wrappedFileSystem;
  }

  @Override
  public void checkSubPath(Path parent, String name) throws InvalidPathException {
    if (parent.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    wrappedFileSystem.checkSubPath(unwrapPath(parent), name);
  }

  @Override
  public Path join(String[] names) throws InvalidPathException {
    return wrapPath(wrappedFileSystem.join(names));
  }

  @Override
  public Path parsePath(String value) throws InvalidPathException {
    return wrapPath(wrappedFileSystem.parsePath(value));
  }

  /**
   * Wraps {@link PathIterator}, wrapping each result in {@link #next()}.
   */
  protected class PathIteratorWrapper extends PathIterator {

    protected final PathWrapper parent;
    protected final PathIterator wrappedIter;

    protected PathIteratorWrapper(PathWrapper parent, PathIterator wrappedIter) {
      this.parent = parent;
      this.wrappedIter = wrappedIter;
    }

    @Override
    public boolean hasNext() throws DirectoryIteratorException {
      return wrappedIter.hasNext();
    }

    @Override
    public PathWrapper next() throws NoSuchElementException {
      return wrapSubPath(parent, wrappedIter.next());
    }

    @Override
    public void close() throws IOException {
      wrappedIter.close();
    }
  }

  @Override
  public PathIterator list(Path path) throws IOException {
    if (path.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    PathWrapper pathWrapper = (PathWrapper) path;
    return new PathIteratorWrapper(pathWrapper, wrappedFileSystem.list(pathWrapper.wrappedPath));
  }

  @Override
  public void delete(Path path) throws IOException {
    if (path.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    wrappedFileSystem.delete(unwrapPath(path));
  }

  @Override
  public long size(Path path) throws IOException {
    if (path.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    return wrappedFileSystem.size(unwrapPath(path));
  }

  @Override
  public Path createFile(Path path) throws IOException {
    if (path.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    wrappedFileSystem.createFile(unwrapPath(path));
    return path;
  }

  @Override
  public Path createDirectory(Path path) throws IOException {
    if (path.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    wrappedFileSystem.createDirectory(unwrapPath(path));
    return path;
  }

  @Override
  public FileLock lock(Path path) throws IOException {
    if (path.getFileSystem() != this) {
      throw new IllegalArgumentException();
    }
    return wrappedFileSystem.lock(unwrapPath(path));
  }
}
