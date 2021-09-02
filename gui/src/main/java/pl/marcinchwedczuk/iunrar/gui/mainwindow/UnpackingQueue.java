package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import javafx.scene.control.ListView;
import pl.marcinchwedczuk.iunrar.gui.unpackingqueue.UnpackingQueueItem;

public class UnpackingQueue {
    private UnpackingQueue() { }

    public static boolean hasAnyCancellableItems(ListView<UnpackingQueueItem> unpackingQueue) {
        for (UnpackingQueueItem item: unpackingQueue.getItems()) {
            if (isAlive(item)) {
                return true;
            }
        }
        return false;
    }

    public static void cancelAll(ListView<UnpackingQueueItem> unpackingQueue) {
        for (UnpackingQueueItem item : unpackingQueue.getItems()) {
            if (isAlive(item)) {
                item.cancel();
            }
        }
    }

    public static void pauseAll(ListView<UnpackingQueueItem> unpackingQueue) {
        for (UnpackingQueueItem item : unpackingQueue.getItems()) {
            if (isAlive(item)) {
                item.setPaused(true);
            }
        }
    }

    public static void resumeAll(ListView<UnpackingQueueItem> unpackingQueue) {
        for (UnpackingQueueItem item : unpackingQueue.getItems()) {
            if (isAlive(item)) {
                item.setPaused(false);
            }
        }
    }

    private static boolean isAlive(UnpackingQueueItem item) {
        return !item.isDone() && !item.isCancelled();
    }
}
