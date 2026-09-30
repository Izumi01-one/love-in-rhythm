package main.createScene;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

import main.costomCursor.GameCursor;
import main.scale.AutoScale;

public class GameSceneFactory {

    public static final double APP_WIDTH = 1400;
    public static final double APP_HEIGHT = 800;

    public static Scene createScene(StackPane contentRoot, GameCursor[] cursorHolder) {
        /*
         * Tính scale để game vừa với màn hình.
         */
        AutoScale autoScale = new AutoScale();

        double scale = autoScale.calculateScale(APP_WIDTH, APP_HEIGHT);

        /*
         * Nếu không muốn màn hình lớn phóng to quá kích thước gốc 1400x800,
         * giữ dòng này.
         *
         * Nếu muốn cho phép phóng to theo màn hình lớn, xóa hoặc comment dòng này.
         */
        scale = Math.min(scale, 1.0);

        double sceneWidth = APP_WIDTH * scale;
        double sceneHeight = APP_HEIGHT * scale;

        /*
         * sceneRoot là root thật của Scene.
         * contentRoot là giao diện game.
         * cursorLayer là lớp cursor riêng, không bị scale.
         */
        StackPane sceneRoot = new StackPane();

        sceneRoot.setPrefSize(sceneWidth, sceneHeight);
        sceneRoot.setMinSize(sceneWidth, sceneHeight);
        sceneRoot.setMaxSize(sceneWidth, sceneHeight);

        /*
         * contentRoot vẫn giữ kích thước gốc 1400x800.
         */
        contentRoot.setPrefSize(APP_WIDTH, APP_HEIGHT);
        contentRoot.setMinSize(APP_WIDTH, APP_HEIGHT);
        contentRoot.setMaxSize(APP_WIDTH, APP_HEIGHT);

        /*
         * Chỉ scale contentRoot, không scale cursor.
         */
        contentRoot.getTransforms().clear();
        contentRoot.getTransforms().add(new Scale(scale, scale, 0, 0));

        Group scaledContentRoot = new Group(contentRoot);

        /*
         * Layer cursor dùng kích thước thật của Scene sau khi scale.
         */
        Pane cursorLayer = new Pane();

        cursorLayer.setPrefSize(sceneWidth, sceneHeight);
        cursorLayer.setMinSize(sceneWidth, sceneHeight);
        cursorLayer.setMaxSize(sceneWidth, sceneHeight);

        /*
         * Quan trọng:
         * - mouseTransparent: không chặn click game
         * - pickOnBounds false: không bắt event vùng trống
         * - managed false: không tham gia layout StackPane
         */
        cursorLayer.setMouseTransparent(true);
        cursorLayer.setPickOnBounds(false);
        cursorLayer.setManaged(false);

        sceneRoot.getChildren().addAll(
                scaledContentRoot,
                cursorLayer
        );

        StackPane.setAlignment(scaledContentRoot, Pos.TOP_LEFT);
        StackPane.setAlignment(cursorLayer, Pos.TOP_LEFT);

        Scene scene = new Scene(sceneRoot, sceneWidth, sceneHeight);

        /*
         * Cursor layer luôn bằng kích thước Scene thật.
         */
        cursorLayer.prefWidthProperty().bind(scene.widthProperty());
        cursorLayer.prefHeightProperty().bind(scene.heightProperty());

        cursorLayer.minWidthProperty().bind(scene.widthProperty());
        cursorLayer.minHeightProperty().bind(scene.heightProperty());

        cursorLayer.maxWidthProperty().bind(scene.widthProperty());
        cursorLayer.maxHeightProperty().bind(scene.heightProperty());

        GameCursor cursor = new GameCursor(cursorLayer, scene);

        /*
         * Nếu cần chỉnh kích thước cursor thì để ở đây.
         */
        cursor.setSize(48, 48);

        /*
         * Nếu đầu chuột bị lệch, chỉnh hotspot ở đây.
         */
        cursor.setHotSpot(0, 0);

        if (cursorHolder != null && cursorHolder.length > 0) {
            cursorHolder[0] = cursor;
        }

        return scene;
    }
}