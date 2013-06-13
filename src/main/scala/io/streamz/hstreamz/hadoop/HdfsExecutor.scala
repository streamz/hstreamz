/**
 * Copyright (C) 2011 streamz.io
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * -------------------------
 *    _|_ _ _ _  _ __
 *   _)|_| (-(_|||| /__
 *          streamz.io ~~~
 * -------------------------
 */
package io.streamz.hstreamz.hadoop

import org.apache.hadoop.fs.{FileStatus, Path, FileSystem}
import java.net.URI
import org.apache.hadoop.conf.Configuration
import scala.io.Source
import java.io.OutputStream

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

sealed trait FsCmd { def name: String }
case object LS extends FsCmd { val name = "LS" }
case object Get extends FsCmd { val name = "Get" }
case object Put extends FsCmd { val name = "Put" }
case object Del extends FsCmd { val name = "Del" }

trait Executor[T] {
  def exec(in: String): T
}

object HdfsExecutor {
  def apply(uri: URI, user: String) = new HdfsExecutor(uri, user)
}

class HdfsExecutor(private val uri: URI, private val user: String)
  extends AutoCloseable {
  private val conf = new Configuration
  conf.setBoolean("fs.hdfs.impl.disable.cache", true)
  private val hdfs = FileSystem.get(uri, conf, user)
  def close() { hdfs.close() }
  def exec[T](cmd: FsCmd, in: String): T = {
    cmd match {
      case LS => new Executor[Array[FileStatus]] {
        def exec(in: String) = hdfs.listStatus(new Path(hdfs.getWorkingDirectory, in))
      }.exec(in).asInstanceOf[T]
      case Get => new Executor[Source] {
        def exec(in: String) = Source.fromInputStream(hdfs.open(new Path(hdfs.getWorkingDirectory, in)))
      }.exec(in).asInstanceOf[T]
      case Put => new Executor[OutputStream] {
        def exec(in: String) = hdfs.create(new Path(hdfs.getWorkingDirectory, in), true)
      }.exec(in).asInstanceOf[T]
      case Del => new Executor[Boolean] {
        def exec(in: String) = hdfs.delete(new Path(hdfs.getWorkingDirectory, in), true)
      }.exec(in).asInstanceOf[T]
    }
  }
}