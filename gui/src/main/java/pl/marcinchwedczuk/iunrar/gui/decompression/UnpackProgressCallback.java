package pl.marcinchwedczuk.iunrar.gui.decompression;

public interface UnpackProgressCallback {
    /**
     * @param message
     *  Message describing progress e.g. unpacking foo.java
     * @param progress
     *  Progress in percents (0.0 - 100.0)
     */
    void update(String message, double progress);
}
