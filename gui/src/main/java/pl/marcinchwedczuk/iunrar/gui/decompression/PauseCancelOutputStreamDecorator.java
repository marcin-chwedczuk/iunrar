package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.IOException;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

public class PauseCancelOutputStreamDecorator extends OutputStream {
    private static final long CHECK_EVERY_BYTE = 64*1024;

    private final WorkerStatus workerStatus;
    private final OutputStream inner;
    private long bytesWritten = 0;

    public PauseCancelOutputStreamDecorator(WorkerStatus workerStatus, OutputStream inner) {
        this.workerStatus = requireNonNull(workerStatus);
        this.inner = requireNonNull(inner);
    }

    @Override
    public void write(int b) throws IOException {
        inner.write(b);
        bytesWritten++;
        checkForStopAndPause();
    }

    @Override
    public void write(byte[] b) throws IOException {
        inner.write(b);
        bytesWritten += b.length;
        checkForStopAndPause();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        inner.write(b, off, len);
        bytesWritten += len;
        checkForStopAndPause();
    }

    @Override
    public void flush() throws IOException {
        inner.flush();
    }

    @Override
    public void close() throws IOException {
        inner.close();

        // Always check after file was saved
        checkForStopAndPause();
    }

    private void checkForStopAndPause() {
        if (bytesWritten < CHECK_EVERY_BYTE) {
            return;
        }
        bytesWritten = 0;

        while (workerStatus.shouldPause() && !workerStatus.shouldStop()) {
            try {
                workerStatus.updateMessage("Paused");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        if (workerStatus.shouldStop()) {
            throw new StopCompressionException();
        }
    }
}
