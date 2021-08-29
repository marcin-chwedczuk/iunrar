package pl.marcinchwedczuk.iunrar.gui.decompression;

public interface WorkerStatus {
    void updateMessage(MessageLevel level, String message);
    void updateProgress(double progress);

    boolean shouldStop();
    boolean shouldPause();
}
