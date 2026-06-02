package com.hotel.model;

import java.io.Serializable;

/**
 * Customer.java - Represents a hotel guest
 * Demonstrates: Encapsulation (Week 1), Serialization (Week 6)
 */
public class Customer implements Serializable {

    private int customerId;
    private String name;
    private String contactNumber;
    private String email;
    private int totalBookings;       // tracks loyalty
    private int totalGuests;         // total people across all bookings
    private String specialOccasion;  // Birthday / Wedding / None

    public Customer(int customerId, String name, String contactNumber,
                    String email, String specialOccasion) {
        this.customerId  = customerId;
        this.name        = name;
        this.contactNumber = contactNumber;
        this.email       = email;
        this.specialOccasion = specialOccasion;
        this.totalBookings = 0;
        this.totalGuests   = 0;
    }

    // ---- Getters ----
    public int    getCustomerId()       { return customerId; }
    public String getName()             { return name; }
    public String getContactNumber()    { return contactNumber; }
    public String getEmail()            { return email; }
    public int    getTotalBookings()    { return totalBookings; }
    public int    getTotalGuests()      { return totalGuests; }
    public String getSpecialOccasion()  { return specialOccasion; }

    // ---- Setters ----
    public void setName(String name)                     { this.name = name; }
    public void setContactNumber(String c)               { this.contactNumber = c; }
    public void setEmail(String e)                       { this.email = e; }
    public void setSpecialOccasion(String s)             { this.specialOccasion = s; }
    public void incrementBookings()                      { this.totalBookings++; }
    public void addGuests(int guests)                    { this.totalGuests += guests; }

    @Override
    public String toString() {
        return "Customer #" + customerId + " - " + name + " | " + contactNumber;
    }
}