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

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class FavouriteCell extends ListCell<Item> {


    private HBox root;

    private final ImageView imageview = new ImageView();

    private final Label title = new Label();


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
        imageview.setFitWidth(50);
        imageview.setFitHeight(50);

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
            var musicTitle = Objects.requireNonNullElseGet(((String) media.getMetadata().get("title")), () -> new File(media.getSource()).getName());

            item.setImage(image);
            item.setTitle(musicTitle);

            imageview.setImage(image);
            title.setText(musicTitle);

            player.dispose();
        });
    }
}
