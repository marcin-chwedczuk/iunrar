package pl.marcinchwedczuk.iunrar.gui.decompression;

import static java.util.Objects.requireNonNull;

public class ThrottledWorkerStatus implements WorkerStatus {
    private final WorkerStatus inner;
    private double persistentProgress;

    public ThrottledWorkerStatus(WorkerStatus inner, double initialProgress) {
        this.inner = requireNonNull(inner);
        this.persistentProgress = initialProgress;
    }

    @Override
    public void updateMessage(String message) {
        inner.updateMessage(message);
    }

    @Override
    public void updateProgress(double newProgress) {
        if (Math.abs(newProgress - persistentProgress) >= 1.0) {
            inner.updateProgress(newProgress);
            persistentProgress = newProgress;
        }
    }

    @Override
    public boolean shouldStop() {
        return inner.shouldStop();
    }

    @Override
    public boolean shouldPause() {
        return inner.shouldPause();
    }
}
