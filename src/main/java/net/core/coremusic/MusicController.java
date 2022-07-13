package net.core.coremusic;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.core.coremusic.model.Item;
import net.core.coremusic.utils.AppConfigManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MusicController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private ListView<Item> listview;

    private PlayerController playerController;

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

        loadMusic();
    }

    private void loadMusic() {
        var configManager = AppConfigManager.getInstance();
        var musicDir = configManager.getMusicDir();
        if (musicDir.isEmpty())
            return;
        if (!musicDir.get().exists())
            return;

        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var object = new Object();

                for (File file : Objects.requireNonNull(musicDir.get().listFiles())) {
                    if (checkExtension(file, ".mp3") || checkExtension(file, ".wav")) {
                        var media = new Media(file.toURI().toString());
                        var mediaPlayer = new MediaPlayer(media);

                        mediaPlayer.setOnReady(() -> {
                            Platform.runLater(() -> listview.getItems().add(new Item(media)));

                            synchronized (object) {
                                object.notify();
                            }
                        });

                        synchronized (object) {
                            object.wait();
                        }
                    }
                }
                System.gc();

                return null;
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
        thread.start();
    }

    public void setPlayerVisible(boolean visible) {
        var appRoot = ((BorderPane) root.getScene().getRoot());

        if (playerController == null) {
            try {
                var loader = new FXMLLoader(getClass().getResource("player-view.fxml"));
                loader.load();
                playerController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (visible || appRoot.getBottom() == null) {
            if (!playerController.playingProperty().get()) {
                playerController.setItem(listview.getSelectionModel().getSelectedItem());
                appRoot.setBottom(playerController.getRoot());
            }
        }else {
            playerController.stop();
            appRoot.setBottom(null);
            listview.getSelectionModel().clearSelection();
        }
    }
}
