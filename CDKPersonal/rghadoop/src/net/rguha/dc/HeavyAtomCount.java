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
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.IOException;
import java.util.Iterator;

public class HeavyAtomCount extends Configured implements Tool {
    static SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    public static class TokenizerMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value,
                        OutputCollector<Text, IntWritable> output,
                        Reporter reporter) throws IOException {
            try {
                IAtomContainer molecule = sp.parseSmiles(value.toString());
                for (IAtom atom : molecule.atoms()) {
                    word.set(atom.getSymbol());
                    output.collect(word, one);
                }
            } catch (InvalidSmilesException e) {
                // do nothing for now
            }
        }
    }


    public static class Reduce extends MapReduceBase
            implements Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output,
                           Reporter reporter) throws IOException {

            int sum = 0;
            while (values.hasNext()) {
                sum += values.next().get();
            }
            output.collect(key, new IntWritable(sum));
        }
    }

    public int run(String[] args) throws Exception {
        JobConf conf = new JobConf(getConf(), HeavyAtomCount.class);
        conf.setJobName("heavyAtomCount");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(TokenizerMapper.class);
        conf.setCombinerClass(Reduce.class);
        conf.setReducerClass(Reduce.class);
       
        FileInputFormat.setInputPaths(conf, args[0]);
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        JobClient.runJob(conf);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: heavyAtomCount infile outfile");
            System.exit(-1);
        }
        int res = ToolRunner.run(new Configuration(), new HeavyAtomCount(), args);
        System.exit(res);
    }
}