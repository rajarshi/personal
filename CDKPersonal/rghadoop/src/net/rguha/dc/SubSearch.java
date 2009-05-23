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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
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
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import java.io.IOException;

public class SubSearch extends Configured implements Tool {

    static SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    static SMARTSQueryTool sqt;static {
        try {
            sqt = new SMARTSQueryTool("C");
        } catch (CDKException e) {
        }
    }

    private final static IntWritable one = new IntWritable(1);

    public static class MoleculeMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

        private Text matches = new Text();

        @Override
        public void configure(JobConf configuration) {
            String pattern = configuration.get("net.rguha.dc.data.pattern");
            try {
                sqt.setSmarts(pattern);
            } catch (CDKException e) {
            }
        }

        public void map(LongWritable key, Text value,
                        OutputCollector<Text, IntWritable> output,
                        Reporter reporter) throws IOException {
            try {
                IAtomContainer molecule = parser.parseSmiles(value.toString());
                boolean matched = sqt.matches(molecule);

                String title = (String) molecule.getProperty(CDKConstants.TITLE);
                if (title == null) title = value.toString();

                matches.set(title);
                if (matched) output.collect(matches, one);
            } catch (CDKException e) {
                e.printStackTrace();
            }
        }
    }


    public int run(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: subsearch <in.smi> <out> <pattern>");
            System.err.println("Rajarshi Guha <rajarshi.guha@gmail.com>");
            System.exit(2);
        }

        JobConf conf = new JobConf(getConf(), PSearch.class);
        conf.setJobName("subsearch");

        // need to set it before we create the Job object
        conf.set("net.rguha.dc.data.pattern", args[2]);

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);
        conf.setMapperClass(MoleculeMapper.class);        
        FileInputFormat.setInputPaths(conf, args[0]);
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        JobClient.runJob(conf);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new SubSearch(), args);
        System.exit(res);
    }

}