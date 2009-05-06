package net.rguha.dc.io;

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
    private boolean beyondSplitEnd = false;
    private boolean readOneExtra = false;

    private LongWritable key = new LongWritable();
    private Text value = new Text();

    private FSDataInputStream fsin;
    private DataOutputBuffer buffer = new DataOutputBuffer();

    private byte[] endTag = "$$$$\n".getBytes();

    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        FileSplit split = (FileSplit) inputSplit;
        Configuration conf = taskAttemptContext.getConfiguration();
        Path path = split.getPath();
        FileSystem fs = path.getFileSystem(conf);

        fsin = fs.open(path);
        start = split.getStart();
        end = split.getStart() + split.getLength();
        fsin.seek(start);

        // if we're not at the beginning of the input file,
        // sread and discard the current (possibly incomplete) record
        // and flag that we should read the last record even if it goes
        // beyond the end of the current chunk.
        if (start != 0) {
            readUntilMatch(endTag, false);
            readOneExtra = true;
        }
    }

    public boolean nextKeyValue() throws IOException {
        if (readUntilMatch(endTag, true)) {
            try {
                value = new Text();
                value.set(buffer.getData(), 0, buffer.getLength());
                key = new LongWritable(fsin.getPos());
                buffer.reset();
                return true;
            } finally {
                buffer.reset();
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
                // if we started from the middle of the input file,
                // we will have read  and discarded the first record  of this
                // chunk (which is likely
                // incomplete), but indicated that we should then read the last
                // record of the chunk, even if we go beyond the chunk. But after
                // reading this last record, we must indicate no more records in
                // this chunk.
                if (i >= match.length) {
                    return !readOneExtra;
                }
            } else i = 0;
        }
    }

}
