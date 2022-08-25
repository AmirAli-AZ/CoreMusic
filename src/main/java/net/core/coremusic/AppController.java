package net.core.coremusic;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import net.core.coremusic.utils.AppConfigManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private SplitPane splitPane;

    @FXML
    private ToggleButton musicBtn, favouritesBtn;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private MusicController musicController;

    private FavouriteListController favouriteListController;

    private VBox musicRoot, favouriteListRoot;

    private StackPane emptyPage;

    private Stage settingsStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMyMusic();
        splitPane.setDividerPositions(0.25);

        musicBtn.setToggleGroup(toggleGroup);
        favouritesBtn.setToggleGroup(toggleGroup);

        toggleGroup.selectToggle(musicBtn);

        toggleGroup.selectedToggleProperty().addListener((observableValue, previousToggle, currentToggle) -> {
            if (currentToggle != null) {
                if (currentToggle == musicBtn)
                    loadMyMusic();
                else if (currentToggle == favouritesBtn)
                    loadFavourites();
            }else {
                loadEmptyPage();
            }
        });

        try {
            settingsStage = createSettingsStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMyMusic() {
        try {
            if (musicController == null) {
                var loader = new FXMLLoader(getClass().getResource("music-view.fxml"));
                musicRoot = loader.load();
                musicController = loader.getController();

                musicController.setBorderPane(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (favouriteListController != null) {
            favouriteListController.stop();
            favouriteListController.setSelected(false);
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(musicRoot);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, musicRoot);

        musicController.setSelected(true);
        musicController.refresh();
    }

    private void loadFavourites() {
        try {
            if (favouriteListController == null) {
                var loader = new FXMLLoader(getClass().getResource("favourite-list-view.fxml"));
                favouriteListRoot = loader.load();
                favouriteListController = loader.getController();

                favouriteListController.setBorderPane(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (musicController != null) {
            musicController.stop();
            musicController.setSelected(false);
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(favouriteListRoot);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, favouriteListRoot);

        favouriteListController.setSelected(true);
        favouriteListController.refresh();
    }

    private void loadEmptyPage() {
        if (emptyPage == null) {
            var message = new Label("Please selected an item.");
            message.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 16));
            emptyPage = new StackPane(message);
        }

        if (musicController != null) {
            musicController.stop();
            musicController.setSelected(false);
        }

        if (favouriteListController != null) {
            favouriteListController.stop();
            favouriteListController.setSelected(false);
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(emptyPage);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, emptyPage);
    }

    @FXML
    public void openSettings(ActionEvent actionEvent) {
        if (settingsStage != null && !settingsStage.isShowing())
            settingsStage.show();
    }

    private Stage createSettingsStage() throws IOException {
        var stage = new Stage();
        stage.setTitle("Settings");
        var loader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
        var scene = new Scene(loader.load());
        AppConfigManager.getInstance().setTheme(AppConfigManager.getInstance().loadTheme(), scene);
        stage.setScene(scene);
        SettingsController controller = loader.getController();
        controller.setMusicController(musicController);
        stage.initOwner(App.getInstance().getStage());

        return stage;
    }
}
