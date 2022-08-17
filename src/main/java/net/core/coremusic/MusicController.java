package net.core.coremusic;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
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

    private final AppConfigManager configManager = AppConfigManager.getInstance();


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
        var thread = new Thread(this::loadMusics);
        thread.setDaemon(true);
        thread.start();
    }

    private synchronized void loadMusics() {
        if (!isSelected())
            return;

        var musicDir = configManager.getMusicDir();

        if (musicDir.isEmpty())
            return;
        if (Files.notExists(musicDir.get()))
            return;

        setRefreshing(true);
        Platform.runLater(() -> listview.getItems().clear());
        for (File file : Objects.requireNonNull(musicDir.get().toFile().listFiles())) {
            if (isPlayable(file))
                Platform.runLater(() -> listview.getItems().add(new Item(file.toPath())));
        }
        setRefreshing(false);
    }

    private boolean isPlayable(File file) {
        if (!file.exists() || file.isDirectory())
            return false;

        try {
            new Media(file.toURI().toString());
            return true;
        }catch (MediaException e) {
            return false;
        }
    }

    public void setPlayerVisible(boolean visible) {
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
        } else {
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
        var musicDirPath = configManager.getMusicDir();

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
