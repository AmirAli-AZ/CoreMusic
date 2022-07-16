package net.core.coremusic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.core.coremusic.utils.AppConfigManager;
import net.core.coremusic.utils.DirectoryWatcher;
import net.core.coremusic.utils.Environment;
import net.core.coremusic.utils.Icons;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        var configManager = AppConfigManager.getInstance();
        if (configManager.getMusicDir().isEmpty()) {
            if (askMusicFolder())
                openApp(stage);
        }else {
            openApp(stage);
        }
        registerDirectories();
    }

    private void openApp(Stage stage) throws IOException {
        var configManager = AppConfigManager.getInstance();
        stage.setTitle("CoreMusic");
        var scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("app-view.fxml"))));
        configManager.setTheme(configManager.loadTheme(), scene);
        stage.setScene(scene);
        stage.setOnCloseRequest(windowEvent -> DirectoryWatcher.getInstance().interrupt());
        stage.show();
    }

    public static boolean askMusicFolder() {
        var stage = new Stage();
        var pathLabel = new Label();
        var pickBtn = new Button();
        var folderIcon = new SVGPath();
        var okBtn = new Button("OK");
        var dialogExit = new AtomicBoolean(false);
        var musicDir = new AtomicReference<File>();
        var configManager = AppConfigManager.getInstance();

        stage.setResizable(false);
        HBox.setHgrow(pathLabel, Priority.ALWAYS);
        pathLabel.setMaxWidth(Double.MAX_VALUE);
        pickBtn.setPrefSize(30, 30);
        pickBtn.setStyle("-fx-background-color: transparent;");
        folderIcon.setContent(Icons.FOLDER);
        pickBtn.setCursor(Cursor.HAND);
        pickBtn.setGraphic(folderIcon);

        pickBtn.setOnAction(actionEvent -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose music folder");
            musicDir.set(directoryChooser.showDialog(stage));
            if (musicDir.get() != null)
                pathLabel.setText(musicDir.get().getAbsolutePath());
        });
        okBtn.setPrefSize(75, 25);
        okBtn.setDefaultButton(true);
        okBtn.setOnAction(actionEvent -> {
            if (musicDir.get() != null) {
                try {
                    configManager.setMusicDir(musicDir.get());
                    dialogExit.set(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stage.close();
            }
        });

        var center = new HBox(3, new Label("Music Dir: "), pathLabel, pickBtn);
        center.setAlignment(Pos.TOP_LEFT);
        center.setPadding(new Insets(5));

        var bottom = new HBox(okBtn);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(5));

        var root = new BorderPane();
        root.setId("root");
        root.setCenter(center);
        root.setBottom(bottom);

        var scene = new Scene(root, 400, 200);
        configManager.setTheme(configManager.loadTheme(), scene);

        stage.setScene(scene);
        stage.showAndWait();

        return dialogExit.get();
    }

    public void registerDirectories() throws IOException {
        var watcher = DirectoryWatcher.getInstance();

        var configManager = AppConfigManager.getInstance();
        var musicDirPath = configManager.getMusicDirPath();
        var appDataPath = Environment.getAppDataPath();

        musicDirPath.ifPresent(path -> {
            try {
                watcher.register(
                        path,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        watcher.register(
                appDataPath,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
