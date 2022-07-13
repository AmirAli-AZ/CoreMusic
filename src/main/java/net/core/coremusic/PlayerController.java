package net.core.coremusic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import net.core.coremusic.model.Item;
import net.core.coremusic.utils.Icons;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerController {

    @FXML
    private VBox root;

    @FXML
    private Button rewindBtn, playBtn, forwardBtn;

    @FXML
    private SVGPath rewindSvgPath, playSvgPath, forwardSvgPath;

    @FXML
    private Label currentTimeLabel, totalTimeLabel;

    @FXML
    private Slider slider;

    private MediaPlayer player;

    private final BooleanProperty playingProperty = new SimpleBooleanProperty() , slidingProperty = new SimpleBooleanProperty();

    private ListView<Item> listView;

    @FXML
    public void forward(ActionEvent event) {
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
        slidingProperty.set(true);
    }

    @FXML
    public void sliderReleased(MouseEvent event) {
        player.seek(Duration.seconds(slider.getValue()));
        slidingProperty.set(false);
    }

    public Parent getRoot() {
        return root;
    }

    public void initPlayer(@NotNull Item item) {
        player = new MediaPlayer(item.media());
        player.setOnReady(() -> {
            player.play();
            playSvgPath.setContent(Icons.PAUSE);
            playingProperty.set(true);
        });
        player.setOnEndOfMedia(() -> playingProperty.set(false));

        slider.setMax(item.media().getDuration().toSeconds());
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> currentTimeLabel.setText(formatDuration(item.media().getDuration(), Duration.seconds(newValue.doubleValue()))));
        totalTimeLabel.setText(formatDuration(item.media().getDuration()));
        player.currentTimeProperty().addListener((observableValue, duration, currentDuration) -> {
            if (!slidingProperty.get())
                slider.setValue(currentDuration.toSeconds());
        });
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
        if (playingProperty.get()) {
            player.stop();
            playingProperty.set(false);
        }
        slider.setValue(0);
    }

    public BooleanProperty playingProperty() {
        return playingProperty;
    }

    public void setListview(@NotNull ListView<Item> listview) {
        this.listView = listview;
    }
}
