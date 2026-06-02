package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Booking.java - Represents a room booking record
 * Demonstrates: Encapsulation (Week 1), Serialization (Week 6)
 */
public class Booking implements Serializable {

    // Week 2: enum inside booking for status
    public enum BookingStatus { ACTIVE, CHECKED_OUT, CANCELLED }

    private int bookingId;
    private int customerId;
    private int roomNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int numberOfGuests;
    private double totalAmount;
    private double discountApplied;
    private BookingStatus status;
    private String specialNote;   // e.g. "Birthday discount applied"

    public Booking(int bookingId, int customerId, int roomNumber,
                   LocalDate checkIn, LocalDate checkOut,
                   int numberOfGuests, double pricePerDay,
                   double discountPercent, String specialNote) {
        this.bookingId       = bookingId;
        this.customerId      = customerId;
        this.roomNumber      = roomNumber;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.numberOfGuests  = numberOfGuests;
        this.discountApplied = discountPercent;
        this.specialNote     = specialNote;
        this.status          = BookingStatus.ACTIVE;

        // Calculate total
        long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) days = 1;
        double gross = pricePerDay * days;
        this.totalAmount = gross - (gross * discountPercent / 100.0);
    }

    // ---- Getters ----
    public int           getBookingId()       { return bookingId; }
    public int           getCustomerId()      { return customerId; }
    public int           getRoomNumber()      { return roomNumber; }
    public LocalDate     getCheckIn()         { return checkIn; }
    public LocalDate     getCheckOut()        { return checkOut; }
    public int           getNumberOfGuests()  { return numberOfGuests; }
    public double        getTotalAmount()     { return totalAmount; }
    public double        getDiscountApplied() { return discountApplied; }
    public BookingStatus getStatus()          { return status; }
    public String        getSpecialNote()     { return specialNote; }

    public long getNumberOfDays() {
        long d = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        return d <= 0 ? 1 : d;
    }

    // ---- Setters ----
    public void setStatus(BookingStatus s) { this.status = s; }
    public void setCheckOut(LocalDate d)   { this.checkOut = d; }
}