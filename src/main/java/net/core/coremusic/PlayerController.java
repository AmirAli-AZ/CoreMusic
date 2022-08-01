package net.core.coremusic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import net.core.coremusic.model.Item;
import net.core.coremusic.utils.AppConfigManager;
import net.core.coremusic.utils.FavouritesDBManager;
import net.core.coremusic.utils.Icons;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class PlayerController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private Button rewindBtn, playBtn, forwardBtn, favouriteBtn, repeatBtn, volumeBtn;

    @FXML
    private SVGPath rewindSvgPath, playSvgPath, forwardSvgPath, favouriteSvgPath, repeatSvgPath, volumeSvgPath;

    @FXML
    private Label currentTimeLabel, totalTimeLabel;

    @FXML
    private Slider slider, volumeSlider;

    private MediaPlayer player;

    private Media media;

    private final BooleanProperty
            playingProperty = new SimpleBooleanProperty(),
            slidingProperty = new SimpleBooleanProperty(),
            repeatProperty = new SimpleBooleanProperty();

    private ListView<Item> listView;

    private final FavouritesDBManager favouriteDBManager = FavouritesDBManager.getInstance();

    private Item item;

    private Object rootController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (media != null)
                currentTimeLabel.setText(formatDuration(media.getDuration(), Duration.seconds(newValue.doubleValue())));
        });

        volumeSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (player != null) {
                var doubleValue = newValue.doubleValue();

                player.setVolume(doubleValue * 0.01);

                if (doubleValue == 0)
                    volumeSvgPath.setContent(Icons.VOLUME_OFF);
                else if (doubleValue <= 50)
                    volumeSvgPath.setContent(Icons.VOLUME_DOWN);
                else
                    volumeSvgPath.setContent(Icons.VOLUME_UP);
            }
        });
    }


    @FXML
    public void play(ActionEvent event) {
        if (player == null)
            return;

        if (player.getCurrentTime().equals(player.getTotalDuration())) {
            player.seek(Duration.ZERO);
            setPlaying(true);
            playSvgPath.setContent(Icons.PAUSE);

            return;
        }

        if (playingProperty.get()) {
            player.pause();
            setPlaying(false);
            playSvgPath.setContent(Icons.PLAY);
        }else {
            player.play();
            setPlaying(true);
            playSvgPath.setContent(Icons.PAUSE);
        }
    }

    @FXML
    public void rewind(ActionEvent event) {
        if (player == null)
            return;
        if (listView.getItems().isEmpty())
            return;

        var size = listView.getItems().size();

        if (size == 1) {
            player.seek(Duration.ZERO);
            if (!isPlaying()) {
                player.play();
                setPlaying(true);
            }
            playSvgPath.setContent(Icons.PAUSE);

            return;
        }

        var selectedIndex = listView.getSelectionModel().getSelectedIndex();

        player.stop();
        setPlaying(false);

        if (selectedIndex > 0) {
            initPlayer(listView.getItems().get(selectedIndex - 1));
            listView.getSelectionModel().select(selectedIndex - 1);
        }else {
            initPlayer(listView.getItems().get(size - 1));
            listView.getSelectionModel().select(size - 1);
        }
    }

    @FXML
    public void forward(ActionEvent event) {
        if (player == null)
            return;
        if (listView.getItems().isEmpty())
            return;

        setPlaying(false);
        var size = listView.getItems().size();

        if (size == 1) {
            player.seek(player.getMedia().getDuration());
            playSvgPath.setContent(Icons.PLAY);

            return;
        }

        var selectedIndex = listView.getSelectionModel().getSelectedIndex();

        player.stop();

        if (selectedIndex < listView.getItems().size() - 1) {
            initPlayer(listView.getItems().get(selectedIndex + 1));
            listView.getSelectionModel().select(selectedIndex + 1);
        }else {
            initPlayer(listView.getItems().get(0));
            listView.getSelectionModel().select(0);
        }
    }

    @FXML
    public void sliderPressed(MouseEvent event) {
        if (player == null)
            return;
        setSliding(true);
    }

    @FXML
    public void sliderReleased(MouseEvent event) {
        if (player == null)
            return;
        player.seek(Duration.seconds(slider.getValue()));
        if (!isPlaying()) {
            player.play();
            setPlaying(true);
            playSvgPath.setContent(Icons.PAUSE);
        }
        setSliding(false);
    }

    @FXML
    public void addToFavourites() {
        try {
            favouriteDBManager.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (rootController instanceof FavouriteListController controller) {
            controller.setRefreshing(true);
            favouriteDBManager.removeFromFavourites(item);
            listView.getItems().remove(item);
            favouriteSvgPath.setContent(Icons.FAVOURITE_BORDER);
            controller.setRefreshing(false);
        }else if (rootController instanceof MusicController) {
            if (item.getTitle() != null) {
                if (favouriteDBManager.isAdded(item)) {
                    favouriteDBManager.removeFromFavourites(item);
                    favouriteSvgPath.setContent(Icons.FAVOURITE_BORDER);
                } else {
                    favouriteDBManager.addToFavourites(item.getTitle(), item.getPath());
                    favouriteSvgPath.setContent(Icons.FAVOURITE);
                }
            }
        }
    }

    @FXML
    public void repeat(ActionEvent actionEvent) {
        if (player == null)
            return;

        if (isRepeat()) {
            setRepeat(false);
            repeatSvgPath.setContent(Icons.REPEAT_OFF);
        }else {
            setRepeat(true);
            repeatSvgPath.setContent(Icons.REPEAT_ON);
        }
    }

    @FXML
    public void volumeBtnAction(ActionEvent actionEvent) {
        var volumeSliderValue = volumeSlider.getValue();

        if (volumeSliderValue == 0)
            volumeSlider.setValue(volumeSlider.getMax() / 2);
        else if (volumeSliderValue <= 50)
            volumeSlider.setValue(volumeSlider.getMax());
        else
            volumeSlider.setValue(volumeSlider.getMin());
    }

    public void initPlayer(@NotNull Item item) {
        this.item = item;

        media = new Media(item.getPath().toUri().toString());
        player = new MediaPlayer(media);
        player.setOnError(() -> showErrorDialog(player.getError()));
        player.setOnReady(() -> {
            slider.setMax(media.getDuration().toSeconds());
            totalTimeLabel.setText(formatDuration(media.getDuration()));
            player.setVolume(volumeSlider.getValue() * 0.01);
            player.play();
            setPlaying(true);
            playSvgPath.setContent(Icons.PAUSE);
        });
        player.setOnEndOfMedia(() -> {
            if (isRepeat()) {
                player.seek(Duration.ZERO);
            }else {
                setPlaying(false);
                playSvgPath.setContent(Icons.PLAY);
            }
        });
        player.currentTimeProperty().addListener((observableValue, duration, currentDuration) -> {
            if (!isSliding())
                slider.setValue(currentDuration.toSeconds());
        });

        try {
            favouriteDBManager.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        if (isPlaying()) {
            player.stop();
            setPlaying(false);
        }
        player.dispose();
        slider.setValue(0);
    }

    public void setPlaying(boolean playing) {
        playingProperty.set(playing);
    }

    public boolean isPlaying() {
        return playingProperty.get();
    }

    public void setSliding(boolean sliding) {
        slidingProperty.set(sliding);
    }

    public boolean isSliding() {
        return slidingProperty.get();
    }

    public void setRepeat(boolean repeat) {
        repeatProperty.set(repeat);
    }

    public boolean isRepeat() {
        return repeatProperty.get();
    }

    public void setListview(@NotNull ListView<Item> listview) {
        this.listView = listview;
    }

    public void setRootController(@NotNull Object rootController) {
        this.rootController = rootController;
    }

    private void showErrorDialog(@NotNull Exception exception) {
        var alert = new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.OK);
        alert.setTitle("Media error");
        alert.setHeaderText("Media error");
        alert.getDialogPane().getStylesheets().add(AppConfigManager.getInstance().loadTheme().getPath());
        alert.initOwner(App.getInstance().getStage());
        alert.showAndWait();

        if (rootController instanceof MusicController controller)
            controller.stop();
        else if (rootController instanceof  FavouriteListController controller)
            controller.stop();
    }
}
