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
