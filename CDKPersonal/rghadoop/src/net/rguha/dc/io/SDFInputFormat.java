package net.rguha.dc.io;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;

import java.io.IOException;


public class SDFInputFormat extends TextInputFormat {

    public RecordReader<LongWritable, Text> createRecordReader(InputSplit inputSplit,
                                                               JobConf jobConf,
                                                               Reporter reporter) throws IOException {
        return new SDFRecordReader(jobConf, inputSplit);
    }
}
