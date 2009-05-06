package net.rguha.dc;

import net.rguha.dc.io.SDFInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
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
import java.util.List;

public class PSearch {

    private static PharmacophoreMatcher pmatcher = new PharmacophoreMatcher();

    private final static IntWritable one = new IntWritable(1);
    private final static IntWritable zero = new IntWritable(0);

    public static class MoleculeMapper extends Mapper<Object, Text, Text, IntWritable> {

        private Text matches = new Text();

        public void setup(Context context) {
            String pattern = context.getConfiguration().get("net.rguha.dc.data.pcoredef");
            try {
                StringBufferInputStream sbis = new StringBufferInputStream(pattern);
                List<PharmacophoreQuery> defs = PharmacophoreUtils.readPharmacophoreDefinitions(sbis);
                if (defs.size() < 1) throw new CDKException("Must provide at least 1 pharmacophore definition");
                PharmacophoreQuery query = defs.get(0);
                pmatcher.setPharmacophoreQuery(query);
            } catch (CDKException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println("Error in map setup: "+e.getMessage());

            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            try {
                StringReader sreader = new StringReader(value.toString());
                MDLV2000Reader reader = new MDLV2000Reader(sreader);
                ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
                List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
                IAtomContainer molecule = containersList.get(0);

                boolean matched = pmatcher.matches(molecule);
                matches.set((String) molecule.getProperty(CDKConstants.TITLE));
                if (matched) context.write(matches, one);
                else context.write(matches, zero);
            } catch (CDKException e) {
                e.printStackTrace();
            }
        }
    }

    public static class PCoreMatchReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context) throws IOException, InterruptedException {
            for (IntWritable val : values) {
                if (val.compareTo(one) == 0) {
                    result.set(1);
                    context.write(key, result);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

//        conf.set("mapred.job.tracker", "local");

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

        Job job = new Job(conf, "id 1");
        job.setJarByClass(PSearch.class);
        job.setMapperClass(MoleculeMapper.class);
        job.setCombinerClass(PCoreMatchReducer.class);
        job.setReducerClass(PCoreMatchReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(SDFInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}