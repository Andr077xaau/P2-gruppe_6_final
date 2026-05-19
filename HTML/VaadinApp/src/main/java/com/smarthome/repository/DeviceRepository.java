package com.smarthome.repository; //package for repositories

import com.smarthome.Database;    // for database connection
import com.smarthome.model.Device; // device model
import com.smarthome.model.User;   // user model
import java.sql.*; // sql
import java.util.ArrayList; // growable list used to collect query results

public class DeviceRepository {
    private final Database db; // database connection

    public DeviceRepository(Database db) { //give database connection to repository
        this.db = db;
    }


    public ArrayList<Device> findByUser(User user) { // find all devices that belongs to a user
        String sql = "SELECT * FROM devices WHERE user_id = ?";
        ArrayList<Device> result = new ArrayList<>(); // make an arraylist to hold the results
        try (PreparedStatement stmt = db.connect().prepareStatement(sql)) {// make a preparedStatment variable with the sql
            stmt.setLong(1, user.getId()); // replace ? with the users id
            try (ResultSet rs = stmt.executeQuery()) { // run the sql command and get the result as rs
                while (rs.next()) {//while next row exists(there are more devices for this user)
                    result.add(deviceObject(rs, user)); // convert row to Device and add to list
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUser failed", e); // if something goes wrong with sql, make an exception
        }
        return result;
    }


    public Device save(Device device) { //save/update a device to the database
        if (device.getId() == null) { // if its new device
            String sql = "INSERT INTO devices (user_id, name, power_watts) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = db.connect().prepareStatement( // make a preparedStatment variable with the sql
                    sql, Statement.RETURN_GENERATED_KEYS)) { // RETURN_GENERATED_KEYS returns the autoincremented id
                stmt.setLong(1, device.getUser().getId()); // replace ? with info from device object
                stmt.setString(2, device.getName());
                stmt.setDouble(3, device.getPowerWatts());
                stmt.executeUpdate(); // run sql command
                try (ResultSet keys = stmt.getGeneratedKeys()) { //makes variable "keys" with generated keys (new id)
                    if (keys.next()) device.setId(keys.getLong(1));  // if got keys, set new id  in the object
                }
            } catch (SQLException e) {
                throw new RuntimeException("save (insert) failed", e); // if something goes wrong with sql, make an exception
            }
        }
        return device; 
    }


    public void delete(Device device) { // delete device from database
        String sql = "DELETE FROM devices WHERE id = ?";
        try (PreparedStatement stmt = db.connect().prepareStatement(sql)) {
            stmt.setLong(1, device.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete failed", e);
        }
    }


    // Converts one database row into a Device Java object.
    private Device deviceObject(ResultSet rs, User user) throws SQLException {
        Device device = new Device();
        device.setId(rs.getLong("id"));
        device.setUser(user);
        device.setName(rs.getString("name"));
        device.setPowerWatts(rs.getDouble("power_watts"));
        return device;
    }
}
