package main.play;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.costomCursor.GameCursor;
import main.createScene.GameSceneFactory;
import main.model.Album;
import main.model.Song;
import main.setting.SettingMenu;

public class PianoGameApp {


    private static final double APP_HEIGHT = 800;
    SettingMenu settingMenu = new SettingMenu();

    @SuppressWarnings("unused")
    private GameCursor gameCursor;

    public Scene createScene(Stage stage, Album album, Song song, int level) {
        String video = song.getVideoPath();
        String id = song.getId();
        String note = settingMenu.getIconPath();

        PlayPane root = new PlayPane(stage, album, song, video, note, level, id);

        root.setPrefSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMinSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setMaxSize(GameSceneFactory.APP_WIDTH, GameSceneFactory.APP_HEIGHT);
        root.setFocusTraversable(true);

        GameCursor[] holder = new GameCursor[1];

        Scene scene = GameSceneFactory.createScene(root, holder);

        gameCursor = holder[0];

        root.setGameCursor(gameCursor);

        Platform.runLater(root::requestFocus);

        return scene;


    }

}