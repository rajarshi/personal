package net.guha.apps.rest.representations;

import org.restlet.resource.OutputRepresentation;
import org.restlet.data.MediaType;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A {@link org.restlet.resource.Representation} implementation to support binary data types.
 * <p/>
 * Use this for returning images and so on.
 *
 * @author Rajarshi Guha
 */
public class ByteRepresentation extends OutputRepresentation {
    byte[] bytes;

    public ByteRepresentation(byte[] bytes, MediaType mediaType) {
        super(mediaType);
        this.bytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
    }

    public ByteRepresentation(byte[] bytes, MediaType mediaType, long l) {
        super(mediaType, l);
        this.bytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
    }

    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
    }
}
