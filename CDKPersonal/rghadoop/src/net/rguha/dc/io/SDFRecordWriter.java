/*
 *
 * Copyright (C) 2009 Rajarshi Guha <rajarshi.guha@gmail.com>
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.rguha.dc.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

public class SDFRecordWriter<LongWritable,Text> implements RecordWriter<LongWritable, Text> {
    public SDFRecordWriter() {
    }

    public void write(LongWritable text, Text text1) throws IOException {
//        TextOutputFormat.LineRecordWriter lrw;
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close(Reporter reporter) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close(Configuration jobConf) throws IOException, InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }    
}
