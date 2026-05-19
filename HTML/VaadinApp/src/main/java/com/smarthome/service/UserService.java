package com.smarthome.service; //package for all services

import com.smarthome.model.User; // User model
import com.smarthome.repository.UserRepository; // user repository for database access
import org.mindrot.jbcrypt.BCrypt; // password hashing library
import java.util.Optional; // to stop null pointer exceptions

// all user-account logic.
public class UserService {
    private final UserRepository userRepository; // empty object variable for the repository


    public UserService(UserRepository userRepository) { // give repository to empty variable
        this.userRepository = userRepository;
    }


    public Optional<User> findByUsername(String username) { // find a user by their username (optional because it might not exist)
        return userRepository.findByUsername(username);
    }


    public boolean checkPassword(User user, String rawPassword) { // check if the password is correct by comparing the raw password with the stored hash
        return BCrypt.checkpw(rawPassword, user.getPasswordHash());
    }


    public User register(String username, String rawPassword) { // register a new user with username and password
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Brugernavnet eksisterer allerede");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt())); //hash the password with BCrypt before storing it in the database. BCrypt.gensalt() generates a random salt for each passwords, so even if two users have the same password, their hashes will be different.
        return userRepository.save(newUser); // write to database
    }


    public User save(User user) { // Save changes to existing user (f.eks. update price).
        return userRepository.save(user); 
    }


    public long count() { // count users in database (just for demo data)
        return userRepository.count(); 
    }
}
