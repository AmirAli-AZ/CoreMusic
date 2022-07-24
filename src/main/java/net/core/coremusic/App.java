package net.core.coremusic;

import javafx.application.Application;
import javafx.application.Platform;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class App extends Application {

    private Stage stage;

    private static final AppConfigManager configManager = AppConfigManager.getInstance();

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        if (configManager.getMusicDir().isEmpty()) {
            if (askMusicFolder())
                openApp();
        } else {
            openApp();
        }
    }

    private void openApp() throws IOException, AWTException {
        var configManager = AppConfigManager.getInstance();
        stage.setTitle("CoreMusic");
        var scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("app-view.fxml"))));
        configManager.setTheme(configManager.loadTheme(), scene);
        stage.setScene(scene);
        stage.show();

        registerDirectories();
        var success = createTrayIcon();

        if (!success)
            stage.setOnCloseRequest(windowEvent -> DirectoryWatcher.getInstance().interrupt());
    }

    public static boolean askMusicFolder() {
        var stage = new Stage();
        var pathLabel = new Label();
        var pickBtn = new Button();
        var folderIcon = new SVGPath();
        var okBtn = new Button("OK");
        var dialogExit = new AtomicBoolean(false);
        var musicDir = new AtomicReference<File>();

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

    private boolean createTrayIcon() throws AWTException {
        if (!SystemTray.isSupported())
            return false;

        Platform.setImplicitExit(false);

        var popupMenu = new PopupMenu();
        var exitItem = new MenuItem("Exit Application");
        exitItem.addActionListener(e -> {
            DirectoryWatcher.getInstance().interrupt();
            Platform.exit();
            System.exit(0);
        });
        var openItem = new MenuItem("Open CoreMusic");
        openItem.addActionListener(e -> Platform.runLater(() -> {
            if (!stage.isShowing())
                stage.show();
        }));
        popupMenu.add(openItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        var trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/music-icon.png")), "CoreMusic", popupMenu);
        trayIcon.setImageAutoSize(true);
        SystemTray.getSystemTray().add(trayIcon);

        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
