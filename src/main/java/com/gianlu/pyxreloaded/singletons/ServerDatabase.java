package com.gianlu.pyxreloaded.singletons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class ServerDatabase {
    private final Connection conn;

    public ServerDatabase(Preferences preferences) throws SQLException {
        conn = DriverManager.getConnection(preferences.getString("serverDbUrl", "jdbc:sqlite:server.sqlite"));
    }

    public Statement statement() throws SQLException {
        return conn.createStatement();
    }
}
