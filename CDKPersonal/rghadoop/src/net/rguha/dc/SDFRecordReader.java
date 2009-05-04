package net.rguha.dc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

public class SDFRecordReader extends RecordReader<LongWritable, Text> {
    private long start, end;
    private boolean more = true;

    private LongWritable key = null;
    private Text value = null;

    private FSDataInputStream fsin;
    private DataOutputBuffer buffer = new DataOutputBuffer();

    private byte[] endTag = "$$$$".getBytes();

    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        FileSplit split = (FileSplit) inputSplit;
        Configuration conf = taskAttemptContext.getConfiguration();
        Path path = split.getPath();
        FileSystem fs = path.getFileSystem(conf);

        fsin = fs.open(path);
        end = split.getStart() + split.getLength();
        fsin.seek(start);
        start = fsin.getPos();

    }

    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (fsin.getPos() < end) {
            if (readUntilMatch(endTag, true)) {
                try {
//                    key.set(Long.toString(fsin.getPos()));
                    key.set(fsin.getPos());
                    value.set(buffer.getData(), 0, buffer.getLength());
                    return true;
                } finally {
                    buffer.reset();
                }
            }
        }
        return false;
    }

    public LongWritable getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    public Text getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    public float getProgress() throws IOException, InterruptedException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() throws IOException {
        fsin.close();
    }

    private boolean readUntilMatch(byte[] match, boolean withinBlock) throws IOException {
        int i = 0;
        while (true) {
            int b = fsin.read();
            // end of file:
            if (b == -1) return false;
            // save to buffer:
            if (withinBlock) buffer.write(b);

            // check if we're matching:
            if (b == match[i]) {
                i++;
                if (i >= match.length) return true;
            } else i = 0;
            // see if we've passed the stop point:
            if (!withinBlock && i == 0 && fsin.getPos() >= end) return false;
        }
    }

}
