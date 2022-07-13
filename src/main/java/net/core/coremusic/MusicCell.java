package net.core.coremusic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import net.core.coremusic.model.Item;

import java.io.IOException;
import java.util.Objects;

public class MusicCell extends ListCell<Item> {

    @FXML
    private HBox root;

    @FXML
    private ImageView imageview;

    @FXML
    private Label title;

    private FXMLLoader loader;

    @Override
    protected void updateItem(Item item, boolean b) {
        super.updateItem(item, b);

        if (item == null || b) {
            setGraphic(null);
        }else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("music-cell-view.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            var media = item.media();
            var image = ((Image) media.getMetadata().get("image"));
            var musicTitle = ((String) media.getMetadata().get("title"));

            imageview.setImage(Objects.requireNonNullElseGet(image, () -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/music-icon.png")))));
            title.setText(musicTitle);

            setGraphic(root);
        }

        setText(null);
    }
}
