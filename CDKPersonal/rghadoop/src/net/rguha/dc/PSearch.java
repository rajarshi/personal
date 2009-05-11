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
import org.apache.hadoop.util.GenericOptionsParser;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.pharmacophore.PharmacophoreMatcher;
import org.openscience.cdk.pharmacophore.PharmacophoreQuery;
import org.openscience.cdk.pharmacophore.PharmacophoreUtils;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

public class PSearch {

    private static PharmacophoreMatcher pmatcher = new PharmacophoreMatcher();

    private final static IntWritable one = new IntWritable(1);
    private final static IntWritable zero = new IntWritable(0);

    public static class MoleculeMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

        private Text matches = new Text();

        @Override
        public void configure(JobConf configuration) {
            String pattern = configuration.get("net.rguha.dc.data.pcoredef");
            try {
                StringBufferInputStream sbis = new StringBufferInputStream(pattern);
                List<PharmacophoreQuery> defs = PharmacophoreUtils.readPharmacophoreDefinitions(sbis);
                if (defs.size() < 1) throw new CDKException("Must provide at least 1 pharmacophore definition");
                PharmacophoreQuery query = defs.get(0);
                pmatcher.setPharmacophoreQuery(query);
            } catch (CDKException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println("Error in map setup: " + e.getMessage());

            }
        }

        public void map(LongWritable key, Text value,
                        OutputCollector<Text, IntWritable> output,
                        Reporter reporter) throws IOException {
            try {
                StringReader sreader = new StringReader(value.toString());
                MDLV2000Reader reader = new MDLV2000Reader(sreader);
                ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
                List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
                IAtomContainer molecule = containersList.get(0);

                boolean matched = pmatcher.matches(molecule);
                matches.set((String) molecule.getProperty(CDKConstants.TITLE));
                if (matched) output.collect(matches, one);
                else output.collect(matches, zero);
            } catch (CDKException e) {
                e.printStackTrace();
            }
        }
    }

    public static class PCoreMatchReducer
            extends MapReduceBase
            implements Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();


        public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output,
                           Reporter reporter) throws IOException {
            while (values.hasNext()) {
                if (values.next().get() == 0) {
                    result.set(1);
                    output.collect(key, result);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        JobConf conf = new JobConf(configuration, PSearch.class);

        conf.set("mapred.job.tracker", "local");

        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 3) {
            System.err.println("Usage: psearch <in> <out> <pcorefile>");
            System.exit(2);
        }

        // read in the 
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(otherArgs[2]));
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            else sb.append(line);
        }
        reader.close();

        // need to set it before we create the Job object
        conf.set("net.rguha.dc.data.pcoredef", sb.toString());

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(MoleculeMapper.class);
        conf.setCombinerClass(PCoreMatchReducer.class);
        conf.setReducerClass(PCoreMatchReducer.class);
        conf.setInputFormat(SDFInputFormat.class);

        FileInputFormat.setInputPaths(conf, args[0]);
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        JobClient.runJob(conf);
        System.exit(0);
    }
}