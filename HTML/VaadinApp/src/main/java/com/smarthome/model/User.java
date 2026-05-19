package com.smarthome.model; //package for all models

public class User {

    private Long id; // id(primary key in database)
    private String username; //login name
    private String passwordHash;   // BCrypt hash of password
    private double pricePerKWh = 2.50; //price in dkk/kwh


    //Getters og setters
    public Long getId() {return id;} //get id
    public void setId(Long id) {this.id = id;} //set id

    public String getUsername() {return username;} //get username
    public void setUsername(String username) {this.username = username;} //set username

    public String getPasswordHash() {return passwordHash;} //get password hash
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;} //set password hash

    public double getPricePerKWh() {return pricePerKWh;} //get price
    public void setPricePerKWh(double pricePerKWh) {this.pricePerKWh = pricePerKWh;} //set price
}
