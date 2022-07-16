package net.core.coremusic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private SplitPane splitPane;

    @FXML
    private ToggleButton myMusicBtn, favouritesBtn;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private MusicController musicController;

    private FavouriteListController favouriteListController;

    private VBox musicRoot, favouriteListRoot;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMyMusic();

        myMusicBtn.setToggleGroup(toggleGroup);
        favouritesBtn.setToggleGroup(toggleGroup);

        var emptyPage = createEmptyPage();

        toggleGroup.selectedToggleProperty().addListener((observableValue, previousToggle, currentToggle) -> {
            if (currentToggle != null) {
                if (currentToggle == myMusicBtn)
                    loadMyMusic();
                else if (currentToggle == favouritesBtn)
                    loadFavourites();
            }else {
                splitPane.getItems().set(splitPane.getItems().size() - 1, emptyPage);
            }
        });
    }

    private void loadMyMusic() {
        try {
            if (musicController == null) {
                var loader = new FXMLLoader(getClass().getResource("music-view.fxml"));
                musicRoot = loader.load();
                musicController = loader.getController();

                musicController.setBorderPane(root);
                splitPane.setDividerPositions(0.2168);
            }
            if (splitPane.getItems().size() == 1)
                splitPane.getItems().add(musicRoot);
            else
                splitPane.getItems().set(splitPane.getItems().size() - 1, musicRoot);

            if (!myMusicBtn.isSelected())
                myMusicBtn.setSelected(true);

            musicController.setSelected(true);
            musicController.refresh();

            if (favouriteListController != null) {
                favouriteListController.stop();
                favouriteListController.setSelected(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFavourites() {
        try {
            if (favouriteListController == null) {
                var loader = new FXMLLoader(getClass().getResource("favourite-list-view.fxml"));
                favouriteListRoot = loader.load();
                favouriteListController = loader.getController();

                favouriteListController.setBorderPane(root);
                splitPane.setDividerPositions(0.2168);
            }
            if (splitPane.getItems().size() == 1)
                splitPane.getItems().add(favouriteListRoot);
            else
                splitPane.getItems().set(splitPane.getItems().size() - 1, favouriteListRoot);

            favouriteListController.setSelected(true);
            favouriteListController.refresh();

            if (musicController != null) {
                musicController.stop();
                musicController.setSelected(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StackPane createEmptyPage() {
        var message = new Label("Please selected an item.");
        message.setFont(Font.font(18));

        return new StackPane(message);
    }

}
