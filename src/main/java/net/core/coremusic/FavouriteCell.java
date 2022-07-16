package net.core.coremusic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import net.core.coremusic.model.Item;

import java.io.IOException;

public class FavouriteCell extends ListCell<Item> {

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
                loader = new FXMLLoader(getClass().getResource("favourite-cell-view.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            imageview.setImage(item.image());
            title.setText(item.title());

            setGraphic(root);
        }

        setText(null);
    }
}
