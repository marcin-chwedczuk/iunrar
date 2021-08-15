package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;

public class ResizeRequestedEvent extends Event {
    public static final EventType<ResizeRequestedEvent> RESIZE_REQUESTED =
            new EventType<>(Event.ANY, "RESIZE_REQUESTED");

    public final double zoom;

    public ResizeRequestedEvent(double zoom) {
        super(RESIZE_REQUESTED);
        this.zoom = zoom;
    }
}
