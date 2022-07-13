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
import net.core.coremusic.utils.DirectoryWatcher;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private SplitPane splitPane;

    @FXML
    private ToggleButton myMusicBtn, favouritesBtn;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private VBox myMusicRoot;

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
            }else {
                splitPane.getItems().set(splitPane.getItems().size() - 1, emptyPage);
            }
        });
    }

    private void loadMyMusic() {
        try {
            if (myMusicRoot == null) {
                myMusicRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("music-view.fxml")));
                splitPane.setDividerPositions(0.2168);
            }
            if (splitPane.getItems().size() == 1)
                splitPane.getItems().add(myMusicRoot);
            else
                splitPane.getItems().set(splitPane.getItems().size() - 1, myMusicRoot);
            if (!myMusicBtn.isSelected())
                myMusicBtn.setSelected(true);
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
