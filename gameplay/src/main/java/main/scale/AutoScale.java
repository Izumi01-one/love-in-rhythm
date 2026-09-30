package main.scale;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class AutoScale {
    public double calculateScale(double APP_WIDTH, double APP_HEIGHT) {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        double maxWidth = visualBounds.getWidth() * 0.95;
        double maxHeight = visualBounds.getHeight() * 0.90;

        double scaleX = maxWidth / APP_WIDTH;
        double scaleY = maxHeight / APP_HEIGHT;

        return Math.min(1.0, Math.min(scaleX, scaleY));
    }
}
