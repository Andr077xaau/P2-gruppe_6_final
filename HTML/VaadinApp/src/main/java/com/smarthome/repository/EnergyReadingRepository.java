package com.smarthome.repository; //package for repositories

import com.smarthome.Database;  // for database connection
import com.smarthome.model.Device;  //device model
import com.smarthome.model.EnergyReading; // energy reading model
import com.smarthome.model.User;  // user model
import java.sql.*; // sql
import java.time.LocalDateTime; // date and time
import java.util.ArrayList; // growable list used to collect query results


public class EnergyReadingRepository {
    private final Database db; // database connection
    private final DeviceRepository deviceRepo; // device repository to load Device objects for readings

    public EnergyReadingRepository(Database db, DeviceRepository deviceRepo) { // give database connection and device repository to this repository
        this.db = db;
        this.deviceRepo = deviceRepo;
    }

    public EnergyReading save(EnergyReading reading) { // save a new energy reading to the database
        String sql = "INSERT INTO energy_readings (user_id, device_id, hours_used, kwh, recorded_at) VALUES (?, ?, ?, ?, ?)"; //insert values into specified columns
        try (PreparedStatement stmt = db.connect().prepareStatement( // make a preparedStatment variable with the sql
                sql, Statement.RETURN_GENERATED_KEYS)) { // RETURN_GENERATED_KEYS returns the autoincremented id
            stmt.setLong(1, reading.getUser().getId()); // replace ? with info from reading object
            stmt.setLong(2, reading.getDevice().getId());
            stmt.setDouble(3, reading.getHoursUsed());
            stmt.setDouble(4, reading.getKWh());
            stmt.setString(5, reading.getRecordedAt().toString()); // store the date-time as text in ISO8601 (eks. 2026-05-15T20:00:00) format
            stmt.executeUpdate(); // run sql command
            try (ResultSet keys = stmt.getGeneratedKeys()) { //makes variable "keys" with generated keys (new id)
                if (keys.next()) reading.setId(keys.getLong(1)); // if got keys, set new id  in the object
            }
        } catch (SQLException e) {
            throw new RuntimeException("save failed", e); // if something goes wrong with sql, make an exception
        }
        return reading;
    }


    public ArrayList<EnergyReading> findLast10ByUser(User user) { // select 10 last readings from user
        String sql = "SELECT * FROM energy_readings WHERE user_id = ? ORDER BY recorded_at DESC LIMIT 10";
        ArrayList<EnergyReading> result = new ArrayList<>();
        try (PreparedStatement stmt = db.connect().prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(readinfObject(rs, user));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findLast10ByUser failed", e);
        }
        return result;
    }


    public ArrayList<EnergyReading> findByUserByTime(User user, LocalDateTime start) { // select all readings from user that are newer than start
        String sql = "SELECT * FROM energy_readings WHERE user_id = ? AND recorded_at >= ?";
        ArrayList<EnergyReading> result = new ArrayList<>(); // make an arraylist to hold the results
        try (PreparedStatement stmt = db.connect().prepareStatement(sql)) { // make a preparedStatment variable with the sql
            stmt.setLong(1, user.getId()); // replace ? with the id
            stmt.setString(2, start.toString()); // replace ? with the start date-time as text iso8601 format
            try (ResultSet rs = stmt.executeQuery()) { // run the sql command and get the result as rs
                while (rs.next()) { //while next row exists(there are more readings for this user that are newer than start)
                    result.add(readinfObject(rs, user)); // convert to object and add to list
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUserByTime failed", e);
        }
        return result;
    }


    public double sumKWhBetween(User user, LocalDateTime start, LocalDateTime end) { // sum of kwh between start and end
        String sql = "SELECT COALESCE(SUM(kwh), 0) FROM energy_readings WHERE user_id = ? AND recorded_at >= ? AND recorded_at < ?"; //coalesce makes it return 0 instead of null
        try (PreparedStatement stmt = db.connect().prepareStatement(sql)) {
            stmt.setLong(1, user.getId()); 
            stmt.setString(2, start.toString());
            stmt.setString(3, end.toString());
            try (ResultSet rs = stmt.executeQuery()) { // run the sql command and get the result as rs
                return rs.getDouble(1); // the SUM result
            }
        } catch (SQLException e) {
            throw new RuntimeException("sumKWhBetween failed", e);
        }
    }


    public void deleteByDevice(Device device) { // delete all readings for a device(when deleting device)
        String sql = "DELETE FROM energy_readings WHERE device_id = ?";
        try (PreparedStatement stmt = db.connect().prepareStatement(sql)) { 
            stmt.setLong(1, device.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteByDevice failed", e);
        }
    }


    private EnergyReading readinfObject(ResultSet rs, User user) throws SQLException { // make an energy reading object with info from the database
        EnergyReading reading = new EnergyReading();
        reading.setId(rs.getLong("id"));
        reading.setUser(user);
        reading.setHoursUsed(rs.getDouble("hours_used"));
        reading.setKWh(rs.getDouble("kwh"));
        reading.setRecordedAt(LocalDateTime.parse(rs.getString("recorded_at"))); // make ISO-8601 string into a LocalDateTime object

        long deviceId = rs.getLong("device_id");// Load the Device object by id
        Device device = deviceRepo.findByUser(user).stream() //make it stream to to find the device with the right id
                .filter(d -> d.getId() == deviceId) // filter the devices by id
                .findFirst() // find the device that matches 
                .orElse(null); // null if the device was deleted after the reading was saved
        reading.setDevice(device);
        return reading;
    }
}
