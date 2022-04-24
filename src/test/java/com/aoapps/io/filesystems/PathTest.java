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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author  AO Industries, Inc.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class PathTest {

  private final TempFileSystem tempfs = new TempFileSystem();
  private final Path root = new Path(tempfs);
  private final Path bin = new Path(root, "bin");
  private final Path bash = new Path(bin, "bash");
  private final Path bin2 = new Path(root, "bin");
  private final Path cp = new Path(bin, "cp");
  private final ReadOnlyFileSystem readOnlyFs = new ReadOnlyFileSystem(tempfs);
  private final Path readOnlyBash = readOnlyFs.parsePath("/bin/bash");

  /**
   * Test constructor checks.
   */
  @Test(expected = InvalidPathException.class)
  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public void testInitNullParent() {
    System.out.println("<init>");
    new Path(null, "");
  }

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public void testInitNullName() {
    System.out.println("<init>");
    new Path(root, null);
  }

  @Test(expected = InvalidPathException.class)
  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public void testInitEmpty() {
    System.out.println("<init>");
    new Path(root, "");
  }

  @Test(expected = InvalidPathException.class)
  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public void testInitSlash() {
    System.out.println("<init>");
    new Path(root, "test" + Path.SEPARATOR);
  }

  /**
   * Test of equals method, of class Path.
   */
  @Test
  public void testEquals_Object() {
    System.out.println("equals");
    Object rootObj = root;
    Object binObj = bin;
    Object bashObj = bash;
    Object bin2Obj = bin2;
    // root
    assertTrue(root.equals(rootObj));
    assertFalse(root.equals(binObj));
    assertFalse(root.equals(bashObj));
    assertFalse(root.equals(bin2Obj));
    // bin
    assertFalse(bin.equals(rootObj));
    assertTrue(bin.equals(binObj));
    assertFalse(bin.equals(bashObj));
    assertTrue(bin.equals(bin2Obj));
    // bash
    assertFalse(bash.equals(rootObj));
    assertFalse(bash.equals(binObj));
    assertTrue(bash.equals(bashObj));
    assertFalse(bash.equals(bin2Obj));
    // bin2
    assertFalse(bin2.equals(rootObj));
    assertTrue(bin2.equals(bin2Obj));
    assertFalse(bin2.equals(bashObj));
    assertTrue(bin2.equals(binObj));
  }

  /**
   * Test of equals method, of class Path.
   */
  @Test
  public void testEquals_Path() {
    System.out.println("equals");
    // root
    assertTrue(root.equals(root));
    assertFalse(root.equals(bin));
    assertFalse(root.equals(bash));
    assertFalse(root.equals(bin2));
    // bin
    assertFalse(bin.equals(root));
    assertTrue(bin.equals(bin));
    assertFalse(bin.equals(bash));
    assertTrue(bin.equals(bin2));
    // bash
    assertFalse(bash.equals(root));
    assertFalse(bash.equals(bin));
    assertTrue(bash.equals(bash));
    assertFalse(bash.equals(bin2));
    // bin2
    assertFalse(bin2.equals(root));
    assertTrue(bin2.equals(bin2));
    assertFalse(bin2.equals(bash));
    assertTrue(bin2.equals(bin));
  }

  /**
   * Test of hashCode method, of class Path.
   */
  @Test
  public void testHashCode() {
    System.out.println("hashCode");
    assertEquals(0, new Path(tempfs).hashCode());
  }

  /**
   * Test of compareTo method, of class Path.
   */
  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    // root
    assertEquals(0, root.compareTo(root));
    assertTrue(root.compareTo(bin) < 0);
    assertTrue(root.compareTo(bash) < 0);
    // bin
    assertEquals(0, bin.compareTo(bin2));
    assertTrue(bin.compareTo(root) > 0);
    assertTrue(bin.compareTo(bash) < 0);
    assertTrue(bin.compareTo(cp) < 0);
    // bash
    assertEquals(0, bash.compareTo(bash));
    assertTrue(bash.compareTo(root) > 0);
    assertTrue(bash.compareTo(bin) > 0);
    assertTrue(bash.compareTo(cp) < 0);
    // cp
    assertEquals(0, cp.compareTo(cp));
    assertTrue(cp.compareTo(root) > 0);
    assertTrue(cp.compareTo(bin) > 0);
    assertTrue(cp.compareTo(bash) > 0);
  }

  /**
   * Test of toString method, of class Path.
   */
  @Test
  public void testToString_0args() {
    System.out.println("toString");
    assertEquals("/", root.toString());
    assertEquals("/bin", bin.toString());
    assertEquals("/bin/bash", bash.toString());
    assertEquals("/bin/cp", cp.toString());
  }

  /**
   * Test of toString method, of class Path.
   */
  @Test
  public void testToString_Appendable() throws Exception {
    System.out.println("toString");
    // Nothing to check, since this is called by regular toString
  }

  /**
   * Test of getFileSystem method, of class Path.
   */
  @Test
  public void testGetFileSystem() {
    System.out.println("getFileSystem");
    assertSame(tempfs, root.getFileSystem());
    assertSame(tempfs, bash.getFileSystem());
    assertSame(tempfs, bash.getParent().getFileSystem());
    assertSame(readOnlyFs, readOnlyBash.getFileSystem());
    assertSame(readOnlyFs, readOnlyBash.getParent().getFileSystem());
  }

  /**
   * Test of getParent method, of class Path.
   */
  @Test
  public void testGetParent() {
    System.out.println("getParent");
    assertNull(root.getParent());
    assertSame(root, bin.getParent());
    assertSame(bin, bash.getParent());
    assertSame(root, bin2.getParent());
    assertSame(bin, cp.getParent());
  }

  /**
   * Test of getName method, of class Path.
   */
  @Test
  public void testGetName() {
    System.out.println("getName");
    assertEquals("", root.getName());
    assertEquals("bin", bin.getName());
    assertEquals("bash", bash.getName());
    assertEquals("bin", bin2.getName());
    assertEquals("cp", cp.getName());
    assertEquals("bash", readOnlyBash.getName());
  }

  /**
   * Test of getDepth method, of class Path.
   */
  @Test
  public void testGetDepth() {
    System.out.println("getDepth");
    assertEquals(0, root.getDepth());
    assertEquals(1, bin.getDepth());
    assertEquals(2, bash.getDepth());
    assertEquals(1, bin2.getDepth());
    assertEquals(2, cp.getDepth());
    assertEquals(2, readOnlyBash.getDepth());
  }

  /**
   * Test of explode method, of class Path.
   */
  @Test
  public void testExplode_0args() {
    System.out.println("explode");
    assertArrayEquals(
        new String[0],
        root.explode()
    );
    assertArrayEquals(
        new String[]{"bin"},
        bin.explode()
    );
    assertArrayEquals(
        new String[]{"bin", "bash"},
        bash.explode()
    );
    assertArrayEquals(
        new String[]{"bin"},
        bin2.explode()
    );
    assertArrayEquals(
        new String[]{"bin", "cp"},
        cp.explode()
    );
    assertArrayEquals(
        new String[]{"bin", "bash"},
        readOnlyBash.explode()
    );
  }

  /**
   * Test of explode method, of class Path.
   */
  @Test
  public void testExplode_StringArr() {
    System.out.println("explode");
    assertArrayEquals(
        new String[]{null, "2", "3", "4", "5"},
        root.explode(new String[]{"1", "2", "3", "4", "5"})
    );
    assertArrayEquals(
        new String[]{"bin", null, "3", "4", "5"},
        bin.explode(new String[]{"1", "2", "3", "4", "5"})
    );
    assertArrayEquals(
        new String[]{"bin", "bash", null, "4", "5"},
        bash.explode(new String[]{"1", "2", "3", "4", "5"})
    );
    assertArrayEquals(
        new String[]{"bin", null, "3", "4", "5"},
        bin2.explode(new String[]{"1", "2", "3", "4", "5"})
    );
    assertArrayEquals(
        new String[]{"bin", "cp", null, "4", "5"},
        cp.explode(new String[]{"1", "2", "3", "4", "5"})
    );
    assertArrayEquals(
        new String[]{"bin", "bash", null, "4", "5"},
        readOnlyBash.explode(new String[]{"1", "2", "3", "4", "5"})
    );
  }
}
