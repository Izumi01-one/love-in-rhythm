package main.costomCursor;

import java.util.Objects;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.stage.Window;

public class GameCursor {

    private final Pane cursorLayer;
    private final Scene scene;
    private final Robot robot;

    private final ImageView cursorView;
    private final Image normalCursor;
    private final Image hoverCursor;

    private double hotSpotX = 0;
    private double hotSpotY = 0;

    private final String normalCursorPath = "/img/cursor/Sanrio Hello Kitty White Arrow--cursor.png";
    private final String hoverCursorPath = "/img/cursor/Sanrio Hello Kitty White Arrow--pointer.png";

    private boolean enabled = true;
    private boolean manualVisible = true;
    private boolean mouseInsideScene = false;
    private boolean windowFocused = true;

    private AnimationTimer autoHideTimer;

    public GameCursor(Pane cursorLayer, Scene scene) {
        this.cursorLayer = cursorLayer;
        this.scene = scene;
        this.robot = new Robot();

        normalCursor = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(normalCursorPath)
        ));

        hoverCursor = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(hoverCursorPath)
        ));

        cursorView = new ImageView(normalCursor);
        cursorView.setFitWidth(48);
        cursorView.setFitHeight(48);
        cursorView.setManaged(false);
        cursorView.setMouseTransparent(true);
        cursorView.setVisible(false);

        cursorLayer.setMouseTransparent(true);
        cursorLayer.setPickOnBounds(false);

        cursorLayer.getChildren().add(cursorView);
        cursorView.toFront();

        scene.setCursor(Cursor.NONE);

        initMouseEvents();
        initWindowFocusListener();
        startAutoHideChecker();
    }

    private void initMouseEvents() {
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            mouseInsideScene = true;
            updatePosition(e.getSceneX(), e.getSceneY());
        });

        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            mouseInsideScene = true;
            updatePosition(e.getSceneX(), e.getSceneY());
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            mouseInsideScene = true;
            updatePosition(e.getSceneX(), e.getSceneY());
        });

        scene.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            mouseInsideScene = true;
            updatePosition(e.getSceneX(), e.getSceneY());
        });

        scene.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            mouseInsideScene = false;
            updateVisibility();
        });
    }

    private void initWindowFocusListener() {
        scene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
            if (newWindow != null) {
                addWindowFocusListener(newWindow);
            }
        });

        if (scene.getWindow() != null) {
            addWindowFocusListener(scene.getWindow());
        }
    }

    private void addWindowFocusListener(Window window) {
        windowFocused = window.isFocused();

        window.focusedProperty().addListener((obs, oldFocused, focused) -> {
            windowFocused = focused;

            if (!focused) {
                mouseInsideScene = false;
            }

            updateVisibility();
        });
    }

    private void startAutoHideChecker() {
        autoHideTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                checkRealMousePosition();
            }
        };

        autoHideTimer.start();
    }

    private void checkRealMousePosition() {
        Point2D mouse = robot.getMousePosition();

        boolean inside = isRealMouseInsideCursorLayer(mouse);

        if (mouseInsideScene != inside) {
            mouseInsideScene = inside;
            updateVisibility();
        }

        if (enabled && manualVisible && mouseInsideScene && windowFocused) {
            updatePositionFromRealMouse(mouse);
        }
    }

    private boolean isRealMouseInsideCursorLayer(Point2D mouse) {
        try {
            Bounds bounds = cursorLayer.localToScreen(
                    cursorLayer.getBoundsInLocal()
            );

            if (bounds == null || mouse == null) {
                return false;
            }

            return mouse.getX() >= bounds.getMinX()
                    && mouse.getX() <= bounds.getMaxX()
                    && mouse.getY() >= bounds.getMinY()
                    && mouse.getY() <= bounds.getMaxY();

        } catch (Exception e) {
            return false;
        }
    }

    private void updatePositionFromRealMouse(Point2D mouse) {
        try {
            Bounds bounds = cursorLayer.localToScreen(
                    cursorLayer.getBoundsInLocal()
            );

            if (bounds == null || mouse == null) {
                return;
            }

            double sceneX = mouse.getX() - bounds.getMinX();
            double sceneY = mouse.getY() - bounds.getMinY();

            updatePosition(sceneX, sceneY);

        } catch (Exception ignored) {
        }
    }

    private void updatePosition(double sceneX, double sceneY) {
        if (!isInsideScene(sceneX, sceneY)) {
            mouseInsideScene = false;
            updateVisibility();
            return;
        }

        cursorView.relocate(sceneX - hotSpotX, sceneY - hotSpotY);
        cursorView.toFront();

        updateVisibility();
    }

    private boolean isInsideScene(double x, double y) {
        return x >= 0
                && y >= 0
                && x <= scene.getWidth()
                && y <= scene.getHeight();
    }

    private void updateVisibility() {
        boolean shouldShow =
                enabled
                        && manualVisible
                        && mouseInsideScene
                        && windowFocused;

        cursorView.setVisible(shouldShow);

        if (shouldShow) {
            scene.setCursor(Cursor.NONE);
        } else {
            scene.setCursor(Cursor.DEFAULT);
        }
    }

    public void setVisible(boolean visible) {
        this.manualVisible = visible;
        updateVisibility();
    }

    public boolean isVisible() {
        return cursorView.isVisible();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateVisibility();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setSize(double width, double height) {
        cursorView.setFitWidth(width);
        cursorView.setFitHeight(height);
    }

    public void setHotSpot(double x, double y) {
        this.hotSpotX = x;
        this.hotSpotY = y;
    }

    public void useNormalCursor() {
        cursorView.setImage(normalCursor);
    }

    public void useHoverCursor() {
        cursorView.setImage(hoverCursor);
    }

    public ImageView getCursorView() {
        return cursorView;
    }

    public void dispose() {
        if (autoHideTimer != null) {
            autoHideTimer.stop();
            autoHideTimer = null;
        }

        cursorView.setVisible(false);
        cursorLayer.getChildren().remove(cursorView);
        scene.setCursor(Cursor.DEFAULT);
    }
}
