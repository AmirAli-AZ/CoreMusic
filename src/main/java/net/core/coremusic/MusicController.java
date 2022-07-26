package net.core.coremusic;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.core.coremusic.model.Item;
import net.core.coremusic.utils.AppConfigManager;
import net.core.coremusic.utils.DirectoryWatcher;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.ResourceBundle;

public class MusicController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private ListView<Item> listview;

    private PlayerController playerController;

    private final BooleanProperty refreshProperty = new SimpleBooleanProperty() {
        @Override
        public void set(boolean b) {
            super.set(b);

            if (b)
                Platform.runLater(() -> stop());
        }
    }, selectedProperty = new SimpleBooleanProperty();

    private BorderPane borderPane;

    private VBox playerRoot;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listview.setCellFactory(musicCellListView -> {
            var musicCell = new MusicCell();
            musicCell.setOnMouseClicked(mouseEvent -> {
                if (!musicCell.isEmpty())
                    setPlayerVisible(mouseEvent.getButton() == MouseButton.PRIMARY);
            });

            return musicCell;
        });

        refresh();
        watchDirs();
    }

    public void refresh() {
        if (isRefreshing() || !isSelected())
            return;

        var configManager = AppConfigManager.getInstance();
        var musicDir = configManager.getMusicDir();

        if (musicDir.isEmpty())
            return;
        if (!musicDir.get().exists())
            return;

        setRefreshing(true);
        Platform.runLater(() -> listview.getItems().clear());

        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var object = new Object();

                for (File file : Objects.requireNonNull(musicDir.get().listFiles())) {
                    if (checkExtension(file, ".mp3") || checkExtension(file, ".wav")) {
                        var media = new Media(file.toURI().toString());
                        var mediaPlayer = new MediaPlayer(media);

                        mediaPlayer.setOnReady(() -> {
                            var image = Objects.requireNonNullElseGet(((Image) media.getMetadata().get("image")), () -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/CoreMusicLogo64.png"))));
                            var musicTitle = Objects.requireNonNullElseGet(((String) media.getMetadata().get("title")), () -> new File(media.getSource()).getName());

                            Platform.runLater(() -> listview.getItems().add(new Item(musicTitle, image, file.toPath())));

                            synchronized (object) {
                                object.notify();
                            }
                        });

                        synchronized (object) {
                            object.wait();
                        }
                    }
                }

                return null;
            }

            @Override
            protected void succeeded() {
                setRefreshing(false);
            }

            @Override
            protected void failed() {
                setRefreshing(false);
            }

            private boolean checkExtension(File file, String extension) {
                if (file.isDirectory())
                    return false;
                var filename = file.getName();
                var lastIndex = filename.lastIndexOf('.');
                if (lastIndex == -1)
                    return false;
                return filename.substring(lastIndex).equalsIgnoreCase(extension);
            }
        };
        var thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void setPlayerVisible(boolean visible) {
        if (borderPane == null)
            return;

        if (playerController == null) {
            try {
                var loader = new FXMLLoader(getClass().getResource("player-view.fxml"));
                playerRoot = loader.load();
                playerController = loader.getController();
                playerController.setRootController(this);
                playerController.setListview(listview);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (visible || borderPane.getBottom() == null) {
            if (!playerController.isPlaying() && !isRefreshing()) {
                playerController.initPlayer(listview.getSelectionModel().getSelectedItem());
                borderPane.setBottom(playerRoot);
            }
        }else {
            stop();
        }
    }

    public void setBorderPane(@NotNull BorderPane borderPane) {
        this.borderPane = borderPane;
    }

    public void stop() {
        if (playerController == null || borderPane == null)
            return;
        playerController.stop();
        if (isSelected())
            borderPane.setBottom(null);
        listview.getSelectionModel().clearSelection();
    }

    private void watchDirs() {
        var watcher = DirectoryWatcher.getInstance();
        var configManager = AppConfigManager.getInstance();
        var musicDirPath = configManager.getMusicDirPath();

        watcher.addListener((event, eventDir) -> {
            try {
                if (musicDirPath.isPresent() && Files.isSameFile(musicDirPath.get(), eventDir))
                    refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setSelected(boolean selected) {
        selectedProperty.set(selected);
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public void setRefreshing(boolean refreshing) {
        refreshProperty.set(refreshing);
    }

    public boolean isRefreshing() {
        return refreshProperty.get();
    }
}
