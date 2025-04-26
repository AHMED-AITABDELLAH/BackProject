package com.savorybites.backend.model;

public enum OrderStatus {
    UNCOMPLETED,
    COMPLETED,
    ACCEPTED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED, REJECTED
}
