/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.bck2brwr.vm4brwsr

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.runtime.RuntimeLong

@JSExportAll
@JSExportTopLevel( "Longs" )
object Module {
  def to64(lo: Int, hi: Int): Any = new RuntimeLong(lo, hi)
  def high32(a: RuntimeLong): Int = a.hi;
  def low32(a: RuntimeLong): Int = a.lo;
  def fromDouble(a: Double): Long = a.toLong;
  def toDouble(a: Long): Double = a;
  def add64(a: Long, b: Long): Long = a + b;
  def sub64(a: Long, b: Long): Long = a - b;
  def mul64(a: Long, b: Long): Long = a * b;
  def div64(a: Long, b: Long): Long = a / b;
  def mod64(a: Long, b: Long): Long = a % b;
  def and64(a: Long, b: Long): Long = a & b;
  def or64(a: Long, b: Long): Long = a | b;
  def xor64(a: Long, b: Long): Long = a ^ b;
  def neg64(a: Long): Long = -a;
  def shl64(a: Long, n : Int): Long = a << n;
  def shr64(a: Long, n : Int): Long = a >> n;
  def ushr64(a: Long, n : Int): Long = a >>> n;
  def compare64(a: Long, b: Long): Int = a.compareTo(b);
}

