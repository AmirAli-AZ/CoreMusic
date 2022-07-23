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
import net.core.coremusic.utils.Themes;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private SplitPane splitPane;

    @FXML
    private ToggleButton themesBtn;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private VBox themesPage;

    private StackPane emptyPage;

    private final AppConfigManager configManager = AppConfigManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadThemesPage();
        splitPane.setDividerPositions(0.25);

        themesBtn.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((observableValue, previousToggle, currentToggle) -> {
            if (currentToggle != null) {
                if (currentToggle == themesBtn)
                    loadThemesPage();
            }else {
                loadEmptyPage();
            }
        });
    }

    private void loadThemesPage() {
        if (themesPage == null) {
            var title = new Label("Themes");
            title.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 16));
            var label = new Label("Select a theme");
            var comboBox = new ComboBox<>(FXCollections.observableArrayList(Themes.values()));
            var currentTheme = configManager.loadTheme();
            comboBox.getSelectionModel().select(currentTheme);
            comboBox.valueProperty().addListener((observableValue, previousTheme, newTheme) -> configManager.setThemeAndCallListeners(newTheme, splitPane.getScene()));
            var hbox = new HBox(3, label, comboBox);
            hbox.setAlignment(Pos.CENTER_LEFT);

            themesPage = new VBox(
                    5,
                    title, new Separator(),
                    hbox
            );
            themesPage.setPadding(new Insets(5));
        }

        if (splitPane.getItems().size() == 1)
            splitPane.getItems().add(themesPage);
        else
            splitPane.getItems().set(splitPane.getItems().size() - 1, themesPage);

        if (!themesBtn.isSelected())
            themesBtn.setSelected(true);
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
}
