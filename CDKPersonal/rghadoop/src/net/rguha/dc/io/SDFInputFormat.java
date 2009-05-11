package net.rguha.dc.io;

import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;


public class SDFInputFormat extends FileInputFormat {

    public RecordReader getRecordReader(InputSplit inputSplit,
                                                               JobConf jobConf,
                                                               Reporter reporter) throws IOException {
        return new SDFRecordReader(jobConf, inputSplit);
    }
  
}
