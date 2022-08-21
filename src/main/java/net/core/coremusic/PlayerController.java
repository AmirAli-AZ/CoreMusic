package net.core.coremusic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import net.core.coremusic.model.Item;
import net.core.coremusic.utils.FavouritesDBManager;
import net.core.coremusic.utils.Icons;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PlayerController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private Button repeatBtn;

    @FXML
    private SVGPath playSvgPath, favouriteSvgPath, repeatSvgPath, volumeSvgPath;

    @FXML
    private Label currentTimeLabel, totalTimeLabel;

    @FXML
    private Slider slider, volumeSlider;

    private MediaPlayer player;

    private Media media;

    private final BooleanProperty
            playingProperty = new SimpleBooleanProperty(),
            slidingProperty = new SimpleBooleanProperty(),
            repeatProperty = new SimpleBooleanProperty() {
                @Override
                public void set(boolean b) {
                    super.set(b);

                    if (b)
                        repeatSvgPath.setContent(Icons.REPEAT_ON);
                    else
                        repeatSvgPath.setContent(Icons.REPEAT_OFF);
                }
            },
            randomPlayerProperty = new SimpleBooleanProperty();

    private ListView<Item> listView;

    private final FavouritesDBManager favouriteDBManager = FavouritesDBManager.getInstance();

    private Item item;

    private Object rootController;

    private ContextMenu contextMenu;

    private final CheckMenuItem randomPlayerCheckMenuItem = new CheckMenuItem("Random player");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (media == null)
                return;
            currentTimeLabel.setText(formatDuration(media.getDuration(), Duration.seconds(newValue.doubleValue())));
        });

        volumeSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (player == null)
                return;
            var doubleValue = newValue.doubleValue();

            player.setVolume(doubleValue * 0.01);

            if (doubleValue == 0)
                volumeSvgPath.setContent(Icons.VOLUME_OFF);
            else if (doubleValue <= 50)
                volumeSvgPath.setContent(Icons.VOLUME_DOWN);
            else
                volumeSvgPath.setContent(Icons.VOLUME_UP);
        });

        contextMenu = createContextMenu();

        randomPlayerProperty.addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                setRepeat(false);
                repeatSvgPath.setContent(Icons.REPEAT_OFF);
                repeatBtn.setDisable(true);
            }else {
                if (repeatBtn.isDisabled())
                    repeatBtn.setDisable(false);
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
            if (!isRepeat())
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

        setRepeat(!isRepeat());
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

    @FXML
    public void more(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY && !contextMenu.isShowing())
            return;

        contextMenu.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
    }

    public void initPlayer(@NotNull Item item) {
        this.item = item;

        media = new Media(item.getPath().toUri().toString());
        player = new MediaPlayer(media);
        player.setOnReady(() -> {
            slider.setMax(media.getDuration().toSeconds());
            totalTimeLabel.setText(formatDuration(media.getDuration()));
            player.setVolume(volumeSlider.getValue() * 0.01);
            player.play();
            setPlaying(true);
            playSvgPath.setContent(Icons.PAUSE);
        });
        player.setOnEndOfMedia(() -> {
            if (isRandomPlayer()) {
                playRandom();
            }else if (isRepeat()) {
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

        if (listView.getItems().size() < 2) {
            randomPlayerCheckMenuItem.setSelected(false);
            randomPlayerCheckMenuItem.setDisable(true);
        } else {
            randomPlayerCheckMenuItem.setDisable(false);
        }
    }

    private String formatDuration(@NotNull Duration duration, @NotNull Duration currentDuration) {
        if (duration.toHours() >= 1)
            return String.format(
                    "%02d:%02d:%02d",
                    ((long) (currentDuration.toHours() % 24)),
                    ((long) (currentDuration.toMinutes() % 60)),
                    ((long) (currentDuration.toSeconds() % 60))
            );
        return String.format(
                "%02d:%02d",
                ((long) (currentDuration.toMinutes() % 60)),
                ((long) (currentDuration.toSeconds() % 60))
        );
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

    public boolean isRandomPlayer() {
        return randomPlayerProperty.get();
    }

    public void setListview(@NotNull ListView<Item> listview) {
        this.listView = listview;
    }

    public void setRootController(@NotNull Object rootController) {
        this.rootController = rootController;
    }

    private ContextMenu createContextMenu() {
        randomPlayerProperty.bind(randomPlayerCheckMenuItem.selectedProperty());

        return new ContextMenu(randomPlayerCheckMenuItem);
    }

    public void playRandom() {
        var size = listView.getItems().size();
        if (listView.getItems().isEmpty() || size < 2)
            return;

        var randomIndex = (int) Math.floor(Math.random() * size);

        initPlayer(listView.getItems().get(randomIndex));
        listView.getSelectionModel().select(randomIndex);
    }
}
