package com.smarthome.repository; //package for repositories

import com.smarthome.Database; // database connection
import com.smarthome.model.User;  // user model
import java.sql.*; // for work with sql
import java.util.Optional; // to stop null pointer exceptions

public class UserRepository {
    private final Database db; // database connection
   
    public UserRepository(Database db) { //give database connection to repository
        this.db = db;
    }


    public Optional<User> findByUsername(String username) { // find a user by their username
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = db.connect().prepareStatement(sql)) {  // make a preparedStatment variable with the sql
            statement.setString(1, username); // replace ? with username
            try (ResultSet rs = statement.executeQuery()) { // run the sql command and get the result
                if (rs.next()) { //is next row exists? when searching it returns ether o or 1 row, so if next row exists its 1 row
                    return Optional.of(userObject(rs)); // return resultat as a user object
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUsername (userRepository) failed", e); // if something goes wrong with sql, make an exception
        }
        return Optional.empty(); // no user found with this username
    }


    public User save(User user) { // save a user to the database
        if (user.getId() == null) { // if its new user
            String sql = "INSERT INTO users (username, password_hash, price_per_kwh) VALUES (?, ?, ?)";
            try (PreparedStatement statement = db.connect().prepareStatement( // make a preparedStatment variable with the sql
                    sql, Statement.RETURN_GENERATED_KEYS)) { // RETURN_GENERATED_KEYS returns the autoincremented id
                statement.setString(1, user.getUsername()); //replace ? with info from user object
                statement.setString(2, user.getPasswordHash());
                statement.setDouble(3, user.getPricePerKWh());
                statement.executeUpdate(); // run sql command
                try (ResultSet keys = statement.getGeneratedKeys()) { //makes variable "keys" with generated keys (new id)
                    if (keys.next()) user.setId(keys.getLong(1)); // store the new id back in the object
                }
            } catch (SQLException e) {
                throw new RuntimeException("save (insert) failed", e); // if something goes wrong with sql, make an exception
            }
        } else { // if user already exists
            String sql = "UPDATE users SET username = ?, password_hash = ?, price_per_kwh = ? WHERE id = ?";
            try (PreparedStatement statement = db.connect().prepareStatement(sql)) {
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPasswordHash());
                statement.setDouble(3, user.getPricePerKWh());
                statement.setLong(4, user.getId());
                statement.executeUpdate(); // run sql command
            } catch (SQLException e) {
                throw new RuntimeException("save (update) failed", e);
            }
        }
        return user;
    }

    
    public long count() { // count how many users are in the database(just for demo data)
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement statement = db.connect().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.getLong(1); // returnes count of users
        } catch (SQLException e) {
            throw new RuntimeException("count failed", e);
        }
    }


    private User userObject(ResultSet rs) throws SQLException {     // Converts one database row (ResultSet) into a User Java object.
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPricePerKWh(rs.getDouble("price_per_kwh"));
        return user;
    }
}
