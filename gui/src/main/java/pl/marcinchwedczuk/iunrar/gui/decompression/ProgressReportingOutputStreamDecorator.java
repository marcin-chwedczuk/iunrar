package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

public class ProgressReportingOutputStreamDecorator extends OutputStream {
    private final WorkerStatus workerStatus;
    private final long totalSize;
    private final OutputStream inner;
    private final AtomicLong alreadyUnpacked;

    public ProgressReportingOutputStreamDecorator(WorkerStatus workerStatus,
                                                  long totalSize,
                                                  AtomicLong alreadyUnpacked,
                                                  OutputStream inner) {
        this.workerStatus = requireNonNull(workerStatus);
        this.totalSize = totalSize;
        this.alreadyUnpacked = requireNonNull(alreadyUnpacked);
        this.inner = requireNonNull(inner);
    }

    @Override
    public void write(int b) throws IOException {
        inner.write(b);
        alreadyUnpacked.setPlain(alreadyUnpacked.getPlain() + 1);
        updateProgress();
    }

    @Override
    public void write(byte[] b) throws IOException {
        inner.write(b);
        alreadyUnpacked.setPlain(alreadyUnpacked.getPlain() + b.length);
        updateProgress();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        inner.write(b, off, len);
        alreadyUnpacked.setPlain(alreadyUnpacked.getPlain() + len);
        updateProgress();
    }

    @Override
    public void flush() throws IOException {
        inner.flush();
        updateProgress();
    }

    @Override
    public void close() throws IOException {
        inner.close();
        updateProgress();
    }

    private void updateProgress() {
        workerStatus.updateProgress(100.0 * alreadyUnpacked.getPlain() / totalSize);
    }
}
