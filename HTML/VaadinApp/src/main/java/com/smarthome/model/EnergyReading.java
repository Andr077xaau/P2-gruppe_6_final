package com.smarthome.model; //package for all models

import java.time.LocalDateTime; //for date and time

public class EnergyReading {
    private Long id; // id(primary key in database)
    private Device device; //hvilken device
    private User user; //user
    private double hoursUsed; //how long worked(hours)
    private double kWh; // energy used = (watts × hours) / 1000
    private LocalDateTime recordedAt; //when
    private double pricePerKWh; // price in dkk/kwh when recorded (from electricity_prices)

    //Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public double getHoursUsed() { return hoursUsed; }
    public void setHoursUsed(double hoursUsed) { this.hoursUsed = hoursUsed; }

    public double getKWh() { return kWh; }
    public void setKWh(double kWh) { this.kWh = kWh; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public double getPricePerKWh() { return pricePerKWh; }
    public void setPricePerKWh(double pricePerKWh) { this.pricePerKWh = pricePerKWh; }

    public double getCost() { return kWh * pricePerKWh; } // price of this reading in dkk
}
