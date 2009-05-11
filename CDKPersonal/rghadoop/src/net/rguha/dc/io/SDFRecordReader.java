package net.rguha.dc.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;

public class SDFRecordReader implements RecordReader<LongWritable, Text> {

    private long end;
    private boolean stillInChunk = true;

    private FSDataInputStream fsin;
    private DataOutputBuffer buffer = new DataOutputBuffer();
    private long totalLength;

    private byte[] endTag = "$$$$\n".getBytes();


    public SDFRecordReader(Configuration jobConf, InputSplit inputSplit) throws IOException {
        FileSplit split = (FileSplit) inputSplit;
        Path path = split.getPath();
        FileSystem fs = path.getFileSystem(jobConf);

        fsin = fs.open(path);
        long start = split.getStart();
        end = split.getStart() + split.getLength();
        fsin.seek(start);
        totalLength = split.getLength();

        if (start != 0) {
            // we are probably starting in the middle of a record
            // so read this one and discard, as the previous call
            // on the preceding chunk read this one already
            readUntilMatch(endTag, false);
        }
    }

    public boolean next(LongWritable key, Text value) throws IOException {                          

        if (!stillInChunk) return false;

        // status is true as long as we're still within the
        // chunk we got (i.e., fsin.getPos() < end). If we've
        // read beyond the chunk it will be false
        boolean status = readUntilMatch(endTag, true);

        value.set(buffer.getData(), 0, buffer.getLength());
        key = new LongWritable(fsin.getPos());
        buffer.reset();

        if (!status) {
            stillInChunk = false;
        }

        return true;
    }

    public LongWritable createKey() {
        return new LongWritable();
    }

    public Text createValue() {
        return new Text();
    }

    public long getPos() throws IOException {
        return fsin.getPos();
    }

    public void close() throws IOException {
        fsin.close();
    }

    public float getProgress() throws IOException {
        return ((float)getPos()) / totalLength;
    }

    private boolean readUntilMatch(byte[] match, boolean withinBlock) throws IOException {
        int i = 0;
        while (true) {
            int b = fsin.read();
            if (b == -1) return false;
            if (withinBlock) buffer.write(b);
            if (b == match[i]) {
                i++;
                if (i >= match.length) {
                    return fsin.getPos() < end;
                }
            } else i = 0;
        }
    }

}
