package com.locallocket.backend.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FeeCalculationService {

    // Platform Fee Constants
    private static final BigDecimal PLATFORM_FEE_FIXED = new BigDecimal("10.00");
    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.10"); // 10%
    private static final BigDecimal PLATFORM_FEE_THRESHOLD = new BigDecimal("100.00");

    // Delivery Fee Constants
    private static final BigDecimal DELIVERY_FEE_NEARBY = new BigDecimal("10.00");   // ≤ 1km
    private static final BigDecimal DELIVERY_FEE_STANDARD = new BigDecimal("15.00"); // > 1km
    private static final BigDecimal DELIVERY_FEE_DEFAULT = new BigDecimal("12.00");  // Unknown distance

    // Distance threshold in kilometers
    private static final double NEARBY_DISTANCE_KM = 1.0;

    /**
     * Calculate platform fee based on subtotal
     * Rule: ₹10 if subtotal < ₹100, otherwise 10% of subtotal
     *
     * @param subtotal Order subtotal
     * @return Platform fee amount
     */
    public BigDecimal calculatePlatformFee(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (subtotal.compareTo(PLATFORM_FEE_THRESHOLD) < 0) {
            // Less than ₹100: Fixed ₹10 fee
            return PLATFORM_FEE_FIXED;
        } else {
            // ₹100 or more: 10% of subtotal
            return subtotal.multiply(PLATFORM_FEE_PERCENTAGE)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculate delivery fee based on distance
     * Rule: ₹10 for ≤1km, ₹15 for >1km
     *
     * @param distanceKm Distance in kilometers
     * @return Delivery fee amount
     */
    public BigDecimal calculateDeliveryFee(Double distanceKm) {
        if (distanceKm == null || distanceKm < 0) {
            return DELIVERY_FEE_DEFAULT;
        }

        if (distanceKm <= NEARBY_DISTANCE_KM) {
            return DELIVERY_FEE_NEARBY;
        } else {
            return DELIVERY_FEE_STANDARD;
        }
    }

    /**
     * Calculate delivery fee when distance is unknown
     * Returns default delivery fee of ₹12
     *
     * @return Default delivery fee amount
     */
    public BigDecimal calculateDeliveryFee() {
        return DELIVERY_FEE_DEFAULT;
    }

    /**
     * Calculate delivery fee based on vendor location and user location
     * Uses Haversine formula to calculate distance
     *
     * @param userLat User latitude
     * @param userLon User longitude
     * @param vendorLat Vendor latitude
     * @param vendorLon Vendor longitude
     * @return Delivery fee amount
     */
    public BigDecimal calculateDeliveryFee(Double userLat, Double userLon,
                                           Double vendorLat, Double vendorLon) {
        if (userLat == null || userLon == null || vendorLat == null || vendorLon == null) {
            return DELIVERY_FEE_DEFAULT;
        }

        double distanceKm = calculateDistance(userLat, userLon, vendorLat, vendorLon);
        return calculateDeliveryFee(distanceKm);
    }

    /**
     * Calculate total fees (platform + delivery)
     *
     * @param subtotal Order subtotal
     * @param distanceKm Distance in kilometers
     * @return OrderFees object with breakdown
     */
    public OrderFees calculateTotalFees(BigDecimal subtotal, Double distanceKm) {
        BigDecimal platformFee = calculatePlatformFee(subtotal);
        BigDecimal deliveryFee = calculateDeliveryFee(distanceKm);
        BigDecimal totalFees = platformFee.add(deliveryFee);
        BigDecimal totalAmount = subtotal.add(totalFees);

        return new OrderFees(subtotal, platformFee, deliveryFee, totalAmount);
    }

    /**
     * Calculate total fees with default delivery
     *
     * @param subtotal Order subtotal
     * @return OrderFees object with breakdown
     */
    public OrderFees calculateTotalFees(BigDecimal subtotal) {
        return calculateTotalFees(subtotal, null);
    }

    /**
     * Check if order qualifies for any discounts
     *
     * @param subtotal Order subtotal
     * @return true if discount applicable
     */
    public boolean isDiscountApplicable(BigDecimal subtotal) {
        return subtotal.compareTo(PLATFORM_FEE_THRESHOLD) >= 0;
    }

    /**
     * Get minimum order amount for percentage-based platform fee
     *
     * @return Minimum order threshold
     */
    public BigDecimal getPlatformFeeThreshold() {
        return PLATFORM_FEE_THRESHOLD;
    }

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Inner class to represent order fees breakdown
     */
    public static class OrderFees {
        private final BigDecimal subtotal;
        private final BigDecimal platformFee;
        private final BigDecimal deliveryFee;
        private final BigDecimal totalAmount;

        public OrderFees(BigDecimal subtotal, BigDecimal platformFee,
                         BigDecimal deliveryFee, BigDecimal totalAmount) {
            this.subtotal = subtotal;
            this.platformFee = platformFee;
            this.deliveryFee = deliveryFee;
            this.totalAmount = totalAmount;
        }

        // Getters
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getPlatformFee() { return platformFee; }
        public BigDecimal getDeliveryFee() { return deliveryFee; }
        public BigDecimal getTotalAmount() { return totalAmount; }

        @Override
        public String toString() {
            return String.format("OrderFees{subtotal=%.2f, platformFee=%.2f, deliveryFee=%.2f, total=%.2f}",
                    subtotal, platformFee, deliveryFee, totalAmount);
        }
    }
}
