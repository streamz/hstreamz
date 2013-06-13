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
package io.streamz.hstreamz

import scopt.immutable.OptionParser
import java.net.{URLClassLoader, URI, URL}
import io.streamz.hstreamz.hadoop.{Get, LS, HdfsExecutor}
import org.apache.hadoop.fs.FileStatus
import scala.Array
import scala.io.Source
import io.streamz.StreamContext
import java.io.{FilenameFilter, File}
import io.streamz.script.ScriptableProc

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

case class Config(
  home: String = null,
  hdfs: String = null,
  hdfsUser: String = null,
  hdfsSrcFile: String = null,
  hdfsDestFile: String = null,
  schema: String = null,
  scripts: String = null,
  srccode: String = null,
  cluster: String = null,
  protocol: String = null)

object Main {
  def main(args: Array[String]) {
    val parser = new OptionParser[Config]("hstreamz", "0.0.1") {
      def options = Seq(
        opt("home", "hadoop home, path to hadoop jars.") { (v: String, c: Config) => c.copy(home = v) },
        opt("hdfs", "the hdfs uri.") { (v: String, c: Config) => c.copy(hdfs = v) },
        opt("user", "the hdfsUser.") { (v: String, c: Config) => c.copy(hdfsUser = v) },
        argOpt("src", "a file or directory on hdfs.") { (v: String, c: Config) => c.copy(hdfsSrcFile = v) },
        argOpt("dest", "an optional destination file on hdfs.") { (v: String, c: Config) => c.copy(hdfsDestFile = v) },
        opt("schema", "describes the csv.") { (v: String, c: Config) => c.copy(schema = v) },
        opt("scripts", "the streamz scripts to load.") { (v: String, c: Config) => c.copy(scripts = v) },
        opt("srccode", "the directory or .jar file where streamz scripts are located.") { (v: String, c: Config) => c.copy(srccode = v) },
        opt("cluster", "the streamz cluster to connect to.") { (v: String, c: Config) => c.copy(cluster = v) },
        opt("protocol", "the streamz protocol url.") { (v: String, c: Config) => c.copy(protocol = v) },
        help("?", "help", "Show a usage message and exit")
      )
    }

    val conf = parser.parse(args, Config()).getOrElse(null)
    if (conf == null || !validate(conf)) parser.showUsage
    else run(conf)
  }

  private def run(conf: Config) {
    val props = Map(
      "protocol"-> conf.protocol,
      "cluster"->conf.cluster,
      "processor"->classOf[ScriptableProc].getName,
      "scripts"->conf.scripts,
      "code" -> conf.srccode)

    // add .jars to classpath
    val paths = conf.home.split(',')
    paths.foreach {
      p => {
        val f = new File(p)
        if (f.isDirectory) {
          val files = f.listFiles(new FilenameFilter {
            def accept(file: File, name: String) = name.endsWith(".jar")
          })
          files.foreach(jar => addPath(jar.toURI.toURL))
        }
        else addPath(f.toURI.toURL)
      }
    }

    // locate the files on hdfs
    val fs = HdfsExecutor(URI.create(conf.hdfs), conf.hdfsUser)
    val uris = fs.exec[Array[FileStatus]](LS, conf.hdfsSrcFile).filter(f => !f.isDirectory).map(f => f.getPath.toUri)

    // create a list of splitters
    val sources = uris.map(uri => fs.exec[Source](Get, uri.toASCIIString))
    val splitters = StreamSplitters(sources.toList, conf.schema.split(","))

    // create a context
    val ctx = new StreamContext(props, splitters)

    // stream to cluster
    ctx.stream()

    // close and cleanup
    fs.close()
    ctx.close()
  }

  private def validate(conf: Config) = {
    if (conf.cluster == null) false
    else if (conf.home == null) false
    else if (conf.hdfsUser == null) false
    else if (conf.hdfs == null) false
    else if (conf.hdfsSrcFile == null) false
    else if (conf.protocol == null) false
    else if (conf.schema == null) false
    else if (conf.scripts == null) false
    else if (conf.srccode == null) false
    else true
  }

  private def addPath(u: URL) {
    val urlClassLoader = ClassLoader.getSystemClassLoader
    val method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])
    method.setAccessible(true)
    method.invoke(urlClassLoader, u)
  }
}
