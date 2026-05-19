package com.smarthome; //main package

import com.smarthome.service.EnergyService; // service for energy readings and devices
import com.smarthome.service.UserService; // service for user accounts

public class ServiceLocator {
    private static UserService userService;   // empty object variables for the services, will be filled at startup
    private static EnergyService energyService; 


    public static void init(UserService users, EnergyService energy) { //fill empty variables with actual services, called at startup from Application.java
        userService = users;
        energyService = energy;
    }

    public static UserService users() { // to get userService in views
        return userService;
    }


    public static EnergyService energy() { // to get energyService in views
        return energyService;
    }
}
