package net.rguha.dc.io;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

public class SDFRecordWriter<LongWritable,Text> extends RecordWriter<LongWritable, Text> {
    public SDFRecordWriter() {
    }

    public void write(LongWritable text, Text text1) throws IOException {
//        TextOutputFormat.LineRecordWriter lrw;
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }    
}
