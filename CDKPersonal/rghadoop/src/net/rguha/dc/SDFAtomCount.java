package net.rguha.dc;

public class SDFAtomCount {

    /**
    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text haCount = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            try {
                StringReader sreader = new StringReader(value.toString());
                MDLV2000Reader reader = new MDLV2000Reader(sreader);
                ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
                List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
                IAtomContainer molecule = containersList.get(0);
                for (IAtom atom : molecule.atoms()) {
                    haCount.set(atom.getSymbol());
                    context.write(haCount, one);
                }
            } catch (CDKException e) {
                e.printStackTrace();
            }
        }
    }

    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: countha <in> <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "haCount count");
        job.setJarByClass(SDFAtomCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(SDFInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
     */
}