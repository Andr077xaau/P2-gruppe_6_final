package com.smarthome.model; //package for all models

public class Device {

    private Long id; // id(primary key in database)
    private User user; //user 
    private String name;//name of device
    private double powerWatts; //power in watts


    //Getters and setters

    //get/set id, user, name and powerWatts
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPowerWatts() { return powerWatts; }
    public void setPowerWatts(double powerWatts) { this.powerWatts = powerWatts; }
}
