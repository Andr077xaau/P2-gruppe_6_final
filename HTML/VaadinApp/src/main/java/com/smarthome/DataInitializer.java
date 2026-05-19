package com.smarthome; // main package

import com.smarthome.model.Device;// Device model
import com.smarthome.model.User;// User model
import com.smarthome.service.EnergyService; // energy service
import com.smarthome.service.UserService; // usr service
import java.time.LocalDateTime; // date and time

public class DataInitializer { //adds demo data to the database if the database is empty
    private UserService userService; // user service 
    private EnergyService energyService; //energy service

    public DataInitializer(UserService userService, EnergyService energyService) { //constructor 
        this.userService = userService;
        this.energyService = energyService;
    }

    public void run() {
        if (userService.count() > 0) { //if there are user in database
            return;
        }

        User admin = userService.register("admin", "1234"); //add demo user

        Device tv     = energyService.addDevice(admin, "Fjernsyn",  120);  // add demo devices
        Device fridge = energyService.addDevice(admin, "Køleskab",  150); 
        Device lights = energyService.addDevice(admin, "Belysning",  60); 

        energyService.logReadingAt(admin, tv,     3.5, LocalDateTime.now().minusDays(1)); // log usage for devices
        energyService.logReadingAt(admin, fridge, 8.0, LocalDateTime.now().minusDays(1)); 
        energyService.logReadingAt(admin, lights, 4.0, LocalDateTime.now().minusDays(1)); 
    }
}
