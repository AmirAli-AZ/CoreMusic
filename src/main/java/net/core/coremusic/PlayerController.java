package net.core.coremusic;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import net.core.coremusic.model.Item;
import net.core.coremusic.utils.DirectoryWatcher;
import net.core.coremusic.utils.Environment;
import net.core.coremusic.utils.FavouritesDBManager;
import net.core.coremusic.utils.Icons;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class PlayerController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private Button rewindBtn, playBtn, forwardBtn, favouriteBtn;

    @FXML
    private SVGPath rewindSvgPath, playSvgPath, forwardSvgPath, favouriteSvgPath;

    @FXML
    private Label currentTimeLabel, totalTimeLabel;

    @FXML
    private Slider slider;

    private MediaPlayer player;

    private final BooleanProperty playingProperty = new SimpleBooleanProperty() , slidingProperty = new SimpleBooleanProperty();

    private ListView<Item> listView;

    private final FavouritesDBManager favouriteDBManager = FavouritesDBManager.getInstance();

    private Item item;

    private Object rootController;

    @FXML
    public void forward(ActionEvent event) {
        if (listView == null || player == null)
            return;

        var size = listView.getItems().size();

        if (size == 1) {
            player.seek(player.getMedia().getDuration());
            playSvgPath.setContent(Icons.PLAY);
            playingProperty.set(false);

            return;
        }

        var selectedIndex = listView.getSelectionModel().getSelectedIndex();

        player.stop();
        playingProperty.set(false);

        if (selectedIndex < listView.getItems().size() - 1) {
            initPlayer(listView.getItems().get(selectedIndex + 1));
            listView.getSelectionModel().select(selectedIndex + 1);
        }else {
            initPlayer(listView.getItems().get(0));
            listView.getSelectionModel().select(0);
        }
    }

    @FXML
    public void play(ActionEvent event) {
        if (player == null)
            return;

        if (playingProperty.get()) {
            player.pause();
            playSvgPath.setContent(Icons.PLAY);
            playingProperty.set(false);
        }else {
            player.play();
            playSvgPath.setContent(Icons.PAUSE);
            playingProperty.set(true);
        }
    }

    @FXML
    public void rewind(ActionEvent event) {
        if (listView == null || player == null)
            return;

        var size = listView.getItems().size();

        if (size == 1) {
            player.seek(Duration.ZERO);
            if (!playingProperty.get()) {
                player.play();
                playingProperty.set(true);
            }
            playSvgPath.setContent(Icons.PAUSE);

            return;
        }

        var selectedIndex = listView.getSelectionModel().getSelectedIndex();

        player.stop();
        playingProperty.set(false);

        if (selectedIndex > 0) {
            initPlayer(listView.getItems().get(selectedIndex - 1));
            listView.getSelectionModel().select(selectedIndex - 1);
        }else {
            initPlayer(listView.getItems().get(size - 1));
            listView.getSelectionModel().select(size - 1);
        }
    }

    @FXML
    public void sliderPressed(MouseEvent event) {
        if (player == null)
            return;

        slidingProperty.set(true);
    }

    @FXML
    public void sliderReleased(MouseEvent event) {
        if (player == null)
            return;

        player.seek(Duration.seconds(slider.getValue()));
        slidingProperty.set(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var watcher = DirectoryWatcher.getInstance();
        var appDataPath = Environment.getAppDataPath();

        if (Files.exists(appDataPath)) {
            watcher.addCallBack((event, eventDir) -> {
                try {
                    if (Files.isSameFile(eventDir, appDataPath)) {
                        var context = ((Path) event.context());

                        if (context.toString().equals("Favourites.db") && event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            Platform.runLater(() -> {
                                favouriteBtn.setDisable(true);
                                favouriteBtn.setGraphic(null);
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @FXML
    public void addToFavourites() {
        if (item == null)
            return;

        if (rootController instanceof FavouriteListController controller) {
            controller.setRefreshing(true);
            favouriteDBManager.removeFromFavourites(item);
            listView.getItems().remove(item);
            favouriteSvgPath.setContent(Icons.FAVOURITE_BORDER);
            controller.setRefreshing(false);
        }else if (rootController instanceof MusicController) {
            if (favouriteDBManager.isAdded(item)) {
                favouriteDBManager.removeFromFavourites(item);
                favouriteSvgPath.setContent(Icons.FAVOURITE_BORDER);
            } else {
                favouriteDBManager.addToFavourites(item.title(), item.path());
                favouriteSvgPath.setContent(Icons.FAVOURITE);
            }
        }
    }

    public void initPlayer(@NotNull Item item) {
        this.item = item;

        var media = new Media(item.path().toUri().toString());
        player = new MediaPlayer(media);
        player.setOnReady(() -> {
            slider.setMax(media.getDuration().toSeconds());
            totalTimeLabel.setText(formatDuration(media.getDuration()));
            player.play();
            playSvgPath.setContent(Icons.PAUSE);
            playingProperty.set(true);
        });
        player.setOnEndOfMedia(() -> playingProperty.set(false));

        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> currentTimeLabel.setText(formatDuration(media.getDuration(), Duration.seconds(newValue.doubleValue()))));
        player.currentTimeProperty().addListener((observableValue, duration, currentDuration) -> {
            if (!slidingProperty.get())
                slider.setValue(currentDuration.toSeconds());
        });

        if (favouriteDBManager.isAdded(item))
            favouriteSvgPath.setContent(Icons.FAVOURITE);
        else
            favouriteSvgPath.setContent(Icons.FAVOURITE_BORDER);
    }

    private String formatDuration(@NotNull Duration duration, @NotNull Duration currentDuration) {
        var pattern = "mm:ss";

        if (duration.toMinutes() >= 60)
            pattern = "hh:mm:ss";

        return new SimpleDateFormat(pattern).format(new Date(((long) currentDuration.toMillis())));
    }

    private String formatDuration(@NotNull Duration duration) {
        return formatDuration(duration, duration);
    }

    public void stop() {
        if (player == null)
            return;

        if (playingProperty.get()) {
            player.stop();
            playingProperty.set(false);
        }
        slider.setValue(0);
    }

    public boolean isPlaying() {
        return !playingProperty.get();
    }

    public void setListview(@NotNull ListView<Item> listview) {
        this.listView = listview;
    }

    public void setRootController(@NotNull Object rootController) {
        this.rootController = rootController;
    }
}
