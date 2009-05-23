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

package net.rguha.dc;

import net.rguha.dc.io.SDFInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

public class Identity {

    private final static IntWritable one = new IntWritable(1);

    public static class MapperClass extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

        private Text matches = new Text();

        public void map(LongWritable key, Text value,
                        OutputCollector<Text, IntWritable> output,
                        Reporter reporter) throws IOException {
            try {
                StringReader sreader = new StringReader(value.toString());
                MDLV2000Reader reader = new MDLV2000Reader(sreader);
                ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
                List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
                IAtomContainer molecule = containersList.get(0);
                matches.set((String) molecule.getProperty(CDKConstants.TITLE));
                output.collect(matches, one);
            } catch (CDKException e) {
                e.printStackTrace();
            }
        }

    }

    public static class ReducerClass
                extends MapReduceBase
                implements Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output,
                           Reporter reporter) throws IOException {
            while (values.hasNext()) {
                values.next().get();
                result.set(1);
                output.collect(key, result);
            }

        }
    }

    public static int run(String[] args, Configuration configuration) throws Exception {
        JobConf conf = new JobConf(configuration, HeavyAtomCount.class);
        conf.setJobName("identity");
//        conf.set("mapred.job.tracker", "local");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(MapperClass.class);
        conf.setCombinerClass(ReducerClass.class);
        conf.setReducerClass(ReducerClass.class);
        conf.setInputFormat(SDFInputFormat.class);

        FileInputFormat.setInputPaths(conf, args[0]);
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        JobClient.runJob(conf);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: identity <in> <out>");
            System.exit(-1);
        }
        int res = run(args, new Configuration());
        System.exit(res);
    }

}