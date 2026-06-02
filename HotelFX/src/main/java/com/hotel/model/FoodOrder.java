package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * FoodOrder.java - A room-service or dining order placed by a guest
 */
public class FoodOrder implements Serializable {

    private int orderId;
    private int bookingId;      // which booking/room
    private int roomNumber;
    private Map<FoodItem, Integer> items;  // item -> quantity  (Week 8: HashMap)
    private LocalDateTime orderTime;
    private double totalCost;
    private boolean paid;

    public FoodOrder(int orderId, int bookingId, int roomNumber) {
        this.orderId   = orderId;
        this.bookingId = bookingId;
        this.roomNumber = roomNumber;
        this.items     = new HashMap<>();
        this.orderTime = LocalDateTime.now();
        this.paid      = false;
        this.totalCost = 0;
    }

    public void addItem(FoodItem item, int qty) {
        items.merge(item, qty, Integer::sum);
        recalculate();
    }

    private void recalculate() {
        totalCost = 0;
        for (Map.Entry<FoodItem, Integer> e : items.entrySet()) {
            totalCost += e.getKey().getPrice() * e.getValue();
        }
    }

    public int                   getOrderId()    { return orderId; }
    public int                   getBookingId()  { return bookingId; }
    public int                   getRoomNumber() { return roomNumber; }
    public Map<FoodItem,Integer> getItems()      { return items; }
    public LocalDateTime         getOrderTime()  { return orderTime; }
    public double                getTotalCost()  { return totalCost; }
    public boolean               isPaid()        { return paid; }
    public void                  setPaid(boolean p) { this.paid = p; }
}