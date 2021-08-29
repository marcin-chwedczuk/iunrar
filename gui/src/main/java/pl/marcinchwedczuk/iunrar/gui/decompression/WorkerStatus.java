package pl.marcinchwedczuk.iunrar.gui.decompression;

public interface WorkerStatus {
    void updateProgress(String message);
    /**
     * @param progress
     *  Progress in percents (0.0 - 100.0)
     */
    void updateProgress(String message, double progress);

    boolean shouldStop();
    boolean shouldPause();
}
