package net.rguha.dc.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.TextOutputFormat;

import java.io.IOException;


public class SDFOutputFormat extends TextOutputFormat {
    public RecordWriter getRecordWriter(Configuration jobConf) throws IOException, InterruptedException {
        return new SDFRecordWriter<LongWritable,Text>();
    }
}
