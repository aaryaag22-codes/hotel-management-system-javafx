package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * GiftVoucher.java - Loyalty gift voucher for valued guests
 */
public class GiftVoucher implements Serializable {

    private String voucherCode;
    private int    customerId;
    private double discountPercent;
    private LocalDate expiryDate;
    private boolean used;

    public GiftVoucher(String voucherCode, int customerId,
                       double discountPercent, LocalDate expiryDate) {
        this.voucherCode      = voucherCode;
        this.customerId       = customerId;
        this.discountPercent  = discountPercent;
        this.expiryDate       = expiryDate;
        this.used             = false;
    }

    public String    getVoucherCode()     { return voucherCode; }
    public int       getCustomerId()      { return customerId; }
    public double    getDiscountPercent() { return discountPercent; }
    public LocalDate getExpiryDate()      { return expiryDate; }
    public boolean   isUsed()             { return used; }
    public void      setUsed(boolean u)   { this.used = u; }

    public boolean isValid() {
        return !used && LocalDate.now().isBefore(expiryDate);
    }

    @Override
    public String toString() {
        return "Voucher " + voucherCode + " | " + discountPercent
               + "% off | Expires: " + expiryDate
               + (used ? " [USED]" : (isValid() ? " [VALID]" : " [EXPIRED]"));
    }
}