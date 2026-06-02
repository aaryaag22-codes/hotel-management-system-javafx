package com.hotel.data;

import com.hotel.model.*;
import com.hotel.model.Booking.BookingStatus;

import java.io.*;
import java.time.LocalDate;
import java.util.*;


/**
 * DataStore.java - Central in-memory store + file persistence
 *
 * Demonstrates:
 *   Week 6  : Serialization / Deserialization for persistent storage
 *   Week 8  : ArrayList, HashMap, Iterator usage
 *   Week 7  : Generic Pair utility (see inner class)
 */
public class DataStore {

    // ---- Week 8: Collections ----
    private List<Room>        rooms     = new ArrayList<>();
    private List<Customer>    customers = new ArrayList<>();
    private List<Booking>     bookings  = new ArrayList<>();
    private List<FoodItem>    menuItems = new ArrayList<>();
    private List<FoodOrder>   foodOrders= new ArrayList<>();
    private List<GiftVoucher> vouchers  = new ArrayList<>();

    // Week 8: HashMap - room number -> booking id (quick lookup)
    private Map<Integer, Integer> activeRoomBooking = new HashMap<>();

    // File paths for serialization
    private static final String ROOMS_FILE    = "data/rooms.dat";
    private static final String CUSTOMERS_FILE= "data/customers.dat";
    private static final String BOOKINGS_FILE = "data/bookings.dat";
    private static final String ORDERS_FILE   = "data/orders.dat";
    private static final String VOUCHERS_FILE = "data/vouchers.dat";

    private int nextCustomerId = 1;
    private int nextBookingId  = 1;
    private int nextOrderId    = 1;

    // ---- Room-change listeners (notified whenever a room is added/removed) ----
    private final List<Runnable> roomChangeListeners = new ArrayList<>();

    public void addRoomChangeListener(Runnable listener) {
        roomChangeListeners.add(listener);
    }

    private void fireRoomChanged() {
        roomChangeListeners.forEach(Runnable::run);
    }

    // Singleton pattern so every UI tab shares the same data
    private static DataStore instance;
    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private DataStore() {
        new File("data").mkdirs();  // create data folder if missing
        loadAll();
        if (rooms.isEmpty()) seedDefaultData();
         if (menuItems.isEmpty()) {
            menuItems.clear(); 
        menuItems.add(new FoodItem(1,  "Masala Dosa",       120, FoodItem.FoodCategory.BREAKFAST, true));
        menuItems.add(new FoodItem(2,  "Idli Sambar",        80, FoodItem.FoodCategory.BREAKFAST, true));
        menuItems.add(new FoodItem(3,  "Poha",               70, FoodItem.FoodCategory.BREAKFAST, true));
        menuItems.add(new FoodItem(4,  "Paneer Butter Masala",280, FoodItem.FoodCategory.LUNCH,   true));
        menuItems.add(new FoodItem(5,  "Dal Makhani",        220, FoodItem.FoodCategory.LUNCH,    true));
        menuItems.add(new FoodItem(6,  "Chicken Biryani",   350, FoodItem.FoodCategory.LUNCH,     false));
        menuItems.add(new FoodItem(7,  "Grilled Fish",      420, FoodItem.FoodCategory.DINNER,    false));
        menuItems.add(new FoodItem(8,  "Veg Thali",         180, FoodItem.FoodCategory.DINNER,    true));
        menuItems.add(new FoodItem(9,  "Fresh Lime Soda",    60, FoodItem.FoodCategory.BEVERAGE,  true));
        menuItems.add(new FoodItem(10, "Mango Lassi",        90, FoodItem.FoodCategory.BEVERAGE,  true));
        menuItems.add(new FoodItem(11, "Masala Chai",        40, FoodItem.FoodCategory.BEVERAGE,  true));
        menuItems.add(new FoodItem(12, "Samosa (2 pcs)",     60, FoodItem.FoodCategory.SNACKS,    true));
        menuItems.add(new FoodItem(13, "Spring Rolls",      110, FoodItem.FoodCategory.SNACKS,    true));

         }

        rebuildRoomBookingMap();
    }

    // =====================================================================
    //  SEED DATA  (first run only)
    // =====================================================================
    private void seedDefaultData() {
        // Rooms
        rooms.add(new Room(101, "Single",  1500, "WiFi, TV"));
        rooms.add(new Room(102, "Single",  1500, "WiFi, TV"));
        rooms.add(new Room(201, "Double",  2500, "WiFi, TV, AC"));
        rooms.add(new Room(202, "Double",  2500, "WiFi, TV, AC"));
        rooms.add(new Room(301, "Deluxe",  4000, "WiFi, TV, AC, Mini-Bar"));
        rooms.add(new Room(302, "Deluxe",  4000, "WiFi, TV, AC, Mini-Bar"));
        rooms.add(new Room(401, "Suite",   7000, "WiFi, TV, AC, Jacuzzi, Butler"));
        rooms.add(new Room(402, "Suite",   7000, "WiFi, TV, AC, Jacuzzi, Butler"));
        
        // Food Menu
        saveAll();
    }

