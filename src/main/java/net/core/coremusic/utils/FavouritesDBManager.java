package net.core.coremusic.utils;

import net.core.coremusic.model.Item;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class FavouritesDBManager {

    private static FavouritesDBManager instance;

    private final Connection connection;

    private final Path dbPath = Paths.get(Environment.getAppDataPath() + File.separator + "Favourites.db");

    private FavouritesDBManager() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        var statement = connection.createStatement();
        statement.execute("create table if not exists favourites (title text not null, path text unique not null);");
        statement.close();
    }

    public static FavouritesDBManager getInstance() {
        if (instance == null) {
            try {
                instance = new FavouritesDBManager();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public void addToFavourites(@NotNull String title, @NotNull Path path) {
        if (!Files.exists(dbPath))
            return;

        Item addedFavouriteItem = null;

        try {
            var preparedStatement = connection.prepareStatement("insert or ignore into favourites values (?, ?);");
            preparedStatement.setString(1, title);
            preparedStatement.setObject(2, path);

            preparedStatement.execute();
            preparedStatement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFromFavourites(@NotNull Item item) {
        if (!Files.exists(dbPath))
            return;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from favourites where path == ?;");
            preparedStatement.setObject(1, item.path());

            preparedStatement.execute();
            preparedStatement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFromFavourites(@NotNull Path path) {
        if (!Files.exists(dbPath))
            return;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from favourites where path == ?;");
            preparedStatement.setObject(1, path);

            preparedStatement.execute();
            preparedStatement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isAdded(@NotNull Item item) {
        if (!Files.exists(dbPath))
            return false;

        try {
            var preparedStatement = connection.prepareStatement("select * from favourites where title == ?;");
            preparedStatement.setString(1, item.title());

            var result = preparedStatement.executeQuery();
            var resultHasNext = result.next();

            result.close();
            preparedStatement.close();

            return resultHasNext;
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public Path getDbPath() {
        return dbPath;
    }
}
