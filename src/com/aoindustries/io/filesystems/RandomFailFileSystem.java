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

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * A file system implementation that randomly fails, this is used by test
 * suites to verify correct behavior under expected failure modes.
 *
 * @author  AO Industries, Inc.
 */
public class RandomFailFileSystem extends FileSystemWrapper {

	/**
	 * Thrown when a failure occurs randomly.
	 */
	public static class RandomFailIOException extends IOException {
		private static final long serialVersionUID = 1L;
		
		private RandomFailIOException(float probability) {
			super("Random Fail: probability = " + probability);
		}
	}

	/**
	 * Default probabilities
	 */
	public static final float
		DEFAULT_LIST_FAILURE_PROBABILITY = 0.001f,
		DEFAULT_LIST_ITERATE_FAILURE_PROBABILITY = 0.0001f,
		DEFAULT_LIST_ITERATE_CLOSE_FAILURE_PROBABILITY = 0.001f,
		DEFAULT_UNLINK_FAILURE_PROBABILITY = 0.001f,
		DEFAULT_SIZE_FAILURE_PROBABILITY = 0.001f
	;

	private final float listFailureProbability;
	private final float listIterateFailureProbability;
	private final float listIterateCloseFailureProbability;
	private final float unlinkFailureProbability;
	private final float sizeFailureProbability;
	private final Random random;

	public RandomFailFileSystem(
		FileSystem wrappedFileSystem,
		float listFailureProbability,
		float listIterateFailureProbability,
		float listIterateCloseFailureProbability,
		float unlinkFailureProbability,
		float sizeFailureProbability,
		Random random
	) {
		super(wrappedFileSystem);
		this.listFailureProbability = listFailureProbability;
		this.listIterateFailureProbability = listIterateFailureProbability;
		this.listIterateCloseFailureProbability = listIterateCloseFailureProbability;
		this.unlinkFailureProbability = unlinkFailureProbability;
		this.sizeFailureProbability = sizeFailureProbability;
		this.random = random;
	}

	/**
	 * Uses default probabilities and a SecureRandom source.
	 * 
	 * @see SecureRandom
	 */
	public RandomFailFileSystem(FileSystem wrappedFileSystem) {
		this(
			wrappedFileSystem,
			DEFAULT_LIST_FAILURE_PROBABILITY,
			DEFAULT_LIST_ITERATE_FAILURE_PROBABILITY,
			DEFAULT_LIST_ITERATE_CLOSE_FAILURE_PROBABILITY,
			DEFAULT_UNLINK_FAILURE_PROBABILITY,
			DEFAULT_SIZE_FAILURE_PROBABILITY,
			new SecureRandom()
		);
	}

	protected void randomFail(float probability) throws RandomFailIOException {
		if(
			probability > 0
			&& (
				probability >= 1
				|| random.nextFloat() < probability
			)
		) {
			throw new RandomFailIOException(probability);
		}
	}

	/**
	 * Random chance of fail on list as well as list iteration.
	 */
	@Override
	public PathIterator list(Path path) throws RandomFailIOException, NoSuchFileException, NotDirectoryException, IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(listFailureProbability);
		PathWrapper pathWrapper = (PathWrapper)path;
		return new PathIteratorWrapper(pathWrapper, wrappedFileSystem.list(pathWrapper.wrappedPath)) {
			@Override
			public boolean hasNext() throws DirectoryIteratorException {
				try {
					randomFail(listIterateFailureProbability);
				} catch(RandomFailIOException e) {
					throw new DirectoryIteratorException(e);
				}
				return super.hasNext();
			}
			@Override
			public void close() throws RandomFailIOException, IOException {
				randomFail(listIterateCloseFailureProbability);
				super.close();
			}
		};
	}

	@Override
	public void delete(Path path) throws NoSuchFileException, DirectoryNotEmptyException, IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(unlinkFailureProbability);
		super.delete(path);
	}

	@Override
	public long size(Path path) throws NoSuchFileException, IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(sizeFailureProbability);
		return super.size(path);
	}
}
