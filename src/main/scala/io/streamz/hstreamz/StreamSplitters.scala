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

import scala.io.Source
import io.streamz.{InputSplitter, TextStreamSplitter}
import io.streamz.event.StreamTuple

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

// TODO: Handle Avro and Sequence files
object StreamSplitters {
  def apply(streams: List[Source], headers: Map[String, Int], routeKey: String) = {
    streams.map {
      src => {
        new TextStreamSplitter(src, new InputSplitter[String]{
          def split(in: String) = {
            val items = in.split(',')
            val map = headers.map(i => (i._1, items(i._2)))
            Array(StreamTuple("event", routeKey, map))
          }
        })
      }
    }
  }
}