    // =====================================================================
    //  ROOM OPERATIONS
    // =====================================================================
    public List<Room> getRooms()         { return rooms; }

    public List<Room> getAvailableRooms() {
        List<Room> avail = new ArrayList<>();
        // Week 8: Iterator usage
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            Room r = it.next();
            if (r.isAvailable()) avail.add(r);
        }
        return avail;
    }

    public Room getRoomByNumber(int num) {
        for (Room r : rooms) if (r.getRoomNumber() == num) return r;
        return null;
    }

    public void addRoom(Room r) {
        rooms.add(r);
        saveAll();
        fireRoomChanged();
    }

    public void updateRoom(Room r) { saveAll(); }

    // =====================================================================
    //  CUSTOMER OPERATIONS
    // =====================================================================
    public List<Customer> getCustomers() { return customers; }

    public Customer getCustomerById(int id) {
        for (Customer c : customers) if (c.getCustomerId() == id) return c;
        return null;
    }

    public Customer getCustomerByName(String name) {
        for (Customer c : customers)
            if (c.getName().equalsIgnoreCase(name)) return c;
        return null;
    }

    public int addCustomer(Customer c) {
        customers.add(c);
        saveAll();
        return c.getCustomerId();
    }

    public int nextCustomerId() { return nextCustomerId++; }

    // =====================================================================
    //  BOOKING OPERATIONS
    // =====================================================================
    public List<Booking> getBookings() { return bookings; }

    public Booking getBookingById(int id) {
        for (Booking b : bookings) if (b.getBookingId() == id) return b;
        return null;
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings)
            if (b.getCustomerId() == customerId) result.add(b);
        return result;
    }

    public int addBooking(Booking b) {
        bookings.add(b);
        activeRoomBooking.put(b.getRoomNumber(), b.getBookingId());
        Room r = getRoomByNumber(b.getRoomNumber());
        if (r != null) r.setAvailable(false);
        // Update customer stats
        Customer c = getCustomerById(b.getCustomerId());
        if (c != null) {
            c.incrementBookings();
            c.addGuests(b.getNumberOfGuests());
        }
        saveAll();
        return b.getBookingId();
    }

    public int nextBookingId() { return nextBookingId++; }

    public void checkoutBooking(int bookingId) {
        Booking b = getBookingById(bookingId);
        if (b == null) return;
        b.setStatus(BookingStatus.CHECKED_OUT);
        Room r = getRoomByNumber(b.getRoomNumber());
        if (r != null) r.setAvailable(true);
        activeRoomBooking.remove(b.getRoomNumber());
        saveAll();
    }

    public Integer getActiveBookingForRoom(int roomNum) {
        return activeRoomBooking.get(roomNum);
    }

    // =====================================================================
    //  FOOD MENU + ORDERS
    // =====================================================================
    public List<FoodItem>  getMenuItems()  { return menuItems; }
    public List<FoodOrder> getFoodOrders() { return foodOrders; }

    public int nextOrderId() { return nextOrderId++; }

    public void addFoodOrder(FoodOrder order) {
        foodOrders.add(order);
        saveAll();
    }

    public List<FoodOrder> getOrdersForBooking(int bookingId) {
        List<FoodOrder> result = new ArrayList<>();
        for (FoodOrder o : foodOrders)
            if (o.getBookingId() == bookingId) result.add(o);
        return result;
    }

    // =====================================================================
    //  VOUCHERS
    // =====================================================================
    public List<GiftVoucher> getVouchers() { return vouchers; }

    public void addVoucher(GiftVoucher v) {
        vouchers.add(v);
        saveAll();
    }

    public GiftVoucher findVoucher(String code, int customerId) {
        for (GiftVoucher v : vouchers)
            if (v.getVoucherCode().equalsIgnoreCase(code)
                    && v.getCustomerId() == customerId && v.isValid())
                return v;
        return null;
    }

    // =====================================================================
    //  DISCOUNT ENGINE  (Week 1 polymorphism concept applied here)
    // =====================================================================
    /**
     * Returns discount percentage based on loyalty + occasion
     * Returns a Week 7-style Pair<Double, String> = <percent, reason>
     */
    public Pair<Double, String> calculateDiscount(Customer c, String occasion,
                                                  int guests, String voucherCode) {
        double discount = 0;
        StringBuilder reason = new StringBuilder();

        // Loyalty discount
        if (c.getTotalBookings() >= 10) {
            discount += 20;
            reason.append("Loyalty 20% | ");
        } else if (c.getTotalBookings() >= 5) {
            discount += 10;
            reason.append("Loyalty 10% | ");
        } else if (c.getTotalBookings() >= 3) {
            discount += 5;
            reason.append("Loyalty 5% | ");
        }

        // Large group discount
        if (guests >= 10) {
            discount += 15;
            reason.append("Large Group 15% | ");
        } else if (guests >= 5) {
            discount += 8;
            reason.append("Group 8% | ");
        }

        // Special occasion
        if ("Birthday".equalsIgnoreCase(occasion)) {
            discount += 10;
            reason.append("Birthday 10% | ");
        } else if ("Wedding".equalsIgnoreCase(occasion)) {
            discount += 15;
            reason.append("Wedding 15% | ");
        } else if ("Anniversary".equalsIgnoreCase(occasion)) {
            discount += 8;
            reason.append("Anniversary 8% | ");
        }

        // Voucher
        if (voucherCode != null && !voucherCode.isBlank()) {
            GiftVoucher v = findVoucher(voucherCode, c.getCustomerId());
            if (v != null) {
                discount += v.getDiscountPercent();
                reason.append("Voucher ").append(v.getDiscountPercent()).append("% | ");
                v.setUsed(true);
                saveAll();
            }
        }

        // Cap at 40%
        if (discount > 40) discount = 40;
        if (reason.length() == 0) reason.append("No discount");
        return new Pair<>(discount, reason.toString());
    }

    /**
     * Issue a gift voucher to loyal customers (called after checkout)
     */
    public GiftVoucher maybeIssueVoucher(Customer c) {
        if (c.getTotalBookings() == 5 || c.getTotalBookings() == 10
                || c.getTotalBookings() % 10 == 0) {
            double pct = c.getTotalBookings() >= 10 ? 15 : 10;
            String code = "GV" + c.getCustomerId() + "-" + System.currentTimeMillis() % 10000;
            GiftVoucher v = new GiftVoucher(code, c.getCustomerId(),
                    pct, LocalDate.now().plusMonths(3));
            addVoucher(v);
            return v;
        }
        return null;
    }

    /**
     * Check which customers haven't booked in 60+ days (for SMS reminder)
     */
    public List<Customer> getInactiveCustomers() {
        Map<Integer, LocalDate> lastBookingDate = new HashMap<>();
        for (Booking b : bookings) {
            LocalDate ci = b.getCheckIn();
            lastBookingDate.merge(b.getCustomerId(), ci,
                    (existing, newVal) -> existing.isAfter(newVal) ? existing : newVal);
        }
        List<Customer> inactive = new ArrayList<>();
        LocalDate cutoff = LocalDate.now().minusDays(60);
        for (Customer c : customers) {
            LocalDate last = lastBookingDate.get(c.getCustomerId());
            if (last == null || last.isBefore(cutoff)) inactive.add(c);
        }
        return inactive;
    }

    // =====================================================================
    //  SERIALIZATION  (Week 6)
    // =====================================================================
    @SuppressWarnings("unchecked")
    private <T> List<T> loadList(String path) {
        File f = new File(path);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private <T> void saveList(String path, List<T> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(list);
        } catch (IOException e) {
            System.err.println("Save error: " + path + " -> " + e.getMessage());
        }
    }

    public void loadAll() {
        rooms      = loadList(ROOMS_FILE);
        customers  = loadList(CUSTOMERS_FILE);
        bookings   = loadList(BOOKINGS_FILE);
        foodOrders = loadList(ORDERS_FILE);
        vouchers   = loadList(VOUCHERS_FILE);
        // Recalculate next IDs
        customers.forEach(c -> { if (c.getCustomerId() >= nextCustomerId)
            nextCustomerId = c.getCustomerId() + 1; });
        bookings.forEach(b  -> { if (b.getBookingId()  >= nextBookingId)
            nextBookingId  = b.getBookingId()  + 1; });
        foodOrders.forEach(o-> { if (o.getOrderId()    >= nextOrderId)
            nextOrderId    = o.getOrderId()    + 1; });
        if (rooms.isEmpty()) seedDefaultData();
    }

    public void saveAll() {
        saveList(ROOMS_FILE,     rooms);
        saveList(CUSTOMERS_FILE, customers);
        saveList(BOOKINGS_FILE,  bookings);
        saveList(ORDERS_FILE,    foodOrders);
        saveList(VOUCHERS_FILE,  vouchers);
    }

    private void rebuildRoomBookingMap() {
        activeRoomBooking.clear();
        for (Booking b : bookings)
            if (b.getStatus() == BookingStatus.ACTIVE)
                activeRoomBooking.put(b.getRoomNumber(), b.getBookingId());
    }

    // =====================================================================
    //  Week 7: Generic Pair class
    // =====================================================================
    public static class Pair<T, U> {
        private final T first;
        private final U second;
        public Pair(T first, U second) { this.first = first; this.second = second; }
        public T getFirst()  { return first; }
        public U getSecond() { return second; }
    }
}