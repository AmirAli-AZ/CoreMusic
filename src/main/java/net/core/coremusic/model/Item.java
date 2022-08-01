package net.core.coremusic.model;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Item {

    private Path path;

    private String title;

    private Image image;

    public Item(@NotNull Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
