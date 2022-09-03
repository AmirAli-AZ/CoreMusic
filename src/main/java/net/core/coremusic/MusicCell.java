package net.core.coremusic;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.core.coremusic.model.Item;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MusicCell extends ListCell<Item> {


    private HBox root;

    private ImageView imageview;

    private Label title;


    @Override
    protected void updateItem(Item item, boolean b) {
        super.updateItem(item, b);

        setText(null);
        if (item == null || b) {
            setGraphic(null);
        }else {
            if (root == null)
                createView();

            setContent(item);
            setGraphic(root);
        }
    }

    private void createView() {
        imageview = new ImageView();
        imageview.setFitWidth(50);
        imageview.setFitHeight(50);

        title = new Label();
        HBox.setMargin(title, new Insets(5, 0, 0, 0));

        root = new HBox(5, imageview, title);
        root.setStyle("-fx-background-color: transparent;");
    }

    private void setContent(Item item) {
        if (Files.notExists(item.getPath()))
            return;

        var media = new Media(item.getPath().toUri().toString());
        var player = new MediaPlayer(media);

        player.setOnReady(() -> {
            var image = Objects.requireNonNullElseGet(((Image) media.getMetadata().get("image")), () -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/CoreMusicLogo64.png"))));
            var musicTitle = Objects.requireNonNullElseGet(((String) media.getMetadata().get("title")), () -> getFileName(item.getPath()));

            item.setImage(image);
            item.setTitle(musicTitle);

            imageview.setImage(image);
            title.setText(musicTitle);

            player.dispose();
        });
    }

    private String getFileName(@NotNull Path path) {
        var name = path.getFileName().toString();
        var lastIndex = name.lastIndexOf('.');

        if (lastIndex == -1)
            return "";

        return name.substring(0, lastIndex);
    }
}
