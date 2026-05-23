package com.smarthome.service; //package for all services

import com.smarthome.model.Device;  // Device model
import com.smarthome.model.EnergyReading; // EnergyReading model
import com.smarthome.model.User; // User model
import com.smarthome.repository.DeviceRepository; //device repository for database access
import com.smarthome.repository.EnergyReadingRepository;// energy reading repository for database access
import java.time.LocalDate; // calendar date
import java.time.LocalDateTime; // date + time
import java.util.ArrayList; // growable list
import java.util.HashMap; // map for grouping
import java.util.LinkedHashMap; // Map that remembers insertion order

// all device and energy-reading logic.
public class EnergyService {
    private final DeviceRepository deviceRepository; // empty object variables for the repository
    private final EnergyReadingRepository readingRepository; 

    public EnergyService(DeviceRepository deviceRepository, EnergyReadingRepository readingRepository) { // give repositories to empty variables
        this.deviceRepository  = deviceRepository;
        this.readingRepository = readingRepository;
    }


    //  Device methods 
    public ArrayList<Device> getDevicesForUser(User user) { // find all devices that belongs to a user
        return deviceRepository.findByUser(user);
    }


    public Device addDevice(User user, String name, double powerWatts) { // Creates a new device and saves it to the database.
        Device device = new Device();
        device.setUser(user);
        device.setName(name);
        device.setPowerWatts(powerWatts);
        return deviceRepository.save(device); // add to database
    }


    public void deleteDevice(Device device) { // delete device and all its readings from database
        readingRepository.deleteByDevice(device); // delete readings first so we dont have stray readings with no device
        deviceRepository.delete(device); //delete device from database
    }


    //  Energy reading methods
    public EnergyReading logReading(User user, Device device, double hoursUsed) { //log a new energy reading for a device with the current timestamp
        return logReadingAt(user, device, hoursUsed, LocalDateTime.now());
    }


    public EnergyReading logReadingAt(User user, Device device, double hoursUsed, LocalDateTime at) { // log a new energy reading for a device with a specific timestamp(used for demo data)
        double kWh = calculateEnergy(device.getPowerWatts(), hoursUsed); 
        EnergyReading reading = new EnergyReading();
        reading.setUser(user);
        reading.setDevice(device);
        reading.setHoursUsed(hoursUsed);
        reading.setKWh(kWh); 
        reading.setRecordedAt(at);
        return readingRepository.save(reading); //add to database
    }


    public ArrayList<EnergyReading> getRecentReadings(User user) { // find 10 last readings for user
        return readingRepository.findLast10ByUser(user);
    }


    //Statistics
    public double getTodayKWh(User user) { //total kwh for today, from midnight to now
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // midnight at start of today
        LocalDateTime endOfDay = startOfDay.plusDays(1); // midnight at start of tomorrow
        return readingRepository.sumKWhBetween(user, startOfDay, endOfDay);
    }

  
    public double getMonthKWh(User user) { // total kwh for this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(); // first day of month at midnight
        LocalDateTime endOfMonth   = startOfMonth.plusMonths(1); // first day of next month at midnight
        return readingRepository.sumKWhBetween(user, startOfMonth, endOfMonth);
    }


    public double getMonthCost(User user) { //cost this month
        return getMonthKWh(user) * user.getPricePerKWh();
    }


    public double getMaxDayKWhThisMonth(User user) { // highest one-day kWh this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(); // first day of month at midnight
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1); // first day of next month at midnight
        ArrayList<EnergyReading> readings = readingRepository.findByUserByTime(user, startOfMonth); //all readings this month

        HashMap<LocalDate, Double> daily = new HashMap<>(); 
        for (EnergyReading r : readings) {
            if (r.getRecordedAt().isBefore(endOfMonth)) {
                daily.merge(r.getRecordedAt().toLocalDate(), r.getKWh(), Double::sum); // for each reading, get the date and add the kwh to the map, if there is already something for this date, sum them
            }
        }
        return daily.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0); // find the maximum kWh value, return 0 ifno readings
    }


    public LinkedHashMap<LocalDate, Double> getLast7DaysKWh(User user) { // total kwh for each of the last 7 days, returned as a map where the key is the date and value is kwh this day
        LocalDateTime weekAgo = LocalDate.now().minusDays(6).atStartOfDay(); // 6 days ago at 00:00
        ArrayList<EnergyReading> readings = readingRepository.findByUserByTime(user, weekAgo); 

        LinkedHashMap<LocalDate, Double> result = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            result.put(LocalDate.now().minusDays(i), 0.0); //Fill the map with 0 so empty days still appear in chart.
        }

        for (EnergyReading r : readings) {
            result.merge(r.getRecordedAt().toLocalDate(), r.getKWh(), Double::sum); // for each reading, get the date and add the kwh to the map, if there is already something for this date, sum them
        }
        return result;
    }

    // Math 
    private double calculateEnergy(double powerWatts, double hours) { // calculate kWh from power and hours
        return (powerWatts * hours) / 1000.0;
    }
}
