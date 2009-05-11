package net.rguha.dc;

public class SubSearch {
    /*
    static SMARTSQueryTool sqt;static {
        try {
            sqt = new SMARTSQueryTool("C");
        } catch (CDKException e) {
        }
    }

    private final static IntWritable one = new IntWritable(1);
    private final static IntWritable zero = new IntWritable(0);

    public static class MoleculeMapper extends Mapper<Object, Text, Text, IntWritable> {

        private Text matches = new Text();
        private String pattern;

        public void setup(Context context) {
            pattern = context.getConfiguration().get("net.rguha.dc.data.pattern");
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            try {
                StringReader sreader = new StringReader(value.toString());
                MDLV2000Reader reader = new MDLV2000Reader(sreader);
                ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
                List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
                IAtomContainer molecule = containersList.get(0);

                sqt.setSmarts(pattern);
                boolean matched = sqt.matches(molecule);
                matches.set((String) molecule.getProperty(CDKConstants.TITLE));
                if (matched) context.write(matches, one);
                else context.write(matches, zero);
            } catch (CDKException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SMARTSMatchReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
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
            System.err.println("Usage: subsearch <in> <out> <pattern>");
            System.exit(2);
        }

        // need to set it before we create the Job object
        conf.set("net.rguha.dc.data.pattern", otherArgs[2]);
        
        Job job = new Job(conf, "id 1");
        job.setJarByClass(SubSearch.class);
        job.setMapperClass(MoleculeMapper.class);
        job.setCombinerClass(SMARTSMatchReducer.class);
        job.setReducerClass(SMARTSMatchReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(SDFInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
    */
}