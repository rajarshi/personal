package net.rguha.dc.io;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/**
 * Handle multi-line SDF records.
 * <p/>
 * Based on code found <a href="http://www.nabble.com/Re%3A-map-reduce-function-on-xml-string-p15835195.html">here</a>
 * which handles records in an XML file.
 */
public class SDFInputFormat extends TextInputFormat {

    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) {
        return new SDFRecordReader();
    }
}
