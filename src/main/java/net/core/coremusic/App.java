package net.core.coremusic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
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

public final class App extends Application {

    private static App app;

    private Stage stage;

    private final AppConfigManager configManager = AppConfigManager.getInstance();

    public App() {
        app = this;
    }

    public static App getInstance() {
        return app;
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        syncWindowIcons();
        if (configManager.getMusicDir().isEmpty()) {
            if (askMusicFolder())
                openApp();
        }else {
            openApp();
        }
    }

    private void openApp() throws IOException, AWTException {
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

    public boolean askMusicFolder() {
        var dialog = new javafx.scene.control.Dialog<Boolean>();
        var pathLabel = new Label();
        var pickBtn = new Button();
        var folderIcon = new SVGPath();
        var musicDir = new SimpleObjectProperty<File>();

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
            var dir = directoryChooser.showDialog(stage);
            musicDir.set(dir);
            if (dir != null)
                pathLabel.setText(dir.getAbsolutePath());
        });

        var root = new HBox(3, new Label("Music Directory: "), pathLabel, pickBtn);
        root.setPrefSize(400, 150);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(5));

        dialog.getDialogPane().getStylesheets().add(configManager.loadTheme().getPath());
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK && musicDir.get() != null && musicDir.get().exists()) {
                try {
                    configManager.setMusicDir(musicDir.get());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return false;
        });

        return dialog.showAndWait().get();
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

        var trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/CoreMusicLogo64.png")), "CoreMusic", popupMenu);
        trayIcon.setImageAutoSize(true);
        SystemTray.getSystemTray().add(trayIcon);

        return true;
    }

    public void syncWindowIcons() {
        Window.getWindows().addListener((ListChangeListener<? super Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Window window : change.getAddedSubList()) {
                        if (window instanceof Stage addedStage)
                            addedStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/CoreMusicLogo64.png"))));
                    }
                }
            }
        });
    }


    public Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
