package net.core.coremusic;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import net.core.coremusic.utils.AppConfigManager;
import net.core.coremusic.utils.DirectoryWatcher;
import net.core.coremusic.utils.Themes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.StandardWatchEventKinds;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private SplitPane splitPane;

    @FXML
    private ToggleButton themesBtn, musicDirBtn;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private VBox themesSettingsPage, musicDirSettingsPage;

    private StackPane emptyPage;

    private final AppConfigManager configManager = AppConfigManager.getInstance();

    private MusicController musicController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadThemesSettingsPage();
        splitPane.setDividerPositions(0.25);

        themesBtn.setToggleGroup(toggleGroup);
        musicDirBtn.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((observableValue, previousToggle, currentToggle) -> {
            if (currentToggle != null) {
                if (currentToggle == themesBtn)
                    loadThemesSettingsPage();
                else if (currentToggle == musicDirBtn)
                    loadMusicDirSettingsPage();
            }else {
                loadEmptyPage();
            }
        });
    }

    private void loadThemesSettingsPage() {
        if (themesSettingsPage == null) {
            var title = new Label("Themes");
            title.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 16));
            var label = new Label("Select a theme");
            var comboBox = new ComboBox<>(FXCollections.observableArrayList(Themes.values()));
            var currentTheme = configManager.loadTheme();
            comboBox.getSelectionModel().select(currentTheme);
            comboBox.valueProperty().addListener((observableValue, previousTheme, newTheme) -> configManager.applyThemeToAllWindows(newTheme));
            var hbox = new HBox(5, label, comboBox);
            hbox.setAlignment(Pos.CENTER_LEFT);

            themesSettingsPage = new VBox(
                    5,
                    title, new Separator(),
                    hbox
            );
            themesSettingsPage.setPadding(new Insets(5));
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(themesSettingsPage);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, themesSettingsPage);

        if (!themesBtn.isSelected())
            themesBtn.setSelected(true);
    }

    private void loadMusicDirSettingsPage() {
        if (musicDirSettingsPage == null) {
            var title = new Label("Music Directory");
            title.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 16));
            var label = new Label("Change Music Directory");
            var changeButton = new Button("Change");
            changeButton.setDefaultButton(true);
            changeButton.setPrefSize(75,25);
            changeButton.setOnAction(actionEvent -> {
                if (musicController != null && App.getInstance().askMusicFolder()) {
                    musicController.refresh();

                    var watcher = DirectoryWatcher.getInstance();
                    var musicDirPath = configManager.getMusicDirPath();

                    musicDirPath.ifPresent(path -> {
                        try {
                            watcher.register(
                                    path,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_MODIFY,
                                    StandardWatchEventKinds.ENTRY_DELETE
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            var hbox = new HBox(5, label, changeButton);
            hbox.setAlignment(Pos.CENTER_LEFT);

            musicDirSettingsPage = new VBox(
                    5,
                    title, new Separator(),
                    hbox
            );
            musicDirSettingsPage.setPadding(new Insets(5));
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(musicDirSettingsPage);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, musicDirSettingsPage);

        if (!musicDirBtn.isSelected())
            musicDirBtn.setSelected(true);
    }

    private void loadEmptyPage() {
        if (emptyPage == null) {
            var message = new Label("Please selected an item.");
            message.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 16));
            emptyPage = new StackPane(message);
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(emptyPage);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, emptyPage);
    }

    public void setMusicController(@NotNull MusicController musicController) {
        this.musicController = musicController;
    }
}
