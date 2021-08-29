package pl.marcinchwedczuk.iunrar.gui.decompression;

import static java.util.Objects.requireNonNull;
import static pl.marcinchwedczuk.iunrar.gui.decompression.MessageLevel.IMPORTANT;

public class ThrottledWorkerStatus implements WorkerStatus {
    private final WorkerStatus inner;
    private double persistentProgress;
    private long messageUpdateLastTime = System.currentTimeMillis();

    public ThrottledWorkerStatus(WorkerStatus inner) {
        this.inner = requireNonNull(inner);
        this.persistentProgress = 0.0;
    }

    @Override
    public void updateMessage(MessageLevel messageLevel, String message) {
        long currentTime = System.currentTimeMillis();
        if (messageLevel == IMPORTANT || Math.abs(messageUpdateLastTime - currentTime) > 1000) {
            inner.updateMessage(messageLevel, message);
        }
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
