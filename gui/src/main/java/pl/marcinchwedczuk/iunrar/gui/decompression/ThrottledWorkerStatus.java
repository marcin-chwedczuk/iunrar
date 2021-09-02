package pl.marcinchwedczuk.iunrar.gui.decompression;

import static java.util.Objects.requireNonNull;
import static pl.marcinchwedczuk.iunrar.gui.decompression.MessageLevel.IMPORTANT;

public class ThrottledWorkerStatus implements WorkerStatus {
    private final WorkerStatus inner;

    private double prevProgress;
    private long prevMessageTime;

    public ThrottledWorkerStatus(WorkerStatus inner) {
        this.inner = requireNonNull(inner);

        this.prevProgress = -1;
        this.prevMessageTime = System.currentTimeMillis();
    }

    @Override
    public void updateMessage(MessageLevel messageLevel, String message) {
        long currentTime = System.currentTimeMillis();
        if (messageLevel == IMPORTANT ||
                Math.abs(prevMessageTime - currentTime) > 1000)
        {
            inner.updateMessage(messageLevel, message);
            prevMessageTime = currentTime;
        }
    }

    @Override
    public void updateProgressPercent(double newProgress) {
        if (Math.abs(newProgress - prevProgress) >= 1.0) {
            inner.updateProgressPercent(newProgress);
            prevProgress = newProgress;
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
