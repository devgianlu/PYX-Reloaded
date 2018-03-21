package com.gianlu.pyxreloaded;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Updater {
    private final static Logger logger = Logger.getLogger(Updater.class.getSimpleName());
    private static final String[] BACKUP_EXCLUDE = {".git", ".backup", ".idea", ".update"};
    private final GHRelease latestRelease;
    private final File currentFiles;
    private final JsonParser parser;

    private Updater(GitHub api) throws IOException {
        this.latestRelease = api.getRepository("devgianlu/PYX-Reloaded").getLatestRelease();
        this.currentFiles = new File(".");
        this.parser = new JsonParser();
    }

    public static void update() throws IOException, SQLException {
        logger.info("Checking for a newer version...");
        Updater updater = new Updater(GitHub.connectAnonymously());
        if (updater.checkVersion()) {
            File backupDir = updater.doBackup();
            logger.info("Files have been backed up into " + backupDir.getAbsolutePath());

            File updateDir = new File(updater.currentFiles, ".update");
            if (!updateDir.exists() && !updateDir.mkdir())
                throw new IllegalStateException("Cannot create update directory: " + updateDir.getAbsolutePath());

            FileUtils.cleanDirectory(updateDir);

            File updateFiles = updater.downloadLatest(updateDir);
            updater.copyPreferences(updateFiles);
            updater.copyDatabase(updateFiles);

            updater.moveUpdatedFiles(updateFiles);
            FileUtils.deleteDirectory(updateDir);
            logger.info("The server has been updated successfully!");
            System.exit(0);
        }
    }

    private static void copyValues(JsonObject from, JsonObject to) {
        for (Map.Entry<String, JsonElement> entry : from.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                JsonElement child = to.get(entry.getKey());
                if (child == null || !child.isJsonObject()) {
                    child = new JsonObject();
                    to.add(entry.getKey(), child);
                }

                copyValues(entry.getValue().getAsJsonObject(), child.getAsJsonObject());
            } else {
                to.add(entry.getKey(), entry.getValue());
            }
        }
    }

    private void moveUpdatedFiles(File updateFiles) throws IOException {
        File[] files = updateFiles.listFiles();
        if (files == null) throw new IllegalStateException("Cannot read update files.");

        for (File file : files) {
            File destFile = new File(currentFiles, file.getName());
            if (destFile.exists()) FileUtils.forceDelete(destFile);

            if (file.isDirectory()) FileUtils.moveDirectoryToDirectory(file, currentFiles, true);
            else FileUtils.moveFileToDirectory(file, currentFiles, true);
        }
    }

    @NotNull
    private File doBackup() throws IOException {
        File backupDir = new File(currentFiles, ".backup");
        if (!backupDir.exists() && !backupDir.mkdir())
            throw new IllegalStateException("Cannot create backup directory: " + backupDir.getAbsolutePath());

        FileUtils.cleanDirectory(backupDir);

        FileUtils.copyDirectory(currentFiles, backupDir, pathname -> !Utils.contains(BACKUP_EXCLUDE, pathname.getName()));
        return backupDir;
    }

    private void copyDatabase(File updateFiles) throws SQLException {
        File oldDb = new File(currentFiles, "server.sqlite");
        File newDb = new File(updateFiles, "server.sqlite");

        try (Connection newConn = DriverManager.getConnection("jdbc:sqlite:" + newDb.getAbsolutePath())) {
            List<String> tables = DatabaseHelper.listTables(newConn);
            DatabaseHelper.copyTables(oldDb, newConn, tables);
        }

        logger.info("Server database has been copied successfully.");
    }

    private void copyPreferences(File updateFiles) throws IOException {
        File newerPrefs = new File(updateFiles, "preferences.json");
        JsonObject newer;

        try (FileReader olderReader = new FileReader(new File(currentFiles, "preferences.json"));
             FileReader newerReader = new FileReader(newerPrefs)) {
            JsonObject older = parser.parse(olderReader).getAsJsonObject();
            newer = parser.parse(newerReader).getAsJsonObject();

            copyValues(older, newer);
        }

        FileUtils.writeStringToFile(newerPrefs, newer.toString());
        logger.info("Preferences have been copied successfully. NOTE: The newer file may contain new keys (which have been set to their default)");
    }

    @NotNull
    private File downloadLatest(@NotNull File into) throws IOException {
        List<GHAsset> assets = latestRelease.getAssets();

        GHAsset zipAsset = null;
        for (GHAsset asset : assets)
            if (asset.getContentType().equals("application/x-zip-compressed"))
                zipAsset = asset;

        if (zipAsset == null) throw new IllegalStateException("Cannot find ZIP asset. " + assets);

        logger.info("Downloading the latest release... (" + zipAsset.getBrowserDownloadUrl() + ")");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(zipAsset.getBrowserDownloadUrl());
            HttpResponse resp = client.execute(get);

            StatusLine sl = resp.getStatusLine();
            if (sl.getStatusCode() != 200)
                throw new IllegalStateException("Failed downloading the asset. Response code was " + sl.getStatusCode());

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IllegalStateException("Response has no entity.");

            float length = entity.getContentLength();
            float downloaded = 0;
            long partial = 0;

            String dir = null;
            try (ZipInputStream zis = new ZipInputStream(entity.getContent())) {
                byte[] buffer = new byte[4096];
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    File newFile = new File(into, zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        if (dir == null) dir = zipEntry.getName();
                        //noinspection ResultOfMethodCallIgnored
                        newFile.mkdirs();
                        continue;
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);

                            partial += len;
                            downloaded += len;
                            if (partial > 1000000) {
                                partial = 0;
                                logger.info(String.format(Locale.getDefault(), "Downloaded %.2f%% (%.0fB/%.0fB)", (downloaded / length) * 100f, downloaded, length));
                            }
                        }
                    }
                }

                zis.closeEntry();
            }

            get.releaseConnection();

            if (dir == null) throw new IllegalStateException("What happened?");

            logger.info("Latest release downloaded successfully!");
            return new File(into, dir);
        }
    }

    private boolean checkVersion() {
        String currentVersion = Utils.getServerVersion(Package.getPackage("com.gianlu.pyxreloaded"));
        if (currentVersion.equals("debug")) throw new IllegalStateException("Cannot update debug version!");

        String latestVersion = latestRelease.getTagName().substring(1);

        if (Utils.isVersionNewer(currentVersion, latestVersion)) {
            logger.info("There is a newer version available! (" + currentVersion + " => " + latestVersion + ")");
            return true;
        } else {
            logger.info("Your version is already the latest! (" + currentVersion + ")");
            return false;
        }
    }

    private static final class DatabaseHelper {

        @SuppressWarnings("ALL")
        private static List<String> listTables(Connection conn) throws SQLException {
            try (Statement statement = conn.createStatement();
                 ResultSet set = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
                List<String> list = new ArrayList<>();
                while (set.next()) list.add(set.getString(1));
                return list;
            }
        }

        @SuppressWarnings("ALL")
        private static void copyTables(File from, Connection to, List<String> tables) throws SQLException {
            try (Statement statement = to.createStatement()) {
                statement.execute("ATTACH DATABASE '" + from.getAbsolutePath() + "' AS old");

                for (String table : tables)
                    statement.executeUpdate("INSERT INTO main." + table + " SELECT * FROM old." + table);

                statement.execute("DETACH old");
            }
        }
    }
}
