package com.hotel.model;

import java.io.Serializable;

/**
 * Room.java - Represents a hotel room (Week 6: Serializable for file storage)
 * Demonstrates: Encapsulation (Week 1), Serialization (Week 6)
 */
public class Room implements Serializable {

    // Week 1: Encapsulation - private fields
    private int roomNumber;
    private String roomType;    // Single, Double, Deluxe, Suite
    private double pricePerDay;
    private boolean available;
    private String amenities;   // e.g. "WiFi, AC, TV"

    // Week 1: Constructor
    public Room(int roomNumber, String roomType, double pricePerDay, String amenities) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.available = true;  // new room is always available
        this.amenities = amenities;
    }

    // Week 1: Getters and Setters
    public int getRoomNumber()            { return roomNumber; }
    public String getRoomType()           { return roomType; }
    public double getPricePerDay()        { return pricePerDay; }
    public boolean isAvailable()          { return available; }
    public String getAmenities()          { return amenities; }

    public void setAvailable(boolean available) { this.available = available; }
    public void setPricePerDay(double price)    { this.pricePerDay = price; }
    public void setRoomType(String type)        { this.roomType = type; }
    public void setAmenities(String amenities)  { this.amenities = amenities; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " [" + roomType + "] - ₹" + pricePerDay
               + "/day | " + (available ? "Available" : "Occupied");
    }
}