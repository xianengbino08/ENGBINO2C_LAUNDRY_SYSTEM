package com.laundry.model;

public record LaundryOrder(int orderId, String customerName, String serviceName,
                           double weightKg, double totalPrice, String orderStatus, String paymentStatus) {
}