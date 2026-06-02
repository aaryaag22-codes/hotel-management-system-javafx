package com.hotel.service;

import com.hotel.data.DataStore;
import com.hotel.model.Customer;

import java.util.List;
import java.util.function.Consumer;

/**
 * ReminderService.java - Runs in a background thread to check for inactive customers
 *
 * Demonstrates:
 *   Week 3: Thread creation using Runnable interface
 *   Week 3: sleep() to simulate background checking
 *   Week 4: synchronized callback to update UI safely
 */
public class ReminderService implements Runnable {

    private final DataStore       store;
    private final PrintService    printService;
    private final Consumer<String> onReminder;  // UI callback when a reminder is found
    private volatile boolean      running = true;

    public ReminderService(Consumer<String> onReminder) {
        this.store        = DataStore.getInstance();
        this.printService = new PrintService();
        this.onReminder   = onReminder;
    }

    @Override
    public void run() {
        System.out.println("[ReminderService] Background thread started.");
        while (running) {
            try {
                // Week 3: sleep() - check every 30 seconds in real app
                // Using 10s here for demo purposes
                Thread.sleep(10_000);
                checkInactiveCustomers();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
        System.out.println("[ReminderService] Background thread stopped.");
    }

    // Week 4: synchronized to prevent race conditions
    private synchronized void checkInactiveCustomers() {
        List<Customer> inactive = store.getInactiveCustomers();
        if (!inactive.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("📋 Inactive Customer Reminders\n");
            sb.append("─────────────────────────────\n");
            for (Customer c : inactive) {
                sb.append(printService.generateSmsReminder(c)).append("\n\n");
            }
            // Week 3: yield() hint to scheduler before UI update
            Thread.yield();
            onReminder.accept(sb.toString());
        }
    }

    public void stop() {
        running = false;
    }

    /** Convenience: run a one-time check immediately (for UI button) */
    public String checkNow() {
        List<Customer> inactive = store.getInactiveCustomers();
        if (inactive.isEmpty()) return "✅ All customers are active. No reminders needed.";
        StringBuilder sb = new StringBuilder();
        for (Customer c : inactive)
            sb.append(printService.generateSmsReminder(c)).append("\n\n");
        return sb.toString();
    }
}