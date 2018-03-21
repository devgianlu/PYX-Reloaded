package com.gianlu.pyxreloaded.singletons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class ServerDatabase {
    private final Connection conn;

    public ServerDatabase(Preferences preferences) throws SQLException {
        String username = preferences.getString("serverDb/username", null);
        String password = preferences.getString("serverDb/password", null);

        if (username != null && !username.isEmpty() && password != null)
            conn = DriverManager.getConnection(preferences.getString("serverDb/url", "jdbc:sqlite:server.sqlite"), username, password);
        else
            conn = DriverManager.getConnection(preferences.getString("serverDb/url", "jdbc:sqlite:server.sqlite"));
    }

    public Statement statement() throws SQLException {
        return conn.createStatement();
    }

    public void close() throws SQLException {
        conn.close();
    }

    @Override
    protected void finalize() throws Throwable {
        conn.close();
    }
}
