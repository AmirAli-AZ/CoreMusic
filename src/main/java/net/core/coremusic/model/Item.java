package net.core.coremusic.model;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record Item(@NotNull String title, @NotNull Image image, @NotNull Path path) {
}
