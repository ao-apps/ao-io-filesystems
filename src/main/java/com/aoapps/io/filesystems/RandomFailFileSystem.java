/*
 * ao-io-filesystems - Advanced filesystem utilities.
 * Copyright (C) 2015, 2019, 2020, 2021  AO Industries, Inc.
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
import com.aoapps.lang.io.IoUtils;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
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
		
		private final float probability;

		private RandomFailIOException(float probability) {
			this.probability = probability;
		}

		private RandomFailIOException(float probability, Throwable cause) {
			super(cause);
			this.probability = probability;
		}

		public float getProbability() {
			return probability;
		}

		@Override
		public String getMessage() {
			return "Random Fail: probability = " + probability;
		}

		static {
			Throwables.registerSurrogateFactory(RandomFailIOException.class, (template, cause) ->
				new RandomFailIOException(template.probability, cause)
			);
		}
	}

	public static interface FailureProbabilities {
		default float getList() {
			return 0.001f;
		}
		default float getListIterate() {
			return 0.0001f;
		}
		default float getListIterateClose() {
			return 0.001f;
		}
		default float getUnlink() {
			return 0.001f;
		}
		default float getSize() {
			return 0.001f;
		}
		default float getCreateFile() {
			return 0.001f;
		}
		default float getCreateDirectory() {
			return 0.001f;
		}
		default float getLock() {
			return 0.0001f;
		}
	}

	private final FailureProbabilities failureProbabilities;
	private final Random random;

	public RandomFailFileSystem(
		FileSystem wrappedFileSystem,
		FailureProbabilities failureProbabilities,
		Random random
	) {
		super(wrappedFileSystem);
		this.failureProbabilities = failureProbabilities;
		this.random = random;
	}

	private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(8)));

	/**
	 * Uses default probabilities and a fast Random source.
	 * 
	 * @see SecureRandom
	 */
	public RandomFailFileSystem(FileSystem wrappedFileSystem) {
		this(
			wrappedFileSystem,
			new FailureProbabilities() {},
			fastRandom
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
	public PathIterator list(Path path) throws RandomFailIOException, IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(failureProbabilities.getList());
		PathWrapper pathWrapper = (PathWrapper)path;
		return new PathIteratorWrapper(pathWrapper, wrappedFileSystem.list(pathWrapper.wrappedPath)) {
			@Override
			public boolean hasNext() throws DirectoryIteratorException {
				try {
					randomFail(failureProbabilities.getListIterate());
				} catch(RandomFailIOException e) {
					throw new DirectoryIteratorException(e);
				}
				return super.hasNext();
			}
			@Override
			public void close() throws RandomFailIOException, IOException {
				randomFail(failureProbabilities.getListIterateClose());
				super.close();
			}
		};
	}

	@Override
	public void delete(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(failureProbabilities.getUnlink());
		super.delete(path);
	}

	@Override
	public long size(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(failureProbabilities.getSize());
		return super.size(path);
	}

	@Override
	public Path createFile(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(failureProbabilities.getCreateFile());
		return super.createFile(path);
	}

	@Override
	public Path createDirectory(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(failureProbabilities.getCreateDirectory());
		return super.createDirectory(path);
	}

	@Override
	public FileLock lock(Path path) throws IOException {
		if(path.getFileSystem() != this) throw new IllegalArgumentException();
		randomFail(failureProbabilities.getLock());
		return super.lock(path);
	}
}
