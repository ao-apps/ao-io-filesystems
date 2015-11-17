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
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * A file system implementation that randomly fails, this is used by test
 * suites to verify correct behavior under expected failure modes.
 *
 * @author  AO Industries, Inc.
 */
public class RandomFailFileSystem implements FileSystem {

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
		DEFAULT_LIST_FAILURE_PROBABILITY = 0.001f
	;

	private final FileSystem wrapped;
	private final float listFailureProbability;
	private final Random random;

	public RandomFailFileSystem(
		FileSystem wrapped,
		float listFailureProbability,
		Random random
	) {
		this.wrapped = wrapped;
		this.listFailureProbability = listFailureProbability;
		this.random = random;
	}

	/**
	 * Uses default probabilities and a SecureRandom source.
	 * 
	 * @see SecureRandom
	 */
	public RandomFailFileSystem(FileSystem wrapped) {
		this(
			wrapped,
			DEFAULT_LIST_FAILURE_PROBABILITY,
			new SecureRandom()
		);
	}

	/**
	 * Defers to the wrapped file system.
	 */
	@Override
	public Path checkPath(Path path) throws InvalidPathException {
		return wrapped.checkPath(path);
	}

	private void randomFail(float probability) throws RandomFailIOException {
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
	 * Defers to the wrapped file system.
	 */
	@Override
	public String[] list(Path path) throws InvalidPathException, RandomFailIOException, FileNotFoundException, IOException {
		checkPath(path);
		randomFail(listFailureProbability);
		return wrapped.list(path);
	}
}
