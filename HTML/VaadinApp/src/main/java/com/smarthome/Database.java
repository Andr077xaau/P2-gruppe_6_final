package com.smarthome; // main package

import java.sql.Connection; //to hold connection to the database
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
                    price_per_kwh REAL NOT NULL DEFAULT 2.50)""");

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
        }
    }
}
