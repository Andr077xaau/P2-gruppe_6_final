package com.smarthome; // main package

import java.io.IOException; // for errors when reading files
import java.io.InputStream; // to read the demo_prices.sql file
import java.nio.charset.StandardCharsets; // UTF-8
import java.sql.Connection; //to hold connection to the database
import java.sql.ResultSet; // result of a query
import java.sql.DriverManager; // to connect to the database
import java.sql.SQLException;// for sql errors
import java.sql.Statement; // to run SQL statements that dont have parameters

public class Database {

    private final Connection connection; //jdbc connection to the SQLite database file

    public Database(String path) throws SQLException { //makes a database connection with file path as argument
        connection = DriverManager.getConnection("jdbc:sqlite:" + path); 
    }

    // Returns the open connection so repositorys can use it.
    public Connection connect() {
        return connection;
    }

    public void createTables() throws SQLException {    // Creates the three tables if they don't already exist.
        try (Statement stmt = connection.createStatement()) {

            stmt.execute( //users table
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    price_per_kwh REAL NOT NULL DEFAULT 2.50,
                    is_admin INTEGER NOT NULL DEFAULT 0,
                    admin_id INTEGER REFERENCES users(id))""");

            addColumnIfMissing(stmt, "users", "is_admin", "INTEGER NOT NULL DEFAULT 0"); // for old databases made before admin existed
            addColumnIfMissing(stmt, "users", "admin_id", "INTEGER REFERENCES users(id)");
            stmt.execute("UPDATE users SET is_admin = 1 WHERE username = 'admin'"); // demo user admin is always admin

            stmt.execute( // devices table
                """
                CREATE TABLE IF NOT EXISTS devices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL REFERENCES users(id),
                    name TEXT NOT NULL,
                    power_watts REAL NOT NULL)""");

            stmt.execute(// energy_readings table
                """
                CREATE TABLE IF NOT EXISTS energy_readings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL REFERENCES users(id),
                    device_id INTEGER NOT NULL REFERENCES devices(id),
                    hours_used REAL NOT NULL,
                    kwh REAL NOT NULL,
                    recorded_at TEXT NOT NULL)""");

            stmt.execute(// electricity_prices table (demo price for each hour of the day)
                """
                CREATE TABLE IF NOT EXISTS electricity_prices (
                    hour INTEGER PRIMARY KEY CHECK (hour BETWEEN 0 AND 23),
                    price_per_kwh REAL NOT NULL)""");
        }
        loadDemoPrices();
    }

    private void addColumnIfMissing(Statement stmt, String table, String column, String type) throws SQLException { // add column to old table if it doesnt have it
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) { // list of columns in table
            while (rs.next()) {
                if (rs.getString("name").equals(column)) return; // column already exists
            }
        }
        stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    private void loadDemoPrices() throws SQLException { // fill electricity_prices with demo data from demo_prices.sql
        String script;
        try (InputStream in = Database.class.getResourceAsStream("/demo_prices.sql")) { // file is in src/main/resources
            if (in == null) throw new SQLException("demo_prices.sql not found");
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("could not read demo_prices.sql", e);
        }

        try (Statement stmt = connection.createStatement()) {
            for (String line : script.split("\n")) { // one INSERT per line
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue; // skip empty lines and comments
                stmt.execute(line);
            }
        }
    }
}
