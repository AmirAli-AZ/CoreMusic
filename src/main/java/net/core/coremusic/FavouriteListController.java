package net.core.coremusic;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import net.core.coremusic.utils.Environment;
import net.core.coremusic.utils.FavouritesDBManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class FavouriteListController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private ListView<Item> listview;

    private final BooleanProperty refreshProperty = new SimpleBooleanProperty() {
        @Override
        public void set(boolean b) {
            super.set(b);

            if (b)
                Platform.runLater(() -> stop());
        }
    }, selectedProperty = new SimpleBooleanProperty();

    private final FavouritesDBManager favouritesDBManager = FavouritesDBManager.getInstance();

    private BorderPane borderPane;

    private PlayerController playerController;

    private VBox playerRoot;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listview.setCellFactory(itemListView -> {
            var favouriteCell = new FavouriteCell();

            favouriteCell.setOnMouseClicked(mouseEvent -> {
                if (!favouriteCell.isEmpty())
                    setPlayerVisible(mouseEvent.getButton() == MouseButton.PRIMARY);
            });

            return favouriteCell;
        });

        //refresh();
        watchDirs();
    }

    public void refresh() {
        var thread = new Thread(() -> {
            try {
                loadFavouriteMusics();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void loadFavouriteMusics() throws SQLException {
        if (!isSelected())
            return;

        setRefreshing(true);
        favouritesDBManager.init();
        Platform.runLater(() -> listview.getItems().clear());

        var statement = favouritesDBManager.getConnection().createStatement();
        var result = statement.executeQuery("select * from favourites;");

        while (result.next()) {
            var path = Paths.get(result.getString("path"));
            if (Files.notExists(path)) {
                favouritesDBManager.removeFromFavourites(path);
                continue;
            }

            Platform.runLater(() -> listview.getItems().add(new Item(path)));
        }

        setRefreshing(false);
    }

    private void watchDirs() {
        var watcher = DirectoryWatcher.getInstance();
        var appDataPath = Environment.getAppDataPath();
        var configManager = AppConfigManager.getInstance();
        var musicDirPath = configManager.getMusicDirPath();

        watcher.addListener((event, eventDir) -> {
            try {
                if (Files.isSameFile(eventDir, appDataPath)) {
                    var context = ((Path) event.context());

                    if (context.toString().equals(favouritesDBManager.getDbPath().getFileName().toString())) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY)
                            refresh();
                        else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
                            Platform.runLater(() -> listview.getItems().clear());
                    }
                } else if (musicDirPath.isPresent() && Files.isSameFile(eventDir, musicDirPath.get())) {
                    refresh();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
