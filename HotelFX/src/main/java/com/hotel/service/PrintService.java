package com.hotel.service;

import com.hotel.data.DataStore;
import com.hotel.model.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PrintService.java - Generates formatted text for all printable documents
 * Demonstrates: Week 5 (File I/O - writes print output to file)
 */
public class PrintService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final String SEPARATOR =
            "============================================================";
    private static final String LINE =
            "------------------------------------------------------------";

    private final DataStore store = DataStore.getInstance();

    // =====================================================================
    //  BILL RECEIPT
    // =====================================================================
    public String generateReceipt(int bookingId) {
        Booking booking = store.getBookingById(bookingId);
        if (booking == null) return "Booking not found.";

        Customer customer = store.getCustomerById(booking.getCustomerId());
        Room room         = store.getRoomByNumber(booking.getRoomNumber());
        List<FoodOrder> orders = store.getOrdersForBooking(bookingId);

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR).append("\n");
        sb.append("          🏨  GRAND VISTA HOTEL\n");
        sb.append("        123 Palace Road, Udupi - 576101\n");
        sb.append("     Tel: +91-820-1234567 | grandvista@hotel.com\n");
        sb.append(SEPARATOR).append("\n");
        sb.append("                   BILL RECEIPT\n");
        sb.append(LINE).append("\n");

        // Customer details
        if (customer != null) {
            sb.append("Guest Name    : ").append(customer.getName()).append("\n");
            sb.append("Contact       : ").append(customer.getContactNumber()).append("\n");
        }
        sb.append("Booking ID    : #").append(booking.getBookingId()).append("\n");
        sb.append("Room No.      : ").append(booking.getRoomNumber()).append("\n");
        if (room != null)
            sb.append("Room Type     : ").append(room.getRoomType()).append("\n");
        sb.append("Check-In      : ").append(booking.getCheckIn().format(DATE_FMT)).append("\n");
        sb.append("Check-Out     : ").append(booking.getCheckOut().format(DATE_FMT)).append("\n");
        sb.append("No. of Days   : ").append(booking.getNumberOfDays()).append("\n");
        sb.append("No. of Guests : ").append(booking.getNumberOfGuests()).append("\n");
        sb.append(LINE).append("\n");

        // Room charges
        double roomRate  = (room != null) ? room.getPricePerDay() : 0;
        double roomTotal = roomRate * booking.getNumberOfDays();
        sb.append(String.format("%-30s ₹%8.2f%n", "Room Charges:", roomTotal));

        // Food charges
        double foodTotal = 0;
        if (!orders.isEmpty()) {
            sb.append("\nRoom Service / Food Orders:\n");
            for (FoodOrder order : orders) {
                for (Map.Entry<FoodItem, Integer> entry : order.getItems().entrySet()) {
                    double lineTotal = entry.getKey().getPrice() * entry.getValue();
                    foodTotal += lineTotal;
                    sb.append(String.format("  %-28s x%d  ₹%6.2f%n",
                            entry.getKey().getName(), entry.getValue(), lineTotal));
                }
            }
            sb.append(String.format("%-30s ₹%8.2f%n", "Food Total:", foodTotal));
        }

        double subtotal = roomTotal + foodTotal;
        double discount = booking.getDiscountApplied();
        double discountAmt = subtotal * discount / 100.0;
        double grandTotal = subtotal - discountAmt;
        double gst        = grandTotal * 0.12;   // 12% GST
        double finalTotal = grandTotal + gst;

        sb.append(LINE).append("\n");
        sb.append(String.format("%-30s ₹%8.2f%n", "Sub Total:", subtotal));
        if (discount > 0) {
            sb.append(String.format("%-30s -₹%7.2f%n",
                    "Discount (" + discount + "%):", discountAmt));
            sb.append("  Note: ").append(booking.getSpecialNote()).append("\n");
        }
        sb.append(String.format("%-30s ₹%8.2f%n", "After Discount:", grandTotal));
        sb.append(String.format("%-30s ₹%8.2f%n", "GST (12%):", gst));
        sb.append(LINE).append("\n");
        sb.append(String.format("%-30s ₹%8.2f%n", "GRAND TOTAL:", finalTotal));
        sb.append(SEPARATOR).append("\n");
        sb.append("     Thank you for choosing Grand Vista Hotel!\n");
        sb.append("          We look forward to your next visit.\n");
        sb.append(SEPARATOR).append("\n");

        // Save receipt to file (Week 5: FileWriter usage)
        saveToFile("receipt_" + bookingId + ".txt", sb.toString());
        return sb.toString();
    }

    // =====================================================================
    //  CUSTOMER DETAILS
    // =====================================================================
    public String printCustomerDetails(int customerId) {
        Customer c = store.getCustomerById(customerId);
        if (c == null) return "Customer not found.";

        List<Booking> bookings = store.getBookingsByCustomer(customerId);

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR).append("\n");
        sb.append("          🏨  GRAND VISTA HOTEL\n");
        sb.append("              CUSTOMER PROFILE\n");
        sb.append(SEPARATOR).append("\n");
        sb.append("Customer ID   : ").append(c.getCustomerId()).append("\n");
        sb.append("Name          : ").append(c.getName()).append("\n");
        sb.append("Contact       : ").append(c.getContactNumber()).append("\n");
        sb.append("Email         : ").append(c.getEmail()).append("\n");
        sb.append("Total Bookings: ").append(c.getTotalBookings()).append("\n");
        sb.append("Total Guests  : ").append(c.getTotalGuests()).append("\n");
        sb.append(LINE).append("\n");
        sb.append("  Booking History:\n");
        if (bookings.isEmpty()) {
            sb.append("  No bookings found.\n");
        } else {
            for (Booking b : bookings) {
                sb.append("  #").append(b.getBookingId())
                  .append(" | Room ").append(b.getRoomNumber())
                  .append(" | ").append(b.getCheckIn().format(DATE_FMT))
                  .append(" → ").append(b.getCheckOut().format(DATE_FMT))
                  .append(" | ₹").append(String.format("%.2f", b.getTotalAmount()))
                  .append(" | ").append(b.getStatus()).append("\n");
            }
        }

        // Vouchers
        sb.append(LINE).append("\n");
        sb.append("  Gift Vouchers:\n");
        boolean hasVoucher = false;
        for (GiftVoucher v : store.getVouchers()) {
            if (v.getCustomerId() == customerId) {
                sb.append("  ").append(v).append("\n");
                hasVoucher = true;
            }
        }
        if (!hasVoucher) sb.append("  No vouchers.\n");
        sb.append(SEPARATOR).append("\n");

        saveToFile("customer_" + customerId + ".txt", sb.toString());
        return sb.toString();
    }

    // =====================================================================
    //  HOTEL BROCHURE
    // =====================================================================
    public String printBrochure() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR).append("\n");
        sb.append("     ✨  WELCOME TO GRAND VISTA HOTEL  ✨\n");
        sb.append("       Where Luxury Meets Warm Hospitality\n");
        sb.append("        123 Palace Road, Udupi - 576101\n");
        sb.append("    Tel: +91-820-1234567 | grandvista@hotel.com\n");
        sb.append(SEPARATOR).append("\n\n");

        sb.append("  🛏  OUR ROOMS:\n");
        sb.append(LINE).append("\n");
        for (Room r : store.getRooms()) {
            sb.append(String.format("  Room %-4d | %-8s | ₹%5.0f/night | %s%n",
                    r.getRoomNumber(), r.getRoomType(),
                    r.getPricePerDay(), r.getAmenities()));
        }

        sb.append("\n  🍽  DINING HIGHLIGHTS:\n");
        sb.append(LINE).append("\n");
        for (FoodItem item : store.getMenuItems()) {
            sb.append(String.format("  %-28s ₹%5.0f  %s%n",
                    item.getName(), item.getPrice(),
                    item.isVegetarian() ? "🌿" : "🍗"));
        }

        sb.append("\n  🎁  LOYALTY PROGRAM:\n");
        sb.append(LINE).append("\n");
        sb.append("  • 3+ bookings → 5% discount\n");
        sb.append("  • 5+ bookings → 10% discount + Gift Voucher\n");
        sb.append("  • 10+ bookings → 20% discount + Premium Voucher\n");
        sb.append("  • Birthday / Anniversary → Extra 10% off\n");
        sb.append("  • Wedding party (10+ guests) → 15% group discount\n");

        sb.append("\n  ⭐  HOTEL POLICIES:\n");
        sb.append(LINE).append("\n");
        sb.append("  • Check-in: 12:00 PM | Check-out: 11:00 AM\n");
        sb.append("  • Early check-in & late check-out on request\n");
        sb.append("  • Free Wi-Fi throughout the property\n");
        sb.append("  • Complimentary breakfast for Suite guests\n");
        sb.append(SEPARATOR).append("\n");

        saveToFile("hotel_brochure.txt", sb.toString());
        return sb.toString();
    }

    // =====================================================================
    //  FOOD MENU
    // =====================================================================
    public String printFoodMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR).append("\n");
        sb.append("       🍽  GRAND VISTA HOTEL - FOOD MENU  🍽\n");
        sb.append(SEPARATOR).append("\n");

        FoodItem.FoodCategory[] cats = FoodItem.FoodCategory.values();
        for (FoodItem.FoodCategory cat : cats) {
            sb.append("\n  ── ").append(cat).append(" ──\n");
            for (FoodItem item : store.getMenuItems()) {
                if (item.getCategory() == cat) {
                    sb.append(String.format("  %-3d. %-28s ₹%5.0f  %s%n",
                            item.getItemId(), item.getName(), item.getPrice(),
                            item.isVegetarian() ? "🌿 Veg" : "🍗 Non-Veg"));
                }
            }
        }
        sb.append("\n").append(SEPARATOR).append("\n");
        sb.append("  All prices are subject to 5% service charge.\n");
        sb.append(SEPARATOR).append("\n");

        saveToFile("food_menu.txt", sb.toString());
        return sb.toString();
    }

    // =====================================================================
    //  SMS REMINDER (simulated - prints message)
    // =====================================================================
    public String generateSmsReminder(Customer c) {
        return "📱 SMS to " + c.getContactNumber() + ":\n"
             + "Dear " + c.getName() + ", we miss you at Grand Vista Hotel!\n"
             + "It's been a while since your last visit. Book now and enjoy\n"
             + "special returning guest offers. Call: +91-820-1234567";
    }

    // =====================================================================
    //  File Write  (Week 5: FileWriter / CharacterStream)
    // =====================================================================
    private void saveToFile(String filename, String content) {
        new java.io.File("data/prints").mkdirs();
        try (java.io.FileWriter fw = new java.io.FileWriter("data/prints/" + filename)) {
            fw.write(content);
        } catch (java.io.IOException e) {
            System.err.println("Could not save print file: " + e.getMessage());
        }
    }
}