package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class DecorableOutputStream extends OutputStream {
    private final OutputStream inner;

    public DecorableOutputStream(OutputStream inner) {
        this.inner = requireNonNull(inner);
    }

    @Override
    public void write(int b) throws IOException {
        inner.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        inner.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        inner.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        inner.flush();
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }
}
