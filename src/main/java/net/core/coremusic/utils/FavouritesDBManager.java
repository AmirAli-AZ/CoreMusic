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

    private Connection connection;

    private final Path dbPath = Paths.get(Environment.getAppData() + File.separator + "Favourites.db");

    private FavouritesDBManager() throws SQLException {
        init();
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

    public void init() throws SQLException {
        if (connection != null)
            connection.close();
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        var statement = connection.createStatement();
        statement.execute("create table if not exists favourites (path text unique not null);");
        statement.close();
    }

    public void addToFavourites(@NotNull Item item) {
        if (Files.notExists(dbPath))
            return;

        try {
            var preparedStatement = connection.prepareStatement("insert or ignore into favourites values (?);");
            preparedStatement.setObject(1, item.getPath());

            preparedStatement.execute();
            preparedStatement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFromFavourites(@NotNull Item item) {
        removeFromFavourites(item.getPath());
    }

    public void removeFromFavourites(@NotNull Path path) {
        if (Files.notExists(dbPath))
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
        if (Files.notExists(dbPath))
            return false;

        try {
            var preparedStatement = connection.prepareStatement("select * from favourites where path == ?;");
            preparedStatement.setObject(1, item.getPath());

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
