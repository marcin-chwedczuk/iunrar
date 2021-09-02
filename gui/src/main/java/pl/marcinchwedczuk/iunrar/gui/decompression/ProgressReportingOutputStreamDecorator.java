package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

public class ProgressReportingOutputStreamDecorator extends OutputStream {
    private final OutputStream inner;

    private final WorkerStatus workerStatus;
    private final long archiveTotalUnpackedSize;
    private final AtomicLong currentlyUnpackedSize;

    public ProgressReportingOutputStreamDecorator(WorkerStatus workerStatus,
                                                  long archiveTotalUnpackedSize,
                                                  AtomicLong currentlyUnpackedSize,
                                                  OutputStream inner) {
        this.workerStatus = requireNonNull(workerStatus);
        this.archiveTotalUnpackedSize = archiveTotalUnpackedSize;
        this.currentlyUnpackedSize = requireNonNull(currentlyUnpackedSize);
        this.inner = requireNonNull(inner);
    }

    @Override
    public void write(int b) throws IOException {
        inner.write(b);
        currentlyUnpackedSize.setPlain(currentlyUnpackedSize.getPlain() + 1);
        updateProgress();
    }

    @Override
    public void write(byte[] b) throws IOException {
        inner.write(b);
        currentlyUnpackedSize.setPlain(currentlyUnpackedSize.getPlain() + b.length);
        updateProgress();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        inner.write(b, off, len);
        currentlyUnpackedSize.setPlain(currentlyUnpackedSize.getPlain() + len);
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
        workerStatus.updateProgressPercent(100.0 * currentlyUnpackedSize.getPlain() / archiveTotalUnpackedSize);
    }
}
