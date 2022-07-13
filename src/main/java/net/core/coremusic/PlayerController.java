package net.core.coremusic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private final BooleanProperty playingProperty = new SimpleBooleanProperty(), slidingProperty = new SimpleBooleanProperty();

    @FXML
    public void forward(ActionEvent event) {

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

    public void setItem(@NotNull Item item) {
        player = new MediaPlayer(item.media());
        player.setOnReady(() -> {
            player.play();
            playingProperty.set(true);
        });

        slider.setMin(0);
        slider.setMax(item.media().getDuration().toSeconds());
        totalTimeLabel.setText(formatDuration(item.media().getDuration()));
        player.currentTimeProperty().addListener((observableValue, duration, currentDuration) -> {
            if (!slidingProperty.get()) {
                slider.setValue(currentDuration.toSeconds());
                currentTimeLabel.setText(formatDuration(item.media().getDuration(), currentDuration));
            }
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
    }

    public BooleanProperty playingProperty() {
        return playingProperty;
    }
}
